export interface ChallengeCreateRequest {
  title: string;
  description?: string;
  durationDays: number;
  startDate: string;
  maxParticipants?: number;
  failThreshold?: number;
}

export interface ChallengeResponse {
  id: number;
  creatorId: number;
  title: string;
  description: string | null;
  durationDays: number;
  startDate: string;
  endDate: string;
  maxParticipants: number;
  currentParticipants: number;
  failThreshold: number;
  maxMissedDays: number;
  status: 'UPCOMING' | 'ACTIVE' | 'ENDED';
}

export interface DailyLogEntry {
  date: string;
  achieved: boolean;
}

export interface ParticipantProgress {
  participantId: number;
  status: 'ACTIVE' | 'COMPLETED' | 'FAILED';
  missedCount: number;
  maxMissedDays: number;
  totalDays: number;
  achievedDays: number;
  progressPercent: number;
  dailyLogs: DailyLogEntry[];
}
