const API_BASE = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8081').replace(/\/$/, '');

export class ApiError extends Error {
  constructor(message, status) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
  }
}

export async function api(path, options = {}) {
  const token = localStorage.getItem('cashff_token');
  const headers = { ...(options.headers || {}) };
  if (token) headers.Authorization = `Bearer ${token}`;
  const isForm = options.body instanceof FormData;
  if (options.body != null && !isForm && typeof options.body !== 'string') {
    headers['Content-Type'] = 'application/json';
    options = { ...options, body: JSON.stringify(options.body) };
  }
  const response = await fetch(`${API_BASE}${path}`, { ...options, headers });
  const raw = await response.text();
  let data = null;
  try { data = raw ? JSON.parse(raw) : null; } catch { data = raw; }
  if (!response.ok) {
    if (response.status === 401 && token && !path.startsWith('/api/auth/')) {
      localStorage.removeItem('cashff_token');
      localStorage.removeItem('cashff_username');
      window.dispatchEvent(new Event('cashff-auth-expired'));
    }
    const message = typeof data === 'string'
      ? data.trim()
      : data?.message || data?.error || data?.detail || data?.title;
    throw new ApiError(message || `Request failed with HTTP ${response.status}.`, response.status);
  }
  return data;
}

export const uploadUrl = (url) => {
  if (!url) return '';
  if (/^https?:\/\//i.test(url)) return url;
  return `${API_BASE}${url.startsWith('/') ? '' : '/'}${url}`;
};
