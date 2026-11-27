package ar.edu.utn.donatrack.services.internal;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import java.io.IOException;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Rate limiting del backend (E6, req. 4). Como cliente liviano, el que llama a los servicios es este
 * servidor y no el navegador: sin ayuda, el backend vería a todos los visitantes con la IP del front y
 * les daría un único cupo compartido. En cada llamada se avisa la IP real del navegador:
 *
 *   X-Front-Auth: AUTH_FRONT_KEY   (el backend solo le cree a X-Client-IP si trae esta clave)
 *   X-Client-IP:  IP del navegador
 *
 * Sin clave configurada no se agrega nada: el backend ignoraría X-Client-IP de todos modos.
 * Sin request activo (tareas en segundo plano, tests) tampoco: no hay navegador del que hablar.
 * Las llamadas en paralelo de PanelAdminService copian el request al hilo, así que también lo llevan.
 */
@Component
public class IpDelNavegadorInterceptor implements ClientHttpRequestInterceptor {
    public static final String HEADER_FRONT_KEY = "X-Front-Auth";
    public static final String HEADER_CLIENT_IP = "X-Client-IP";
    static final String HEADER_FORWARDED_FOR = "X-Forwarded-For";

    // Solo caracteres posibles de IPv4/IPv6: no se reenvía texto arbitrario al backend
    private static final Pattern IP = Pattern.compile("[0-9a-fA-F:.]{2,45}");

    private final String claveFront;
    private final boolean confiarXForwardedFor;

    public IpDelNavegadorInterceptor(@Value("${apis.front-key:}") String claveFront,
                                     @Value("${frontend.confiar-x-forwarded-for:false}") boolean confiarXForwardedFor) {
        this.claveFront = claveFront == null ? "" : claveFront.trim();
        this.confiarXForwardedFor = confiarXForwardedFor;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        if (!claveFront.isEmpty()) {
            String ip = ipDelNavegador();
            if (ip != null) {
                request.getHeaders().set(HEADER_FRONT_KEY, claveFront);
                request.getHeaders().set(HEADER_CLIENT_IP, ip);
            }
        }
        return execution.execute(request, body);
    }

    /**
     * En Render hay un proxy delante del front: la IP del navegador es la ÚLTIMA de X-Forwarded-For,
     * la que agrega ese proxy (las anteriores las puede escribir el propio navegador). En local,
     * sin proxy, es la IP de la conexión.
     */
    String ipDelNavegador() {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes)) return null;
        HttpServletRequest request = attributes.getRequest();
        if (confiarXForwardedFor) {
            String forwardedFor = request.getHeader(HEADER_FORWARDED_FOR);
            if (forwardedFor != null) {
                String[] ips = forwardedFor.split(",");
                String ultima = normalizar(ips[ips.length - 1]);
                if (ultima != null) return ultima;
            }
        }
        return normalizar(request.getRemoteAddr());
    }

    private static String normalizar(String ip) {
        if (ip == null) return null;
        String limpia = ip.trim();
        return IP.matcher(limpia).matches() ? limpia.toLowerCase(Locale.ROOT) : null;
    }
}
