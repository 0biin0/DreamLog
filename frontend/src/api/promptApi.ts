import client from './client';
import type { ApiResponse } from '../types/auth';

export interface PromptResponse {
  id: number | null;
  category: string;
  content: string;
}

export interface AiPromptRequest {
  goal?: string;
  mood?: string;
}

export const promptApi = {
  getRandom: (category?: string) =>
    client.get<ApiResponse<PromptResponse>>('/api/prompts/random', {
      params: category ? { category } : {},
    }),

  getAi: (data: AiPromptRequest) =>
    client.post<ApiResponse<{ prompt: string }>>('/api/prompts/ai', data),
};
