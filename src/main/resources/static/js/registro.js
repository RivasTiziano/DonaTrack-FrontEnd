/*
 * Registro público: agregar y quitar bloques de representantes, y estado de envío.
 * Progresivo: sin JavaScript quedan los bloques que vienen del servidor y los controles con clase js-only no se muestran.
 */
(function () {
  'use strict';

  function renumerar(contenedor) {
    contenedor.querySelectorAll('[data-registro-bloque]').forEach(function (bloque, indice) {
      bloque.querySelectorAll('[name]').forEach(function (campo) {
        campo.name = campo.name.replace(/representantes\[\d+\]/, 'representantes[' + indice + ']');
      });
      bloque.querySelectorAll('[id]').forEach(function (elemento) {
        elemento.id = elemento.id.replace(/^rep-\d+-/, 'rep-' + indice + '-');
      });
      bloque.querySelectorAll('label[for]').forEach(function (etiqueta) {
        etiqueta.htmlFor = etiqueta.htmlFor.replace(/^rep-\d+-/, 'rep-' + indice + '-');
      });
      var numero = bloque.querySelector('[data-registro-numero]');
      if (numero) numero.textContent = '';
      var quitar = bloque.querySelector('[data-registro-quitar]');
      if (quitar) quitar.hidden = indice === 0;
    });
  }

  function limpiar(bloque) {
    bloque.querySelectorAll('input').forEach(function (campo) { campo.value = ''; });
    bloque.querySelectorAll('select').forEach(function (campo) { campo.selectedIndex = 0; });
    bloque.querySelectorAll('.form-error').forEach(function (error) { error.remove(); });
  }

  function agregar(contenedor) {
    var bloques = contenedor.querySelectorAll('[data-registro-bloque]');
    var nuevo = bloques[bloques.length - 1].cloneNode(true);
    nuevo.querySelectorAll('.animated-select').forEach(function (wrapper) {
      var select = wrapper.querySelector('select');
      select.classList.remove('animated-select-native');
      select.removeAttribute('aria-hidden');
      select.removeAttribute('tabindex');
      wrapper.replaceWith(select);
    });
    nuevo.querySelectorAll('label[id^="dropdown-label-"]').forEach(function (label) { label.removeAttribute('id'); });
    limpiar(nuevo);
    contenedor.querySelector('[data-registro-bloques]').appendChild(nuevo);
    renumerar(contenedor);
    if (window.initializeAnimatedSelects) window.initializeAnimatedSelects();
  }

  function quitar(contenedor, bloque) {
    var minimo = Number(contenedor.getAttribute('data-registro-minimo') || 0);
    if (contenedor.querySelectorAll('[data-registro-bloque]').length <= minimo) return;
    if (bloque === contenedor.querySelector('[data-registro-bloque]')) return;
    bloque.remove();
    renumerar(contenedor);
  }

  document.querySelectorAll('[data-registro-representantes]').forEach(function (contenedor) {
    contenedor.querySelectorAll('.js-only').forEach(function (control) { control.classList.remove('js-only'); });
    renumerar(contenedor);
    contenedor.addEventListener('click', function (evento) {
      if (evento.target.closest('[data-registro-agregar]')) { agregar(contenedor); return; }
      var boton = evento.target.closest('[data-registro-quitar]');
      if (boton) quitar(contenedor, boton.closest('[data-registro-bloque]'));
    });
  });

  document.querySelectorAll('form[data-registro-form]').forEach(function (formulario) {
    formulario.addEventListener('submit', function () {
      var boton = formulario.querySelector('button[type="submit"]');
      if (!boton) return;
      boton.disabled = true;
      boton.classList.add('registro-cargando');
      boton.setAttribute('aria-busy', 'true');
      var texto = boton.querySelector('span');
      if (texto) texto.textContent = 'Creando tu cuenta…';
    });
  });
})();
