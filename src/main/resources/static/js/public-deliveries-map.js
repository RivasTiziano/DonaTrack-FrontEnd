(() => {
    const container = document.getElementById('public-deliveries-map');
    const status = document.getElementById('public-map-status');
    if (!container) return;
    if (!window.L) { status.textContent = 'No pudimos cargar el mapa. Probá recargar la página.'; return; }
    const map = L.map(container, { scrollWheelZoom: false }).setView([-34.6037, -58.3816], 11);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>',
        maxZoom: 19
    }).addTo(map);
    const entries = [...document.querySelectorAll('#public-deliveries-data article')];
    const points = [];
    entries.forEach(entry => {
        if (!entry.dataset.lat || !entry.dataset.lng) return;
        const lat = Number(entry.dataset.lat), lng = Number(entry.dataset.lng);
        if (!Number.isFinite(lat) || !Number.isFinite(lng) || Math.abs(lat) > 90 || Math.abs(lng) > 180) return;
        const details = entry.cloneNode(true);
        details.className = 'public-delivery-popup';
        const marker = L.marker([lat, lng], { title: 'Ver detalles de la donación entregada' }).addTo(map);
        marker.bindPopup(details, { maxWidth: 320 });
        points.push([lat, lng]);
    });
    if (points.length) map.fitBounds(points, { padding: [40, 40], maxZoom: 13 });
    status.textContent = points.length
        ? ''
        : 'Todavía no hay entregas publicadas con ubicación disponible.';
    new ResizeObserver(() => map.invalidateSize()).observe(container);
})();