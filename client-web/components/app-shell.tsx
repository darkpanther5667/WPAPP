"use client";

import { usePathname } from "next/navigation";
import { Home, Users, FileText, BarChart3, Settings } from "lucide-react";
import Link from "next/link";
import { cn } from "@/lib/utils";

const nav = [
  { href: "/dashboard", label: "Dashboard", Icon: Home },
  { href: "/customers", label: "Customers", Icon: Users },
  { href: "/invoices", label: "Invoices", Icon: FileText },
  { href: "/reports", label: "Reports", Icon: BarChart3 },
  { href: "/settings", label: "Settings", Icon: Settings },
];

export function AppShell({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();

  return (
    <div className="mx-auto max-w-screen-sm min-h-screen bg-gray-50 dark:bg-dark-950 flex flex-col">
      {/* Header */}
      <header className="sticky top-0 z-50 bg-white/80 dark:bg-dark-900/80 backdrop-blur-xl border-b border-border">
        <div className="flex items-center justify-between px-4 h-14">
          <Link href="/dashboard" className="flex items-center gap-2">
            <div className="w-8 h-8 rounded-xl bg-primary flex items-center justify-center">
              <span className="text-white text-lg">🏪</span>
            </div>
            <span className="font-bold text-lg text-foreground">Grahbook</span>
          </Link>
          <span className="text-xs text-muted-foreground hidden sm:inline">
            Web Panel
          </span>
        </div>
      </header>

      {/* Main content */}
      <main className="flex-1 overflow-auto pb-24">{children}</main>

      {/* Bottom nav */}
      <nav className="fixed bottom-0 left-0 right-0 z-50 bg-white/90 dark:bg-dark-900/90 backdrop-blur-xl border-t border-border">
        <div className="mx-auto max-w-screen-sm flex items-center justify-around h-16">
          {nav.map(({ href, label, Icon }) => {
            const active = pathname === href || pathname.startsWith(href + "/");
            return (
              <Link
                key={href}
                href={href}
                className={cn(
                  "flex flex-col items-center justify-center gap-1 w-16 h-full transition-colors",
                  active
                    ? "text-primary"
                    : "text-muted-foreground hover:text-foreground"
                )}
              >
                <Icon
                  className="h-5 w-5"
                  strokeWidth={active ? 2.5 : 1.8}
                />
                <span
                  className={cn(
                    "text-[10px] font-medium",
                    active && "font-bold"
                  )}
                >
                  {label}
                </span>
              </Link>
            );
          })}
        </div>
      </nav>
    </div>
  );
}
