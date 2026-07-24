import { create } from "zustand";
import { jwtDecode } from "jwt-decode";

interface DecodedToken {
  sub: string;      // email — JWT subject claim
  org_id?: string;  // organization UUID, absent for platform-level tokens
  exp: number;
  iat: number;
}

export type StaffRole = "ADMINISTRATOR" | "PROPERTY_MANAGER" | "VIEWER";

interface AuthState {
  accessToken: string | null;
  email: string | null;
  organizationId: string | null;
  role: StaffRole | null;
  isAuthenticated: boolean;

  setAccessToken: (token: string, role: StaffRole) => void;
  clearAuth: () => void;
}

/**
 * In-memory auth state only. The access token is NEVER persisted to
 * localStorage or sessionStorage — both are readable by any injected
 * script, making them vulnerable to XSS token theft. Living only in
 * memory means a page refresh clears it, which is why the refresh
 * token (stored in an httpOnly cookie, invisible to JavaScript) exists
 * to silently re-establish a session on load — see auth-actions.ts.
 *
 * role and organizationId are decoded from the JWT payload client-side.
 * This is safe because JWT payloads are base64-encoded, not encrypted —
 * only the signature is secret, and we never trust these decoded values
 * for anything except UI rendering decisions. Every actual authorization
 * check happens on the backend regardless of what this store contains.
 */
export const useAuthStore = create<AuthState>((set) => ({
  accessToken: null,
  email: null,
  organizationId: null,
  role: null,
  isAuthenticated: false,

  setAccessToken: (token, role) => {
    const decoded = jwtDecode<DecodedToken>(token);
    set({
      accessToken: token,
      email: decoded.sub,
      organizationId: decoded.org_id ?? null,
      role,
      isAuthenticated: true,
    });
  },

  clearAuth: () => {
    set({
      accessToken: null,
      email: null,
      organizationId: null,
      role: null,
      isAuthenticated: false,
    });
  },
}));