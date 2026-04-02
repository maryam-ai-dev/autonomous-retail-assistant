import apiClient from "@/core/api/client";
import { AUTH } from "@/core/api/endpoints";
import { saveToken, clearToken } from "@/core/auth/session";

interface AuthResponse {
  token: string;
  userId: string;
  username: string;
}

export async function register(
  email: string,
  password: string,
  username: string,
  displayName: string
): Promise<AuthResponse> {
  const { data } = await apiClient.post<AuthResponse>(AUTH.REGISTER, {
    email,
    password,
    username,
    displayName,
  });
  saveToken(data.token);
  return data;
}

export async function login(
  email: string,
  password: string
): Promise<AuthResponse> {
  const { data } = await apiClient.post<AuthResponse>(AUTH.LOGIN, {
    email,
    password,
  });
  saveToken(data.token);
  return data;
}

export function logout(): void {
  clearToken();
  if (typeof window !== "undefined") {
    window.location.href = "/login";
  }
}
