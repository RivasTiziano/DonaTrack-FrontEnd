
/* Interacciones de presentación: nunca simula una escritura ni inventa resultados. */
document.addEventListener('DOMContentLoaded', () => {
  const shell=document.querySelector('.admin-shell');
  const sidebar=shell?.querySelector('.dashboard-sidebar');
  const collapse=sidebar?.querySelector('.admin-sidebar-collapse');
  if(collapse) {
    sidebar.querySelectorAll('a.sidebar-link').forEach(link => {
      const label=link.textContent.trim();
      link.setAttribute('aria-label',label);
      link.title=label;
    });
    const setCollapsed=value => {
      shell.classList.toggle('admin-sidebar-collapsed',value);
      collapse.setAttribute('aria-expanded',String(!value));
      collapse.setAttribute('aria-label',value?'Abrir menú lateral':'Cerrar menú lateral');
      try { localStorage.setItem('donatrack-admin-sidebar-collapsed',String(value)); } catch {}
      window.dispatchEvent(new Event('resize'));
    };
    let saved=false;
    try { saved=localStorage.getItem('donatrack-admin-sidebar-collapsed')==='true'; } catch {}
    setCollapsed(saved);
    collapse.addEventListener('click',() => setCollapsed(!shell.classList.contains('admin-sidebar-collapsed')));
  }
  document.querySelectorAll('[data-dialog-open]').forEach(button => button.addEventListener('click', () => {
    const dialog=document.getElementById(button.dataset.dialogOpen);
    if(dialog) {
      if(button.dataset.necesidadId) dialog.querySelector('[name="necesidadId"]').value=button.dataset.necesidadId;
      if(button.dataset.donacionId) dialog.querySelector('[name="donacionId"]').value=button.dataset.donacionId;
      dialog.showModal();
    }
  }));
  document.querySelectorAll('[data-dialog-close]').forEach(button => button.addEventListener('click', () => button.closest('dialog').close()));
  document.querySelector('.dashboard-menu-open')?.addEventListener('click', () => document.querySelector('.dashboard-sidebar-toggle')?.click());
  document.querySelectorAll('[data-admin-message]').forEach(message => {
    window.showToast?.(message.dataset.kind === 'error' ? 'No se pudo completar' : 'Operación confirmada', message.dataset.adminMessage, message.dataset.kind);
  });
  document.querySelectorAll('[data-filter-table]').forEach(control => control.addEventListener('input', () => {
    const table=document.getElementById(control.dataset.filterTable);
    if(!table) return;
    const controls=[...document.querySelectorAll('[data-filter-table="'+control.dataset.filterTable+'"]')];
    const rows=[...table.querySelectorAll('tbody tr[data-record]')];
    rows.forEach(row => {
      row.hidden=!controls.every(c => !c.value || (c.dataset.field ? row.dataset[c.dataset.field] === c.value : row.textContent.toLowerCase().includes(c.value.toLowerCase())));
    });
    const empty=document.querySelector('[data-filter-empty="'+control.dataset.filterTable+'"]');
    if(empty) empty.hidden=rows.some(row=>!row.hidden);
  }));
  document.querySelectorAll('[data-navigate-select]').forEach(select=>select.addEventListener('change',()=>{
    if(select.value.startsWith('/admin/dashboard/')) location.assign(select.value);
  }));
  document.querySelectorAll('[data-filter-list]').forEach(control=>control.addEventListener('input',()=>{
    document.getElementById(control.dataset.filterList)?.querySelectorAll('[data-record]').forEach(row=>{
      row.hidden=!row.textContent.toLowerCase().includes(control.value.toLowerCase());
    });
  }));
  document.querySelectorAll('.admin-shell form').forEach(form => form.addEventListener('submit', event => {
    if(event.defaultPrevented || !form.checkValidity() || form.method.toLowerCase() !== 'post') return;
    form.setAttribute('aria-busy','true');
    setTimeout(() => {
      if(!form.isConnected) return;
      form.querySelectorAll('button[type="submit"]').forEach(button => {
        button.disabled=true;
        const spinner=document.createElement('span');spinner.className='dt-spinner';spinner.setAttribute('aria-hidden','true');
        if(form.dataset.loadingText) { button.replaceChildren(spinner, document.createTextNode(' ' + form.dataset.loadingText)); }
        else { button.prepend(spinner);button.append(' Procesando…'); }
      });
    },300);
  }));
  const add=document.getElementById('btn-add-bien');
  add?.addEventListener('click', () => document.getElementById('bienes-extra-container').appendChild(document.getElementById('template-bien-extra').content.cloneNode(true)));
  document.querySelectorAll('.admin-map').forEach(container => {
    if(!window.L) return;
    const source=document.getElementById(container.dataset.markers);
    const markers=source ? [...source.querySelectorAll('[data-lat]')].filter(m => m.dataset.lat !== '' && m.dataset.lng !== '' && Number.isFinite(Number(m.dataset.lat)) && Number.isFinite(Number(m.dataset.lng))) : [];
    if(!markers.length) { container.hidden=true; return; }
    const map=L.map(container).setView([Number(markers[0].dataset.lat),Number(markers[0].dataset.lng)],12);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',{attribution:'© OpenStreetMap contributors'}).addTo(map);
    const bounds=[];
    const truckIcon=L.icon({
      iconUrl:'/img/camion-mapa.webp?v=4',
      iconSize:[40,40],
      iconAnchor:[20,20],
      popupAnchor:[0,-20],
      className:'truck-map-marker'
    });
    markers.forEach(m => {
      const point=[Number(m.dataset.lat),Number(m.dataset.lng)];
      const popup=document.createElement('div');popup.textContent=m.textContent.trim();
      L.marker(point,{icon:truckIcon,title:"Ver ubicación del camión"}).addTo(map).bindPopup(popup);bounds.push(point);
    });
    map.fitBounds(bounds,{padding:[30,30],maxZoom:14});
  });
  const target=location.hash ? document.getElementById(location.hash.slice(1)) : null;
  if(target?.tagName==='DETAILS') target.open=true;
});

