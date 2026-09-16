function pushNotification(data) {
  const container  = document.getElementById('notifications');
  const placeholder = container.querySelector('.notif-placeholder');
  if (placeholder) placeholder.remove();

  const status = (data.status || '').toLowerCase();
  const order  = data.order || {};

  if (status === 'success' && order.productId) {
    const product = products.find(p => p.id === order.productId);
    if (product) {
      product.stock = Math.max(0, product.stock - (order.quantity || 1));
      const stockEl = document.getElementById(`stock-${product.id}`);
      if (stockEl) {
        stockEl.textContent = `Stock: ${product.stock}`;
        stockEl.style.color = product.stock === 0 ? '#f85149' : '';
      }
      const qtyInput = document.getElementById(`qty-${product.id}`);
      if (qtyInput) qtyInput.max = product.stock;
      if (product.stock === 0) {
        const btn = document.querySelector(`#card-${product.id} .order-btn`);
        if (btn) { btn.disabled = true; btn.textContent = 'Out of stock'; }
      }
    }
  }

  const productName = products.find(p => p.id === order.productId)?.name || order.productId;
  const userName    = users.find(u => u.id === order.userId)?.name         || order.userId;
  const quantity    = order.quantity || 1;

  const item = document.createElement('div');
  item.className = `notif-item ${status}`;
  item.innerHTML = `
    <div class="notif-header">
      <span class="notif-badge ${status}">${esc(data.status)}</span>
      <span class="notif-time">${new Date().toLocaleTimeString()}</span>
    </div>
    <div class="notif-body">
      <strong>${esc(userName)}</strong> — ${esc(productName)}<br/>
      Qty: <strong>${quantity}</strong> &nbsp;·&nbsp; Amount: <strong>$${Number(order.amount || 0).toFixed(2)}</strong><br/>
      <span style="color:#8b949e;font-size:0.7rem">Payment ID: ${esc(data.paymentUuid)}</span>
    </div>
  `;
  container.prepend(item);
}

function clearNotifs() {
  document.getElementById('notifications').innerHTML = '<p class="notif-placeholder">Waiting for events…</p>';
}
