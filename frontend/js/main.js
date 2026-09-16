loadUsers();
loadProducts();
connectSSE();

document.addEventListener('DOMContentLoaded', () => {
  document.getElementById('userSelect').addEventListener('change', e => {
    loadOrders(e.target.value);
  });
});
