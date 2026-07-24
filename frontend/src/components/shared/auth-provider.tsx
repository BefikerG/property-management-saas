"use client";

import { useEffect, useState } from "react";
import { refreshAccessToken } from "@/lib/api/auth-actions";

/**
 * Wraps the authenticated app tree. On mount, attempts a silent
 * token refresh using the httpOnly cookie — this restores the
 * session after a full page reload without requiring the user
 * to log in again, as long as their refresh token is still valid.
 */
export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [isRestoring, setIsRestoring] = useState(true);

  useEffect(() => {
    refreshAccessToken().finally(() => setIsRestoring(false));
  }, []);

  if (isRestoring) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-background">
        <div className="h-6 w-6 rounded-full border-2 border-border border-t-primary animate-spin" />
      </div>
    );
  }

  return <>{children}</>;
}