document.addEventListener('DOMContentLoaded', () => {
  document.querySelectorAll('[data-donation-url], [data-row-url]').forEach(row => {
    row.addEventListener('click', event => {
      if (event.target.closest('a,button,input,select,textarea,form,summary')) return;
      if (window.getSelection()?.toString()) return;
      window.location.assign(row.dataset.rowUrl || row.dataset.donationUrl);
    });
    row.addEventListener('keydown', event => {
      if (event.target !== row || !['Enter',' '].includes(event.key)) return;
      event.preventDefault();
      window.location.assign(row.dataset.rowUrl || row.dataset.donationUrl);
    });
  });
});

document.addEventListener('DOMContentLoaded', () => {
 document.querySelectorAll('[data-csv-dropzone]').forEach(zone => {
  const input=zone.querySelector('input[type="file"]');
  const label=zone.querySelector('[data-csv-file-label]');
  let depth=0;
  const update=() => {
   zone.classList.remove('is-dragging');
   zone.classList.toggle('has-file',!!input.files.length);
   label.textContent=input.files.length ? input.files[0].name : 'Seleccioná o arrastrá un archivo CSV';
  };
  zone.addEventListener('dragenter',event => {
   if(!event.dataTransfer?.types.includes('Files')) return;
   event.preventDefault(); depth++; zone.classList.add('is-dragging');
   label.textContent='';
  });
  zone.addEventListener('dragover',event => { event.preventDefault(); if(event.dataTransfer) event.dataTransfer.dropEffect='copy'; });
  zone.addEventListener('dragleave',() => { depth=Math.max(0,depth-1); if(!depth) update(); });
  zone.addEventListener('drop',event => {
   event.preventDefault(); depth=0;
   if(event.dataTransfer?.files.length) {
    const file=event.dataTransfer.files[0];
    if(!file.name.toLowerCase().endsWith('.csv')) {
     update(); label.textContent='Seleccioná un archivo con extensión .csv'; return;
    }
    const transfer=new DataTransfer(); transfer.items.add(file);
    input.files=transfer.files; input.dispatchEvent(new Event('change',{bubbles:true}));
   }
   update();
  });
  input.addEventListener('change',update);
  update();
 });
});
