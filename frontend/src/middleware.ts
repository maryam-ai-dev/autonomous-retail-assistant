import { NextRequest, NextResponse } from "next/server";

const DASHBOARD_PATHS = [
  "/search",
  "/recommendations",
  "/cart",
  "/approvals",
  "/trust",
  "/audit-log",
  "/preferences",
  "/profile",
  "/robotics-simulation",
];

const AUTH_PATHS = ["/login", "/signup"];

export function middleware(request: NextRequest) {
  const token = request.cookies.get("auth_token")?.value;
  const { pathname } = request.nextUrl;

  const isDashboardRoute = DASHBOARD_PATHS.some(
    (path) => pathname === path || pathname.startsWith(path + "/")
  );

  const isAuthRoute = AUTH_PATHS.some(
    (path) => pathname === path || pathname.startsWith(path + "/")
  );

  if (isDashboardRoute && (!token || token.length === 0)) {
    const loginUrl = new URL("/login", request.url);
    return NextResponse.redirect(loginUrl);
  }

  if (isAuthRoute && token && token.length > 0) {
    const searchUrl = new URL("/search", request.url);
    return NextResponse.redirect(searchUrl);
  }

  return NextResponse.next();
}

export const config = {
  matcher: [
    "/search/:path*",
    "/recommendations/:path*",
    "/cart/:path*",
    "/approvals/:path*",
    "/trust/:path*",
    "/audit-log/:path*",
    "/preferences/:path*",
    "/profile/:path*",
    "/robotics-simulation/:path*",
    "/login/:path*",
    "/signup/:path*",
  ],
};
