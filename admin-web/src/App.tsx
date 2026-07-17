import { useEffect, useState, type ReactNode } from 'react';
import { Activity, AlertTriangle, CheckCircle2, LayoutDashboard, LogOut, Radio, Search, ShieldCheck, Users } from 'lucide-react';
import { ActivityRow, adminLogin, loadActivities, loadOverview, loadReports, Overview, ReportRow, resolveReport } from './api';

type View = 'overview' | 'activities' | 'reports';

export function App() {
  const [token, setToken] = useState(() => localStorage.getItem('one_admin_token'));
  const [view, setView] = useState<View>('overview');
  const [overview, setOverview] = useState<Overview | null>(null);
  const [activities, setActivities] = useState<ActivityRow[]>([]);
  const [reports, setReports] = useState<ReportRow[]>([]);
  const [error, setError] = useState('');

  const reload = async () => {
    try {
      const [nextOverview, nextActivities, nextReports] = await Promise.all([
        loadOverview(), loadActivities(), loadReports()
      ]);
      setOverview(nextOverview);
      setActivities(nextActivities);
      setReports(nextReports);
      setError('');
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : '加载失败');
    }
  };

  useEffect(() => { if (token) void reload(); }, [token]);

  if (!token) return <Login onSuccess={() => setToken(localStorage.getItem('one_admin_token'))} />;

  const logout = () => {
    localStorage.removeItem('one_admin_token');
    setToken(null);
  };

  const handleResolve = async (id: number) => {
    await resolveReport(id);
    await reload();
  };

  return (
    <div className="admin-shell">
      <aside className="sidebar">
        <div className="brand"><span className="brand-rings"><i /><i /></span><div><b>ONE</b><small>运营工作台</small></div></div>
        <nav>
          <Nav active={view === 'overview'} icon={<LayoutDashboard />} onClick={() => setView('overview')}>概览</Nav>
          <Nav active={view === 'activities'} icon={<Activity />} onClick={() => setView('activities')}>活动管理</Nav>
          <Nav active={view === 'reports'} icon={<ShieldCheck />} count={reports.length} onClick={() => setView('reports')}>安全与举报</Nav>
        </nav>
        <div className="sidebar-note"><Radio /><span>系统信号正常<br /><small>低成本单机模式</small></span></div>
        <button className="logout" onClick={logout}><LogOut />退出</button>
      </aside>

      <main>
        <header><div><span className="eyebrow">ONE OPERATIONS</span><h1>{view === 'overview' ? '今天也让相遇安全发生' : view === 'activities' ? '活动管理' : '安全与举报'}</h1></div><div className="admin-user"><span>O</span><div>ONE 运营<small>管理员</small></div></div></header>
        {error && <div className="error-banner">{error} · 请确认后端已启动，并检查管理员凭据与接口地址。</div>}
        {view === 'overview' && <OverviewView overview={overview} activities={activities} reports={reports} setView={setView} />}
        {view === 'activities' && <ActivitiesView activities={activities} />}
        {view === 'reports' && <ReportsView reports={reports} onResolve={handleResolve} />}
      </main>
    </div>
  );
}

function Login({ onSuccess }: { onSuccess: () => void }) {
  const [username, setUsername] = useState(import.meta.env.DEV ? 'one-admin' : '');
  const [password, setPassword] = useState(import.meta.env.DEV ? 'local-admin-password' : '');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const submit = async () => {
    setLoading(true);
    try { await adminLogin(username, password); onSuccess(); } catch { setError('登录失败，请检查管理员账号和后端配置'); }
    finally { setLoading(false); }
  };
  return <div className="login-page"><div className="login-glow pink" /><div className="login-glow blue" /><section className="login-card"><span className="brand-rings large"><i /><i /></span><span className="eyebrow">ONE OPERATIONS</span><h1>让每一场相遇<br />安全地发生。</h1><p>管理员凭据由服务端环境变量配置。生产环境务必替换默认账号和密码，并限制管理端访问来源。</p><label>管理员账号<input value={username} onChange={(event) => setUsername(event.target.value)} /></label><label>管理员密码<input type="password" value={password} onChange={(event) => setPassword(event.target.value)} /></label>{error && <div className="login-error">{error}</div>}<button onClick={submit} disabled={loading}>{loading ? '正在连接…' : '进入工作台'}</button></section></div>;
}

