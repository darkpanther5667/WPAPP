"use client";

import { useAuthStore } from "@/stores/auth-store";
import { useRouter, usePathname } from "next/navigation";
import { useEffect } from "react";

export function useAuthGuard() {
  const { isAuthenticated } = useAuthStore();
  const router = useRouter();
  const pathname = usePathname();

  useEffect(() => {
    const publicRoutes = ["/login", "/register"];
    if (!isAuthenticated && !publicRoutes.includes(pathname)) {
      router.replace("/login");
    }
    if (isAuthenticated && publicRoutes.includes(pathname)) {
      router.replace("/dashboard");
    }
  }, [isAuthenticated, pathname, router]);
}

/** Client component wrapper for use in Server Component layouts */
export function AuthGuard() {
  useAuthGuard();
  return null;
}
