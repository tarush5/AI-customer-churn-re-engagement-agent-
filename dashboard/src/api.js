const API_BASE = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

const handleResponse = async (res) => {
  if (!res.ok) {
    const text = await res.text().catch(() => 'Unknown error');
    throw new Error(`API Error ${res.status}: ${text}`);
  }
  return res.json();
};

export const api = {
  getCustomers: () =>
    fetch(`${API_BASE}/customers`).then(handleResponse),

  getCustomer: (id) =>
    fetch(`${API_BASE}/customers/${id}`).then(handleResponse),

  ingestCustomers: () =>
    fetch(`${API_BASE}/customers/ingest`, { method: 'POST' }).then(handleResponse),

  analyzeCustomer: (id) =>
    fetch(`${API_BASE}/customers/${id}/analyze`, { method: 'POST' }).then(handleResponse),

  analyzeAll: () =>
    fetch(`${API_BASE}/customers/analyze-all`, { method: 'POST' }).then(handleResponse),

  generateCampaign: (id) =>
    fetch(`${API_BASE}/campaigns/generate/${id}`, { method: 'POST' }).then(handleResponse),

  sendCampaign: (id) =>
    fetch(`${API_BASE}/campaigns/send/${id}`, { method: 'POST' }).then(handleResponse),

  getCampaignLogs: () =>
    fetch(`${API_BASE}/campaigns/logs`).then(handleResponse),

  getStats: () =>
    fetch(`${API_BASE}/campaigns/stats`).then(handleResponse),
};
