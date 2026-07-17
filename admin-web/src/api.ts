export interface Overview {
  totalActivities: number;
  pendingReports: number;
  generatedAt: string;
}

export interface ActivityRow {
  id: number;
  title: string;
  type: string;
  cityName?: string;
  startAt: string;
  joinedCount: number;
  capacity: number;
  status: string;
}

export interface ReportRow {
  id: number;
  targetType: string;
  targetId: number;
  reasonCode: string;
  description?: string;
  status: string;
  createdAt: string;
}

interface Page<T> { content: T[] }

async function api<T>(path: string, options: RequestInit = {}): Promise<T> {
  const token = localStorage.getItem('one_admin_token');
  const response = await fetch(`/api${path}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...options.headers
    }
  });
  if (!response.ok) throw new Error(response.status === 401 ? '登录已失效' : '请求失败');
  return response.json() as Promise<T>;
}

export async function adminLogin(username: string, password: string): Promise<void> {
  const result = await api<{ token: string }>('/auth/admin', {
    method: 'POST', body: JSON.stringify({ username, password })
  });
  localStorage.setItem('one_admin_token', result.token);
}

export const loadOverview = () => api<Overview>('/admin/overview');
export const loadActivities = () => api<Page<ActivityRow>>('/admin/activities?size=10').then((page) => page.content);
export const loadReports = () => api<Page<ReportRow>>('/admin/reports?status=PENDING&size=10').then((page) => page.content);
export const resolveReport = (id: number) => api<ReportRow>(`/admin/reports/${id}/resolve`, { method: 'POST' });
