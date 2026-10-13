document.querySelectorAll('[data-gallery-carousel]').forEach((carousel) => {
  const viewport = carousel.querySelector('.gallery-viewport');
  const track = carousel.querySelector('.gallery-track');
  const count = track.children.length;
  let moving = false, timer, finishTimer, finishMove;
  function updateVisibility() {
    [...track.children].forEach((photo, index) => {
      photo.inert = index >= 3;
      photo.setAttribute('aria-hidden', String(index >= 3));
      photo.tabIndex = index < 3 && count > 3 ? 0 : -1;
    });
  }
  function move(direction = 1) {
    if (moving || count <= 3) return;
    moving = true;
    const photos = [...track.children];
    const step = photos[1].offsetLeft - photos[0].offsetLeft;
    if (track.contains(document.activeElement)) viewport.focus({ preventScroll: true });
    if (direction < 0) {
      track.classList.add('is-resetting');
      track.prepend(track.lastElementChild);
      track.style.transform = `translateX(${-step}px)`;
      updateVisibility();
      void track.offsetWidth;
      track.classList.remove('is-resetting');
      void track.offsetWidth;
    }
    finishMove = () => {
      if (!moving) return;
      clearTimeout(finishTimer);
      track.classList.add('is-resetting');
      if (direction > 0) track.append(track.firstElementChild);
      track.style.transform = 'translateX(0px)';
      updateVisibility();
      void track.offsetWidth;
      track.classList.remove('is-resetting');
      moving = false;
    };
    track.style.transform = `translateX(${direction > 0 ? -step : 0}px)`;
    finishTimer = setTimeout(finishMove, 750);
  }
  function startTimer() {
    clearInterval(timer);
    if (count > 3 && !document.hidden) timer = setInterval(() => move(), 5000);
  }
  track.addEventListener('transitionend', (event) => {
    if (event.target === track && event.propertyName === 'transform') finishMove?.();
  });
  track.addEventListener('click', (event) => {
    if (!event.target.closest('.gallery-item')) return;
    move(); startTimer();
  });
  viewport.addEventListener('keydown', (event) => {
    if (!['ArrowLeft', 'ArrowRight', 'Enter', ' '].includes(event.key)) return;
    event.preventDefault(); move(event.key === 'ArrowLeft' ? -1 : 1); startTimer();
  });
  document.addEventListener('visibilitychange', startTimer);
  new ResizeObserver(() => { if (moving) finishMove?.(); }).observe(viewport);
  carousel.classList.add('is-ready');
  updateVisibility(); startTimer();
});
