import { authApi } from "./client";
import { useAuthStore } from "@/stores/auth-store";
import type { LoginRequestDto } from "./generated";

/**
 * Performs login against POST /api/v1/auth/login.
 * On success: stores the access token + role in memory (Zustand),
 * and the refresh token in an httpOnly cookie via a Next.js Route
 * Handler (browsers cannot set httpOnly cookies from client JS directly —
 * this requires a same-origin API route acting as the cookie-setting proxy).
 */
export async function login(credentials: LoginRequestDto) {
  const { data } = await authApi.login({ loginRequestDto: credentials });

  useAuthStore.getState().setAccessToken(
    data.accessToken!,
    data.role as "ADMINISTRATOR" | "PROPERTY_MANAGER" | "VIEWER"
  );

  // Persist the refresh token via our own Route Handler — see 3.3.
  await fetch("/api/auth/set-refresh-cookie", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ refreshToken: data.refreshToken }),
  });

  return data;
}

/**
 * Attempts to silently refresh the access token using the httpOnly
 * refresh token cookie. Called by the Axios response interceptor on
 * any 401, and once on initial app load to restore a session after
 * a page refresh (since the in-memory access token is gone at that point).
 */
export async function refreshAccessToken(): Promise<string | null> {
  try {
    const res = await fetch("/api/auth/refresh", { method: "POST" });
    if (!res.ok) return null;

    const data = await res.json();
    useAuthStore.getState().setAccessToken(data.accessToken, data.role);
    return data.accessToken;
  } catch {
    return null;
  }
}

export async function logout() {
  useAuthStore.getState().clearAuth();
  await fetch("/api/auth/clear-refresh-cookie", { method: "POST" });
}