function Nav({ active, icon, count, children, onClick }: { active: boolean; icon: ReactNode; count?: number; children: ReactNode; onClick: () => void }) {
  return <button className={active ? 'nav-item active' : 'nav-item'} onClick={onClick}>{icon}<span>{children}</span>{count ? <em>{count}</em> : null}</button>;
}

function OverviewView({ overview, activities, reports, setView }: { overview: Overview | null; activities: ActivityRow[]; reports: ReportRow[]; setView: (view: View) => void }) {
  return <><section className="metric-grid"><Metric icon={<Activity />} label="全部活动" value={overview?.totalActivities ?? '—'} hint="保持供给密度" tone="pink" /><Metric icon={<Users />} label="本期参与席位" value="—" hint="待接入统计事件" tone="blue" /><Metric icon={<AlertTriangle />} label="待处理举报" value={overview?.pendingReports ?? '—'} hint={reports.length ? '需要及时响应' : '当前很安静'} tone="amber" /></section><section className="content-grid"><Panel title="最近活动" action="查看全部" onAction={() => setView('activities')}><ActivityTable activities={activities.slice(0, 5)} /></Panel><Panel title="安全提醒" action="进入队列" onAction={() => setView('reports')}><div className="safety-summary"><div className="safety-orbit"><ShieldCheck /><i /></div><h3>{reports.length ? `${reports.length} 条内容等待判断` : '没有积压的安全事件'}</h3><p>举报处理应保留证据和审计记录，AI 只能辅助排序，不能直接完成最终封禁。</p></div></Panel></section></>;
}

function ActivitiesView({ activities }: { activities: ActivityRow[] }) { return <Panel title="全部活动" action="导出能力待接入"><div className="table-toolbar"><div className="search"><Search /><input placeholder="搜索标题、城市或发起人" /></div><span>按开始时间排序</span></div><ActivityTable activities={activities} /></Panel>; }

function ReportsView({ reports, onResolve }: { reports: ReportRow[]; onResolve: (id: number) => void }) { return <Panel title="待处理举报" action={`${reports.length} 条`}><div className="report-list">{reports.length === 0 ? <div className="empty"><CheckCircle2 /><h3>队列已经清空</h3><p>新的举报会按照风险等级进入这里。</p></div> : reports.map((report) => <article className="report-card" key={report.id}><div className="report-mark"><AlertTriangle /></div><div><span className="report-meta">{report.targetType} #{report.targetId} · {new Date(report.createdAt).toLocaleString('zh-CN')}</span><h3>{report.reasonCode}</h3><p>{report.description || '用户未补充说明'}</p></div><button onClick={() => onResolve(report.id)}>标记已处理</button></article>)}</div></Panel>; }

function Metric({ icon, label, value, hint, tone }: { icon: ReactNode; label: string; value: string | number; hint: string; tone: string }) { return <article className="metric-card"><span className={`metric-icon ${tone}`}>{icon}</span><div><small>{label}</small><strong>{value}</strong><span>{hint}</span></div></article>; }
function Panel({ title, action, children, onAction }: { title: string; action: string; children: ReactNode; onAction?: () => void }) { return <section className="panel"><div className="panel-head"><h2>{title}</h2><button onClick={onAction}>{action}</button></div>{children}</section>; }
function ActivityTable({ activities }: { activities: ActivityRow[] }) { return <div className="activity-table"><div className="table-row table-head"><span>活动</span><span>时间 / 城市</span><span>报名</span><span>状态</span></div>{activities.map((activity) => <div className="table-row" key={activity.id}><span><i className={`type-dot type-${activity.type.toLowerCase()}`} /><b>{activity.title}</b><small>#{activity.id} · {activity.type}</small></span><span>{new Date(activity.startAt).toLocaleString('zh-CN', { month: 'numeric', day: 'numeric', hour: '2-digit', minute: '2-digit' })}<small>{activity.cityName || '线上'}</small></span><span><b>{activity.joinedCount}</b> / {activity.capacity}</span><span><em className="status-pill">{activity.status}</em></span></div>)}</div>; }
