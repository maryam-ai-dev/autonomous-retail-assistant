import Cookies from "js-cookie";

const TOKEN_KEY = "auth_token";

export function saveToken(token: string): void {
  Cookies.set(TOKEN_KEY, token, {
    path: "/",
    sameSite: "strict",
    secure: process.env.NODE_ENV === "production",
  });
}

export function getToken(): string | undefined {
  return Cookies.get(TOKEN_KEY);
}

export function clearToken(): void {
  Cookies.remove(TOKEN_KEY, { path: "/" });
}

export function isAuthenticated(): boolean {
  const token = getToken();
  return !!token && token.length > 0;
}
