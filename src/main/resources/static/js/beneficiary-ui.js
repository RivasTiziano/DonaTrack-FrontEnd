document.addEventListener('DOMContentLoaded', () => {
  document.querySelectorAll('[data-receipt-image]').forEach(input => {
    const preview = input.form.querySelector('.receipt-image-preview');
    let url;
    input.addEventListener('change', () => {
      if (url) URL.revokeObjectURL(url);
      preview.hidden = true;
      preview.removeAttribute('src');
      const file = input.files[0];
      input.setCustomValidity('');
      if (!file) return;
      if (!['image/jpeg','image/png'].includes(file.type) || file.size > 10 * 1024 * 1024) {
        input.setCustomValidity('Seleccioná una imagen JPG o PNG de hasta 10 MB.');
        input.reportValidity();
        return;
      }
      url = URL.createObjectURL(file);
      preview.src = url;
      preview.hidden = false;
    });
  });
  const rows = document.querySelectorAll('.beneficiary-detail-row');
  if (!rows.length) return;
  const dialog = document.createElement('dialog');
  dialog.className = 'dialog beneficiary-details-dialog';
  dialog.setAttribute('aria-label', 'Detalle del registro');
  const header = document.createElement('div');
  header.className = 'dialog-header';
  const title = document.createElement('h2');
  const close = document.createElement('button');
  close.type = 'button';
  close.className = 'beneficiary-detail-close';
  close.textContent = '×';
  close.setAttribute('aria-label', 'Cerrar detalle');
  close.addEventListener('click', () => dialog.close());
  header.append(title, close);
  const body = document.createElement('div');
  body.className = 'beneficiary-detail-content';
  dialog.append(header, body);
  document.body.append(dialog);
  rows.forEach(row => {
    const open = () => {
      title.textContent = row.querySelector('h3')?.textContent || 'Detalle';
      const content = row.cloneNode(true);
      content.removeAttribute('tabindex');
      content.removeAttribute('role');
      content.classList.remove('beneficiary-detail-row');
      content.classList.remove('need-card', 'donation-assigned-card', 'donation-card');
      content.classList.add('beneficiary-record-detail');
      content.removeAttribute('style');
      content.querySelectorAll('form, h3').forEach(element => element.remove());
      body.replaceChildren(content);
      dialog.showModal();
    };
    row.addEventListener('click', event => {
      if (event.target.closest('a,button,form,input,select,textarea,summary')) return;
      if (!window.getSelection()?.toString()) open();
    });
    row.addEventListener('keydown', event => {
      if (event.target === row && ['Enter',' '].includes(event.key)) { event.preventDefault(); open(); }
    });
  });
});
