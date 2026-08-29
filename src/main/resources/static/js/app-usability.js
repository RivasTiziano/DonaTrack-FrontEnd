/**
 * DonaTrack Usability & Heuristics Script
 * Implements Toast notifications, Accessible Modals, Asynchronous Skeleton/Loaders,
 * Mobile Navigation Drawer, Tooltip handlers, and Interactive UI Feedback.
 */

(function() {
  'use strict';

  // --- 1. TOAST NOTIFICATION SYSTEM (Heurística #1 y #9) ---
  window.showToast = function(title, message, type = 'success') {
    let container = document.getElementById('toast-container');
    if (!container) {
      container = document.createElement('div');
      container.id = 'toast-container';
      container.setAttribute('aria-live', 'polite');
      container.setAttribute('aria-atomic', 'true');
      document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.setAttribute('role', 'alert');

    let iconName = 'check-circle';
    let iconColor = '#10b981';
    if (type === 'error') {
      iconName = 'alert-circle';
      iconColor = '#ef4444';
    } else if (type === 'warning') {
      iconName = 'alert-triangle';
      iconColor = '#f59e0b';
    } else if (type === 'info') {
      iconName = 'info';
      iconColor = '#3b82f6';
    }

    toast.innerHTML = `
      <div class="toast-icon">
        <i data-lucide="${iconName}" style="width: 20px; height: 20px; color: ${iconColor};"></i>
      </div>
      <div class="toast-body">
        <div class="toast-title">${title}</div>
        <div class="toast-message">${message}</div>
      </div>
      <button type="button" class="toast-close" aria-label="Cerrar notificación">
        <i data-lucide="x" style="width: 16px; height: 16px;"></i>
      </button>
    `;

    container.appendChild(toast);
    if (window.lucide) {
      window.lucide.createIcons();
    }

    const closeBtn = toast.querySelector('.toast-close');
    const removeToast = () => {
      toast.style.opacity = '0';
      toast.style.transform = 'translateY(10px)';
      setTimeout(() => toast.remove(), 300);
    };

    closeBtn.addEventListener('click', removeToast);
    setTimeout(removeToast, 4500);
  };

  // --- 2. ACCESSIBLE MODAL SYSTEM (Heurística #3 y #5) ---
  window.openModal = function(modalId) {
    const modal = document.getElementById(modalId);
    if (!modal) return;
    modal.classList.add('active');
    modal.setAttribute('aria-hidden', 'false');
    document.body.style.overflow = 'hidden';

    // Focus first focusable element
    const focusable = modal.querySelectorAll('button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])');
    if (focusable.length) {
      focusable[0].focus();
    }

    if (window.lucide) {
      window.lucide.createIcons();
    }
  };

  window.closeModal = function(modalId) {
    const modal = document.getElementById(modalId);
    if (!modal) return;
    modal.classList.remove('active');
    modal.setAttribute('aria-hidden', 'true');
    document.body.style.overflow = '';
  };

  // Global keydown for Escape key closing modals
  document.addEventListener('keydown', function(e) {
    if (e.key === 'Escape') {
      const activeModal = document.querySelector('.dt-modal.active');
      if (activeModal) {
        closeModal(activeModal.id);
      }
    }
  });

  // --- 3. ASYNCHRONOUS LOADERS & SIMULATIONS (Heurística #1 - Indicadores >300ms) ---
  window.simulateAsyncAction = function(targetBtn, delayMs = 600, onComplete) {
    if (!targetBtn) return;
    const originalContent = targetBtn.innerHTML;
    targetBtn.disabled = true;
    targetBtn.innerHTML = `
      <span class="dt-spinner" style="width: 16px; height: 16px; border-width: 2px; margin-right: 6px;"></span>
      <span>Procesando...</span>
    `;

    setTimeout(() => {
      targetBtn.disabled = false;
      targetBtn.innerHTML = originalContent;
      if (window.lucide) window.lucide.createIcons();
      if (typeof onComplete === 'function') {
        onComplete();
      }
    }, delayMs);
  };

  // --- 4. INITIALIZATION & EVENT DELEGATION ---
  document.addEventListener('DOMContentLoaded', function() {
    // Render Lucide icons
    if (window.lucide) {
      window.lucide.createIcons();
    }

    // Dismissible onboarding hints
    document.querySelectorAll('.onboarding-hint-close').forEach(btn => {
      btn.addEventListener('click', function() {
        const hint = this.closest('.onboarding-hint');
        if (hint) {
          hint.style.transition = 'all 0.2s ease';
          hint.style.opacity = '0';
          hint.style.transform = 'translateY(-8px)';
          setTimeout(() => hint.remove(), 200);
        }
      });
    });

    // Mobile Header Drawer toggle
    const toggleBtn = document.querySelector('.mobile-menu-toggle');
    const drawer = document.querySelector('.mobile-nav-drawer');
    const backdrop = document.querySelector('.mobile-nav-backdrop');
    const drawerCloseBtn = document.querySelector('.mobile-drawer-close');

    if (toggleBtn && drawer && backdrop) {
      const openDrawer = () => {
        drawer.classList.add('open');
        backdrop.classList.add('open');
        document.body.style.overflow = 'hidden';
      };
      const closeDrawer = () => {
        drawer.classList.remove('open');
        backdrop.classList.remove('open');
        document.body.style.overflow = '';
      };

      toggleBtn.addEventListener('click', openDrawer);
      backdrop.addEventListener('click', closeDrawer);
      if (drawerCloseBtn) drawerCloseBtn.addEventListener('click', closeDrawer);
    }

    // Interactive Tab filtering with subtle skeleton transition
    document.querySelectorAll('.filter-tabs .filter-tab').forEach(tab => {
      tab.addEventListener('click', function() {
        const parent = this.closest('.filter-tabs');
        if (parent) {
          parent.querySelectorAll('.filter-tab').forEach(t => t.classList.remove('active'));
          this.classList.add('active');
          showToast('Filtro aplicado', `Mostrando registros: ${this.textContent.trim()}`, 'info');
        }
      });
    });
  });
})();
