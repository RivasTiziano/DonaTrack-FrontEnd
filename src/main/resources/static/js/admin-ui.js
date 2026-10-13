
/* Interacciones de presentación: nunca simula una escritura ni inventa resultados. */
document.addEventListener('DOMContentLoaded', () => {
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
        button.prepend(spinner);button.append(' Procesando…');
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
    markers.forEach(m => {
      const point=[Number(m.dataset.lat),Number(m.dataset.lng)];
      const popup=document.createElement('div');popup.textContent=m.textContent.trim();
      L.marker(point).addTo(map).bindPopup(popup);bounds.push(point);
    });
    map.fitBounds(bounds,{padding:[30,30],maxZoom:14});
  });
  const target=location.hash ? document.getElementById(location.hash.slice(1)) : null;
  if(target?.tagName==='DETAILS') target.open=true;
});
