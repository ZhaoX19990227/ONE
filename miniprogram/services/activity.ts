import { USE_MOCK } from '../config/index';
import { mockActivities } from '../mock/activities';
import { Activity } from '../models/activity';
import { request } from '../utils/request';

const wait = (ms = 180) => new Promise<void>((resolve) => setTimeout(resolve, ms));

export async function listActivities(): Promise<Activity[]> {
  if (USE_MOCK) {
    await wait();
    return mockActivities.map((item) => ({ ...item }));
  }
  const page = await request<{ content: RawActivity[] }>({ url: '/activities', method: 'GET' });
  return page.content.map(adaptActivity);
}

export async function getActivity(id: number): Promise<Activity> {
  if (USE_MOCK) {
    await wait();
    const activity = mockActivities.find((item) => item.id === id);
    if (!activity) throw new Error('活动不存在');
    return { ...activity };
  }
  return request<RawActivity>({ url: `/activities/${id}`, method: 'GET' }).then(adaptActivity);
}

interface RawActivity {
  id: number;
  type: Activity['type'];
  mode: Activity['mode'];
  title: string;
  description?: string;
  cityName?: string;
  district?: string;
  address?: string;
  startAt: string;
  capacity: number;
  joinedCount: number;
  feeFen: number;
  depositFen: number;
  tags?: string | string[];
  attributes?: string | Record<string, string | string[]>;
}

function adaptActivity(raw: RawActivity): Activity {
  const themeMap: Record<string, Activity['theme']> = {
    BADMINTON: 'blush', BOARD_GAME: 'dusk', GAMING: 'mist', CITY_WALK: 'sage'
  };
  return {
    id: raw.id,
    type: raw.type,
    mode: raw.mode,
    title: raw.title,
    description: raw.description || '发起人正在补充这场活动的细节。',
    cityName: raw.cityName || (raw.mode === 'ONLINE' ? '线上' : ''),
    district: raw.district || (raw.mode === 'ONLINE' ? '线上' : '报名后可见'),
    address: raw.address,
    startAt: raw.startAt,
    displayTime: formatDisplayTime(raw.startAt),
    capacity: raw.capacity,
    joinedCount: raw.joinedCount,
    feeFen: raw.feeFen,
    depositFen: raw.depositFen,
    tags: parseJson<string[]>(raw.tags, []),
    attributes: parseJson<Record<string, string | string[]>>(raw.attributes, {}),
    theme: themeMap[raw.type] || 'blush',
    badge: raw.mode === 'ONLINE' ? '🎮 线上' : raw.joinedCount + 1 >= raw.capacity ? '🔥 即将满员' : undefined,
    host: { id: 0, nickname: 'ONE 发起人', avatar: 'O', completedCount: 0 },
    participants: []
  };
}

function parseJson<T>(value: string | T | undefined, fallback: T): T {
  if (value === undefined || value === null) return fallback;
  if (typeof value !== 'string') return value;
  try { return JSON.parse(value) as T; } catch { return fallback; }
}

function formatDisplayTime(iso: string): string {
  const date = new Date(iso);
  const week = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'][date.getDay()];
  return `${week} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
}

export async function joinActivity(id: number): Promise<void> {
  if (USE_MOCK) {
    await wait(360);
    const activity = mockActivities.find((item) => item.id === id);
    if (activity && !activity.isJoined) {
      activity.isJoined = true;
      activity.joinedCount += 1;
    }
    return;
  }
  await request({ url: `/activities/${id}/enrollments`, method: 'POST', data: {} });
}

export async function publishActivity(payload: Record<string, unknown>): Promise<void> {
  if (USE_MOCK) {
    await wait(420);
    return;
  }
  await request({ url: '/activities', method: 'POST', data: payload });
}
