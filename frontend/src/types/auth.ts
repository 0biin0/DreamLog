export interface User {
  id: number;
  email: string;
  nickname: string;
  profileImage: string | null;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface SignupRequest {
  email: string;
  password: string;
  nickname: string;
}

export interface TokenResponse {
  accessToken: string;
  expiresIn: number;
}

export interface ApiResponse<T> {
  success: boolean;
  data: T | null;
  error: string | null;
}

export interface AuthResponse {
  token: TokenResponse;
  user: User;
}
