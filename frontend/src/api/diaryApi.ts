import client from './client';
import type { ApiResponse } from '../types/auth';
import type { DiaryCreateRequest, DiaryUpdateRequest, DiaryResponse, DiaryPage } from '../types/diary';

export const diaryApi = {
  create: (data: DiaryCreateRequest) =>
    client.post<ApiResponse<DiaryResponse>>('/api/diaries', data),

  getList: (page = 0, size = 10) =>
    client.get<ApiResponse<DiaryPage>>(`/api/diaries?page=${page}&size=${size}`),

  getOne: (id: number) =>
    client.get<ApiResponse<DiaryResponse>>(`/api/diaries/${id}`),

  update: (id: number, data: DiaryUpdateRequest) =>
    client.patch<ApiResponse<DiaryResponse>>(`/api/diaries/${id}`, data),

  delete: (id: number) =>
    client.delete<ApiResponse<void>>(`/api/diaries/${id}`),

  getArrived: () =>
    client.get<ApiResponse<DiaryResponse[]>>('/api/diaries/arrived'),
};
