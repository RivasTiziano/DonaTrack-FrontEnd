
/* Local Material Icons (Google, Apache-2.0). Progressive enhancement, no external dependencies. */
(() => {
 'use strict';
 const paths={visibility:'M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zm0 12.5a5 5 0 110-10 5 5 0 010 10zm0-8a3 3 0 100 6 3 3 0 000-6z',off:'m2.1 3.51 3.27 3.27C3.47 8.06 1.94 9.86 1 12c1.73 4.39 6 7.5 11 7.5 1.55 0 3.03-.3 4.38-.84l4.11 4.11 1.41-1.41L3.51 2.1 2.1 3.51zM12 17a5 5 0 01-5-5c0-.65.13-1.26.36-1.83l1.57 1.57a3 3 0 003.33 3.33l1.57 1.57c-.57.23-1.18.36-1.83.36zm0-12.5c-1.45 0-2.84.27-4.13.75l2.17 2.17A5 5 0 0117 12c0 .68-.14 1.32-.38 1.91l3.05 3.05A13.49 13.49 0 0023 12c-1.73-4.39-6-7.5-11-7.5z'};
 const icon=(d)=>'<svg viewBox="0 0 24 24" aria-hidden="true" fill="currentColor"><path d="'+d+'"/></svg>';
 let nextId=0;
 function password(input) {
  if(input.closest('.password-control'))return;
  const wrap=document.createElement('span');wrap.className='password-control';
  input.before(wrap);wrap.append(input);
  const button=document.createElement('button');button.type='button';button.className='password-toggle';
  function update(){const shown=input.type==='text';button.setAttribute('aria-label',shown?'Ocultar contraseña':'Mostrar contraseña');button.setAttribute('aria-pressed',String(shown));button.innerHTML=icon(shown?paths.off:paths.visibility);}
  button.addEventListener('click',()=>{input.type=input.type==='password'?'text':'password';update();});
  wrap.append(button);update();
 }
 function combo(select) {
  if(select.closest('.subcategory-combo')||select.closest('template'))return;
  const wrapper=document.createElement('div');wrapper.className='subcategory-combo';select.before(wrapper);wrapper.append(select);
  select.classList.add('subcategory-native');select.tabIndex=-1;select.setAttribute('aria-hidden','true');
  const trigger=document.createElement('button');trigger.type='button';trigger.className='subcategory-trigger';trigger.setAttribute('aria-haspopup','listbox');trigger.setAttribute('aria-expanded','false');
  const popup=document.createElement('div');popup.className='subcategory-popup';popup.hidden=true;popup.id='subcategory-'+(++nextId);trigger.setAttribute('aria-controls',popup.id);
  const search=document.createElement('input');search.type='search';search.placeholder='Buscar subcategoría';search.setAttribute('aria-label','Buscar subcategoría');search.autocomplete='off';
  const list=document.createElement('div');list.className='subcategory-options';list.setAttribute('role','listbox');list.setAttribute('aria-label','Subcategorías');
  const validation=document.createElement('p');validation.id=popup.id+'-error';validation.hidden=true;validation.textContent='Seleccioná una subcategoría.';validation.setAttribute('role','alert');
  trigger.setAttribute('aria-describedby',validation.id);
  popup.append(search,list);wrapper.append(trigger,popup,validation);
  function update(){trigger.textContent=(select.selectedOptions[0]?.textContent.trim()||'Seleccionar')+' ▾';}
  function close(focus=false){popup.hidden=true;trigger.setAttribute('aria-expanded','false');if(focus)trigger.focus();}
  function render(){
   list.replaceChildren();for(const option of select.options){if(!option.value||option.disabled||!option.textContent.toLocaleLowerCase().includes(search.value.toLocaleLowerCase()))continue;
    const b=document.createElement('button');b.type='button';b.className='subcategory-option';b.textContent=option.textContent;b.setAttribute('role','option');b.setAttribute('aria-selected',String(select.value===option.value));
    b.addEventListener('click',()=>{select.value=option.value;select.dispatchEvent(new Event('change',{bubbles:true}));update();close(true);});
    list.append(b);
   } if(!list.children.length){const p=document.createElement('p');p.textContent='No hay coincidencias.';p.setAttribute('role','status');list.append(p);}
  }
  function open(){popup.hidden=false;trigger.setAttribute('aria-expanded','true');search.value='';render();search.focus();}
  trigger.addEventListener('click',()=>popup.hidden?open():close());
  trigger.addEventListener('keydown',e=>{if(e.key==='ArrowDown'||e.key==='ArrowUp'){e.preventDefault();open();if(e.key==='ArrowUp')list.querySelector('button:last-child')?.focus();}});
  search.addEventListener('input',render);
  wrapper.addEventListener('keydown',e=>{
   if(e.key==='Escape'){e.preventDefault();close(true);return;}
   const buttons=Array.from(list.querySelectorAll('button'));
   if(e.key==='Enter'&&document.activeElement===search&&!popup.hidden){e.preventDefault();buttons[0]?.click();return;}
   let index=buttons.indexOf(document.activeElement);
   if(['ArrowDown','ArrowUp','Home','End'].includes(e.key)&&!popup.hidden&&buttons.length){e.preventDefault();if(e.key==='Home')index=0;else if(e.key==='End'||(index===-1&&e.key==='ArrowUp'))index=buttons.length-1;else index=Math.max(0,Math.min(buttons.length-1,index+(e.key==='ArrowDown'?1:-1)));buttons[index].focus();}
  });
  select.addEventListener('change',()=>{trigger.removeAttribute('aria-invalid');validation.hidden=true;update();});
  wrapper.addEventListener('focusout',()=>queueMicrotask(()=>{if(!wrapper.contains(document.activeElement))close();}));
  select.addEventListener('invalid',e=>{e.preventDefault();trigger.focus();trigger.setAttribute('aria-invalid','true');validation.hidden=false;open();});
  document.addEventListener('click',e=>{if(!wrapper.contains(e.target))close();});
  const label=select.closest('label'); if(label){trigger.setAttribute('aria-label','Subcategoría');label.addEventListener('click',e=>{if(e.target===label)trigger.focus();});}
  update();
 }
 function init(root=document){root.querySelectorAll('input[type="password"]').forEach(password);root.querySelectorAll('select[name="subCategoriaId"],select[name="bienSubCategoriaId"]').forEach(combo);}
 function start(){
  init();new MutationObserver(()=>init()).observe(document.body,{childList:true,subtree:true});
  document.addEventListener('click',e=>document.querySelectorAll('.account-menu[open]').forEach(m=>{if(!m.contains(e.target))m.open=false;}));
  document.addEventListener('keydown',e=>{if(e.key==='Escape')document.querySelectorAll('.account-menu[open]').forEach(m=>{m.open=false;m.querySelector('summary').focus();});});
 }
 if(document.readyState==='loading')document.addEventListener('DOMContentLoaded',start);else start();
})();

