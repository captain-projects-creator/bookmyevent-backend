// my_tickets.js (uses JWT stored in localStorage)
const API_BASE = '/api';

document.addEventListener('DOMContentLoaded', () => {
  loadMyTickets();
});

function getAuthHeaders() {
  // prefer 'token' but fall back to 'jwt' for backwards compatibility
  const token = localStorage.getItem('token') || localStorage.getItem('jwt');
  return token ? { 'Authorization': 'Bearer ' + token } : {};
}

async function loadMyTickets() {
  const list = document.getElementById('ticketsList');
  const noTickets = document.getElementById('noTickets');
  list.innerHTML = 'Loading...';

  try {
    const res = await fetch(`${API_BASE}/bookings/me`, { headers: getAuthHeaders() });
    if (res.status === 401) {
      // not logged in, redirect to login
      window.location.href = 'login.html';
      return;
    }
    if (!res.ok) throw new Error('Failed to load bookings');
    const bookings = await res.json();

    if (!Array.isArray(bookings) || bookings.length === 0) {
      list.innerHTML = '';
      if (noTickets) noTickets.style.display = 'block';
      return;
    }

    if (noTickets) noTickets.style.display = 'none';
    list.innerHTML = '';

    bookings.forEach(b => {
      const ev = b.event || {};
      const user = b.user || {};
      const card = document.createElement('div');
      card.className = 'ticket-card';

      const info = document.createElement('div');
      info.className = 'ticket-info';
      info.innerHTML = `
        <div class="ticket-title">${escapeHtml(ev.title || 'Event')}</div>
        <div class="ticket-meta">Date: ${escapeHtml(ev.date || '—')} • Capacity: ${ev.capacity || '—'}</div>
        <div class="ticket-meta">Booked by: ${escapeHtml(user.username || ('id:' + (user.id || 'N/A')))} • Booking ID: ${b.id}</div>
      `;

      const actions = document.createElement('div');
      actions.className = 'ticket-actions';

      const qrImg = document.createElement('img');
      qrImg.className = 'qr-preview';
      qrImg.alt = 'QR code';
      qrImg.src = `${API_BASE}/bookings/${b.id}/qrcode`;

      const downloadBtn = document.createElement('a');
      downloadBtn.className = 'btn small';
      downloadBtn.textContent = 'Download QR';
      downloadBtn.href = `${API_BASE}/bookings/${b.id}/qrcode`;
      downloadBtn.target = '_blank';

      const cancelBtn = document.createElement('button');
      cancelBtn.className = 'btn outline small';
      cancelBtn.textContent = 'Cancel Booking';
      cancelBtn.onclick = async () => {
        if (!confirm('Cancel this booking?')) return;
        try {
          const r = await fetch(`${API_BASE}/bookings/${b.id}`, { method: 'DELETE', headers: getAuthHeaders() });
          if (r.ok) {
            alert('Booking cancelled.');
            loadMyTickets();
          } else {
            const txt = await r.text();
            alert('Failed to cancel: ' + txt);
          }
        } catch (err) {
          console.error(err);
          alert('Cancel failed');
        }
      };

      actions.appendChild(qrImg);
      actions.appendChild(downloadBtn);
      actions.appendChild(cancelBtn);

      card.appendChild(info);
      card.appendChild(actions);

      list.appendChild(card);
    });

  } catch (err) {
    console.error(err);
    list.innerHTML = `<div class="muted">Failed to load bookings: ${err.message}</div>`;
  }
}

function escapeHtml(s) {
  if (!s) return '';
  return s.toString().replace(/[&<>"']/g, c => ({ '&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
}