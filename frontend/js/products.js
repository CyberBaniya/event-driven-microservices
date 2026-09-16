async function loadProducts() {
  const grid = document.getElementById('productGrid');
  try {
    const res = await fetch(`${API}/products`);
    products = await res.json();
    if (!products.length) {
      grid.innerHTML = '<p class="empty-state">No products found.</p>';
      return;
    }
    grid.innerHTML = products.map(p => `
      <div class="product-card" id="card-${p.id}">
        <div class="product-name">${esc(p.name)}</div>
        <div class="product-price">$${p.price.toFixed(2)}</div>
        <div class="product-stock" id="stock-${p.id}">Stock: ${p.stock}</div>
        <div class="qty-row">
          <label>Qty</label>
          <input class="qty-input" type="number" id="qty-${p.id}" value="1" min="1" max="99" />
        </div>
        <button class="order-btn" onclick="placeOrder('${p.id}', ${p.price})">Order</button>
        <div class="flash" id="flash-${p.id}"></div>
      </div>
    `).join('');
  } catch {
    grid.innerHTML = '<p class="empty-state">Failed to load products.</p>';
  }
}
