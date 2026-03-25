import client from './client';
import type { ApiResponse } from '../types/auth';
import type { ChallengeCreateRequest, ChallengeResponse, ParticipantProgress } from '../types/challenge';

export const challengeApi = {
  create: (data: ChallengeCreateRequest) =>
    client.post<ApiResponse<ChallengeResponse>>('/api/challenges', data),

  getJoinable: () =>
    client.get<ApiResponse<ChallengeResponse[]>>('/api/challenges'),

  getOne: (id: number) =>
    client.get<ApiResponse<ChallengeResponse>>(`/api/challenges/${id}`),

  join: (id: number) =>
    client.post<ApiResponse<void>>(`/api/challenges/${id}/join`),

  getProgress: (id: number) =>
    client.get<ApiResponse<ParticipantProgress>>(`/api/challenges/${id}/progress`),

  getMy: () =>
    client.get<ApiResponse<ChallengeResponse[]>>('/api/challenges/my'),
};
