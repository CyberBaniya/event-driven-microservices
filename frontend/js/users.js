async function loadUsers() {
  try {
    const res = await fetch(`${API}/users`);
    users = await res.json();
    const sel = document.getElementById('userSelect');
    sel.innerHTML = users.length
      ? users.map(u => `<option value="${u.id}">${u.name} (${u.email})</option>`).join('')
      : '<option value="">No users found</option>';
    if (users.length) loadOrders(sel.value);
  } catch {
    document.getElementById('userSelect').innerHTML = '<option value="">Failed to load users</option>';
  }
}
