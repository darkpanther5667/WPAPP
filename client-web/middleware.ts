import { NextResponse } from 'next/server'
import type { NextRequest } from 'next/server'

export function middleware(request: NextRequest) {
  // If the user visits the exact root "/", internally rewrite to "/index.html"
  if (request.nextUrl.pathname === '/') {
    return NextResponse.rewrite(new URL('/index.html', request.url))
  }
}

export const config = {
  matcher: '/',
}
