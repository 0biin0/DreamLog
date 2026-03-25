import client from './client';
import type { ApiResponse, AuthResponse, LoginRequest, SignupRequest } from '../types/auth';

export const authApi = {
  signup: (data: SignupRequest) =>
    client.post<ApiResponse<AuthResponse>>('/api/auth/signup', data),

  login: (data: LoginRequest) =>
    client.post<ApiResponse<AuthResponse>>('/api/auth/login', data),

  refresh: () =>
    client.post<ApiResponse<{ accessToken: string; expiresIn: number }>>('/api/auth/refresh'),

  logout: () =>
    client.post('/api/auth/logout'),
};
