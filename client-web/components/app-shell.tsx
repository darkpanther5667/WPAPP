"use client";

import { usePathname } from "next/navigation";
import {
  Home,
  Users,
  FileText,
  BarChart3,
  Settings,
  LayoutDashboard,
} from "lucide-react";
import Link from "next/link";
import { cn } from "@/lib/utils";
import { useState } from "react";

const nav = [
  { href: "/dashboard", label: "Dashboard", Icon: Home },
  { href: "/customers", label: "Customers", Icon: Users },
  { href: "/invoices", label: "Invoices", Icon: FileText },
  { href: "/reports", label: "Reports", Icon: BarChart3 },
  { href: "/settings", label: "Settings", Icon: Settings },
];

const sidebarNav = [
  { href: "/dashboard", label: "Dashboard", Icon: LayoutDashboard },
  { href: "/customers", label: "Customers", Icon: Users },
  { href: "/invoices", label: "Invoices", Icon: FileText },
  { href: "/payments", label: "Payments", Icon: BarChart3 },
  { href: "/reports", label: "Reports", Icon: BarChart3 },
  { href: "/settings", label: "Settings", Icon: Settings },
];

export function AppShell({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();

  const isActive = (href: string) =>
    pathname === href || pathname.startsWith(href + "/");

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-dark-950 flex">
      {/* ===== SIDEBAR (desktop) ===== */}
      <aside className="hidden lg:flex flex-col w-64 bg-white dark:bg-dark-900 border-r border-gray-200 dark:border-dark-800 fixed inset-y-0 left-0 z-50">
        {/* Logo */}
        <div className="flex items-center gap-3 px-6 h-16 border-b border-gray-200 dark:border-dark-800">
          <div className="w-9 h-9 rounded-xl bg-brand-indigo flex items-center justify-center">
            <span className="text-white text-lg font-bold">G</span>
          </div>
          <div>
            <span className="font-bold text-lg text-gray-900 dark:text-white">
              Grahbook
            </span>
            <span className="block text-[10px] text-gray-500 dark:text-gray-400 leading-tight">
              Web Panel
            </span>
          </div>
        </div>

        {/* Nav items */}
        <nav className="flex-1 px-3 py-4 space-y-1 overflow-y-auto">
          {sidebarNav.map(({ href, label, Icon }) => {
            const active = isActive(href);
            return (
              <Link
                key={href}
                href={href}
                className={cn(
                  "flex items-center gap-3 px-4 py-2.5 rounded-xl text-sm font-medium transition-all duration-200",
                  active
                    ? "bg-brand-indigo text-white shadow-sm shadow-brand-indigo/30"
                    : "text-gray-600 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-dark-800 hover:text-gray-900 dark:hover:text-white"
                )}
              >
                <Icon
                  className={cn("h-5 w-5", active ? "text-white" : "")}
                  strokeWidth={active ? 2.5 : 1.8}
                />
                {label}
              </Link>
            );
          })}
        </nav>

        {/* Bottom section */}
        <div className="px-3 py-4 border-t border-gray-200 dark:border-dark-800">
          <div className="flex items-center gap-3 px-4 py-2.5">
            <div className="w-8 h-8 rounded-lg bg-brand-saffron flex items-center justify-center text-white text-sm font-bold">
              S
            </div>
            <div className="text-sm">
              <p className="font-medium text-gray-900 dark:text-white">
                My Store
              </p>
              <p className="text-[11px] text-gray-500 dark:text-gray-400">
                Online
              </p>
            </div>
          </div>
        </div>
      </aside>

      {/* ===== MAIN CONTENT ===== */}
      <div className="flex-1 flex flex-col lg:pl-64">
        {/* Mobile header */}
        <header className="sticky top-0 z-40 lg:relative bg-white/80 dark:bg-dark-900/80 backdrop-blur-xl border-b border-gray-200 dark:border-dark-800 lg:hidden">
          <div className="flex items-center justify-between px-4 h-14">
            <Link href="/dashboard" className="flex items-center gap-2">
              <div className="w-8 h-8 rounded-xl bg-brand-indigo flex items-center justify-center">
                <span className="text-white text-lg font-bold">G</span>
              </div>
              <span className="font-bold text-lg text-gray-900 dark:text-white">
                Grahbook
              </span>
            </Link>
          </div>
        </header>

        {/* Page content */}
        <main className="flex-1 overflow-auto pb-24 lg:pb-8">{children}</main>
      </div>

      {/* ===== MOBILE BOTTOM NAV (floating pill, like Android) ===== */}
      <nav className="lg:hidden fixed bottom-0 left-0 right-0 z-50 flex justify-center pb-3 pointer-events-none">
        <div className="mx-auto max-w-screen-sm w-full px-4 pointer-events-auto">
          <div className="bg-white/95 dark:bg-dark-900/95 backdrop-blur-xl shadow-xl border border-gray-200/50 dark:border-dark-800/50 rounded-2xl flex items-center justify-around h-14 px-2">
            {nav.map(({ href, label, Icon }) => {
              const active = isActive(href);
              return (
                <Link
                  key={href}
                  href={href}
                  className={cn(
                    "flex flex-col items-center justify-center gap-0.5 rounded-xl transition-all duration-200 px-3 py-1.5",
                    active
                      ? "bg-brand-indigo text-white"
                      : "text-gray-400 dark:text-gray-500 hover:text-gray-600 dark:hover:text-gray-300"
                  )}
                >
                  <Icon
                    className="h-5 w-5"
                    strokeWidth={active ? 2.5 : 1.8}
                  />
                  <span
                    className={cn(
                      "text-[9px] font-medium leading-tight",
                      active && "font-semibold"
                    )}
                  >
                    {label}
                  </span>
                </Link>
              );
            })}
          </div>
        </div>
      </nav>
    </div>
  );
}
