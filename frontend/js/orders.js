async function placeOrder(productId, unitPrice) {
  const userId = document.getElementById('userSelect').value;
  if (!userId) { alert('Select a user first.'); return; }

  const quantity = parseInt(document.getElementById(`qty-${productId}`).value, 10) || 1;
  const amount   = +(unitPrice * quantity).toFixed(2);

  const btn   = document.querySelector(`#card-${productId} .order-btn`);
  const flash = document.getElementById(`flash-${productId}`);
  btn.disabled = true;
  flash.textContent = '';

  const payload = { uuid: crypto.randomUUID(), userId, productId, quantity, amount };

  try {
    const res = await fetch(`${API}/orders`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    if (res.ok) {
      showFlash(flash, 'Order sent!', 'ok');
      prependOrderRow({ id: payload.uuid, productId, quantity, amount, status: 'PENDING', createdAt: new Date().toISOString() });
    } else {
      showFlash(flash, `Error ${res.status}`, 'err');
    }
  } catch {
    showFlash(flash, 'Network error', 'err');
  } finally {
    setTimeout(() => { btn.disabled = false; }, 2000);
  }
}

function showFlash(el, msg, cls) {
  el.textContent = msg;
  el.className   = `flash ${cls}`;
  setTimeout(() => { el.textContent = ''; el.className = 'flash'; }, 4000);
}

async function loadOrders(userId) {
  const list = document.getElementById('orderList');
  if (!userId) {
    list.innerHTML = '<p class="empty-state" id="orderPlaceholder">Select a user to see their orders.</p>';
    return;
  }
  try {
    const res = await fetch(`${API}/orders?userId=${userId}`);
    const orders = await res.json();
    list.innerHTML = '';
    if (!orders.length) {
      list.innerHTML = '<p class="empty-state" id="orderPlaceholder">No orders yet.</p>';
      return;
    }
    orders
      .sort((a, b) => new Date(a.createdAt) - new Date(b.createdAt))
      .forEach(o => prependOrderRow(o));
  } catch {
    list.innerHTML = '<p class="empty-state">Failed to load orders.</p>';
  }
}

function prependOrderRow(order) {
  const list = document.getElementById('orderList');
  const placeholder = document.getElementById('orderPlaceholder');
  if (placeholder) placeholder.remove();

  const product     = products.find(p => p.id === order.productId);
  const productName = product?.name || order.productId;
  const status      = (order.status || 'PENDING').toLowerCase();
  const time        = new Date(order.createdAt).toLocaleTimeString();

  const row = document.createElement('div');
  row.className = 'order-row';
  row.dataset.orderId = order.id;
  row.innerHTML = `
    <div class="order-row-info">
      <strong>${esc(productName)}</strong><br/>
      Qty: ${order.quantity} &nbsp;·&nbsp; $${Number(order.amount).toFixed(2)}
    </div>
    <div class="order-row-meta">${time}</div>
    <span class="order-status-badge ${status}" id="badge-${esc(order.id)}">${esc(order.status || 'PENDING')}</span>
  `;
  list.prepend(row);
}

function patchOrderStatus(orderId, paymentStatus) {
  if (!orderId || !paymentStatus) return;
  const badge = document.getElementById(`badge-${orderId}`);
  if (!badge) return;
  const orderStatus = paymentStatus === 'SUCCESS' ? 'PAID' : paymentStatus;
  badge.className = `order-status-badge ${orderStatus.toLowerCase()}`;
  badge.textContent = orderStatus;
}
