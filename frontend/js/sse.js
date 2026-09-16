function connectSSE() {
  const dot   = document.getElementById('sseDot');
  const label = document.getElementById('sseLabel');

  const es = new EventSource(SSE_URL);

  es.onopen = () => {
    dot.className     = 'sse-dot connected';
    label.textContent = 'Live';
  };

  es.onerror = () => {
    dot.className     = 'sse-dot error';
    label.textContent = 'Reconnecting…';
  };

  es.addEventListener('payment-update', e => {
    const data = JSON.parse(e.data);
    pushNotification(data);
    patchOrderStatus(data.order?.orderUuid, data.status);
  });
}