/* Animated single-choice dropdowns keep native fields for submission and validation. */
window.initializeAnimatedSelects = () => {
  let activeClose;
  document.querySelectorAll('select:not([multiple])').forEach((select, serial) => {
    if (select.closest('.animated-select')) return;
    serial = window.animatedSelectSerial = (window.animatedSelectSerial || 0) + 1;
    const wrapper = document.createElement('div');
    wrapper.className = 'animated-select';
    select.before(wrapper);
    wrapper.append(select);
    select.classList.add('animated-select-native');
    select.tabIndex = -1;
    select.setAttribute("aria-hidden", "true");
    const trigger = document.createElement('button');
    trigger.type = 'button';
    trigger.className = 'animated-select-trigger';
    trigger.setAttribute('role', 'combobox');
    trigger.setAttribute('aria-haspopup', 'listbox');
    trigger.setAttribute('aria-required', String(select.required));
    trigger.setAttribute('aria-expanded', 'false');
    const label = select.labels && select.labels[0];
    if (label) {
      label.id ||= 'dropdown-label-' + serial;
      trigger.setAttribute('aria-labelledby', label.id);
      label.addEventListener('click', () => trigger.focus());
    } else trigger.setAttribute('aria-label', select.getAttribute('aria-label') || 'Elegir una opción');
    const panel = document.createElement('div');
    panel.id = 'dropdown-options-' + serial;
    panel.className = 'animated-select-options';
    panel.setAttribute('role', 'listbox');
    panel.inert = true;
    trigger.setAttribute('aria-controls', panel.id);
    wrapper.append(trigger, panel);
    const sync = () => {
      trigger.textContent = select.selectedOptions[0]?.textContent || 'Elegí una opción';
      trigger.disabled = select.disabled;
    };
    const close = () => {
      wrapper.classList.remove('is-open');
      trigger.setAttribute('aria-expanded', 'false');
      panel.inert = true;
      if (activeClose === close) activeClose = null;
    };
    const choose = index => {
      select.selectedIndex = index;
      select.dispatchEvent(new Event('input', { bubbles: true }));
      select.dispatchEvent(new Event('change', { bubbles: true }));
      trigger.removeAttribute('aria-invalid');
      sync(); close(); trigger.focus();
    };
    const open = () => {
      if (activeClose) activeClose();
      sync();
      panel.replaceChildren();
      [...select.options].forEach((option, index) => {
        if (option.hidden) return;
        const item = document.createElement('button');
        item.type = 'button';
        item.className = 'animated-select-option';
        item.setAttribute('role', 'option');
        item.setAttribute('aria-selected', String(option.selected));
        item.disabled = option.disabled || option.parentElement.disabled;
        item.textContent = option.textContent;
        item.addEventListener('click', () => choose(index));
        panel.append(item);
      });
      wrapper.classList.add('is-open');
      trigger.setAttribute('aria-expanded', 'true');
      panel.inert = false;
      activeClose = close;
    };
    trigger.addEventListener('click', () => wrapper.classList.contains('is-open') ? close() : open());
    wrapper.addEventListener('keydown', event => {
      if (event.key === 'Escape') { close(); trigger.focus(); event.preventDefault(); return; }
      if (!['ArrowDown', 'ArrowUp', 'Home', 'End'].includes(event.key)) return;
      event.preventDefault();
      if (!wrapper.classList.contains('is-open')) open();
      const options = [...panel.querySelectorAll('button:not(:disabled)')];
      let index = options.indexOf(document.activeElement);
      index = event.key === 'Home' ? 0 : event.key === 'End' ? options.length - 1
        : (index + (event.key === 'ArrowDown' ? 1 : -1) + options.length) % options.length;
      options[index]?.focus();
    });
    wrapper.addEventListener('focusout', event => { if (!wrapper.contains(event.relatedTarget)) close(); });
    document.addEventListener('pointerdown', event => { if (!wrapper.contains(event.target)) close(); });
    select.addEventListener('change', sync);
    select.addEventListener('invalid', event => {
      event.preventDefault();
      trigger.setAttribute('aria-invalid', 'true');
      trigger.focus();
      open();
    });
    select.form?.addEventListener('reset', () => setTimeout(sync, 0));
    new MutationObserver(sync).observe(select, { attributes: true, childList: true, subtree: true });
    sync();
  });
};
window.initializeAnimatedSelects();
