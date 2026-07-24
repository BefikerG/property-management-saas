import { NextRequest, NextResponse } from "next/server";

export async function POST(request: NextRequest) {
  const { refreshToken } = await request.json();

  const response = NextResponse.json({ success: true });
  response.cookies.set("refreshToken", refreshToken, {
    httpOnly: true,
    secure: process.env.NODE_ENV === "production",
    sameSite: "strict",
    path: "/",
    maxAge: 60 * 60 * 24 * 7, // 7 days — matches backend refresh token expiry
  });

  return response;
}