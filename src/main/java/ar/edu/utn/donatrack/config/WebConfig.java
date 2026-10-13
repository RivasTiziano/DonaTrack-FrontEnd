package ar.edu.utn.donatrack.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Registra el control de acceso solo en las áreas personales. Lo público (/, /login, /registro/**, /css/**, etc.) queda fuera. */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final AccesoInterceptor acceso;

    public WebConfig(AccesoInterceptor acceso) {
        this.acceso = acceso;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(acceso).addPathPatterns("/admin/**", "/donante/**", "/entidad/**");
    }
}
