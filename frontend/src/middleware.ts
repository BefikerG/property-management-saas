import { NextRequest, NextResponse } from "next/server";

/**
 * Route protection at the Edge runtime — runs before any page component.
 *
 * We check for the presence of the httpOnly refreshToken cookie, NOT
 * the access token (which lives in memory and is invisible to middleware
 * anyway, since middleware runs server-side with no access to client state).
 *
 * This is a coarse check: "does this browser have a session at all?"
 * The fine-grained check (is the access token still valid, what role
 * does the user have) happens client-side via AuthProvider + the
 * backend's own 401 responses. Middleware's job is only to stop a
 * completely unauthenticated browser from ever rendering a dashboard
 * page — it is not a substitute for the backend's own authorization.
 */
export function middleware(request: NextRequest) {
  const hasSession = request.cookies.has("refreshToken");
  const { pathname } = request.nextUrl;

  const isAuthRoute = pathname.startsWith("/login");
  const isProtectedRoute = !isAuthRoute && pathname !== "/api";

  if (!hasSession && isProtectedRoute && !pathname.startsWith("/api/auth")) {
    return NextResponse.redirect(new URL("/login", request.url));
  }

  if (hasSession && isAuthRoute) {
    return NextResponse.redirect(new URL("/", request.url));
  }

  return NextResponse.next();
}

export const config = {
  matcher: ["/((?!_next/static|_next/image|favicon.ico).*)"],
};