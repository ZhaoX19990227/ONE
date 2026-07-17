export type ActivityType = 'BADMINTON' | 'BOARD_GAME' | 'GAMING' | 'CITY_WALK' | 'COFFEE' | 'OTHER';
export type ActivityMode = 'ONLINE' | 'OFFLINE';

export interface Host {
  id: number;
  nickname: string;
  avatar: string;
  completedCount: number;
}

export interface Activity {
  id: number;
  type: ActivityType;
  mode: ActivityMode;
  title: string;
  description: string;
  cityName: string;
  district: string;
  address?: string;
  displayTime: string;
  startAt: string;
  capacity: number;
  joinedCount: number;
  feeFen: number;
  depositFen: number;
  tags: string[];
  attributes: Record<string, string | string[]>;
  theme: 'blush' | 'mist' | 'dusk' | 'sage';
  badge?: string;
  host: Host;
  participants: string[];
  isJoined?: boolean;
}
