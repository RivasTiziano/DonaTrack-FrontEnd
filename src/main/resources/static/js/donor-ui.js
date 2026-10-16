document.addEventListener('DOMContentLoaded', () => {
  document.querySelectorAll('form[data-auto-filters]').forEach(form => {
    const category = form.elements.categoriaId;
    const subcategory = form.elements.subcategoriaId;
    const syncCategories = () => {
      if (!category.value && subcategory.value) category.value = subcategory.selectedOptions[0]?.dataset.categoryId || '';
      [...subcategory.options].forEach(option => {
        const excluded = !!option.value && !!category.value && option.dataset.categoryId !== category.value;
        option.hidden = excluded;
        option.disabled = excluded;
      });
    };
    syncCategories();
    form.addEventListener('change', event => {
      if (!event.target.matches('select')) return;
      if (event.target === category && subcategory.selectedOptions[0]?.dataset.categoryId !== category.value) subcategory.value = '';
      syncCategories();
      form.requestSubmit();
    });
  });
  document.querySelectorAll('[data-donor-category]').forEach(category => {
    const select = () => {
      const tab = [...document.querySelectorAll('.mission-tab-btn')].find(button => button.dataset.categoria === category.dataset.donorCategory);
      window.filterMissionsByCategory(category.dataset.donorCategory, tab);
    };
    category.addEventListener('click', select);
    category.addEventListener('keydown', event => {
      if (['Enter', ' '].includes(event.key)) { event.preventDefault(); select(); }
    });
  });
  document.querySelectorAll('[data-donor-detail]').forEach(row => {
    const open = () => {
      if (row.dataset.donorDetail === 'openDonationDetailModal') window.openDonationDetailModal(Number(row.dataset.id));
      if (row.dataset.donorDetail === 'openEntityProfileModal') window.openEntityProfileModal(row.dataset.id);
    };
    row.addEventListener('click', event => {
      if (event.target.closest('a,button,input,select,textarea,form,summary,details')) return;
      if (!window.getSelection()?.toString()) open();
    });
    row.addEventListener('keydown', event => {
      if (event.target === row && ['Enter', ' '].includes(event.key)) { event.preventDefault(); open(); }
    });

  });
});
