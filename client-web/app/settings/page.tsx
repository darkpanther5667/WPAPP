"use client";

import { useEffect, useState, useCallback } from "react";
import { useRouter } from "next/navigation";
import { apiClient } from "@/lib/api-client";
import { useAuthStore } from "@/stores/auth-store";
import { formatCurrency, formatPhone, cn } from "@/lib/utils";
import { Customer, Bill, Transaction } from "@/types";
import { Button } from "@/components/ui/button";
import { Card, CardContent, Skeleton } from "@/components/ui";
import {
  Store,
  User,
  Phone,
  FileText,
  Wallet,
  TrendingUp,
  RefreshCw,
  LogOut,
  ChevronRight,
  Users,
  Copy,
} from "lucide-react";

export default function SettingsPage() {
  const router = useRouter();
  const { store, user, logout } = useAuthStore();
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [bills, setBills] = useState<Bill[]>([]);
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [loading, setLoading] = useState(true);
  const [loggingOut, setLoggingOut] = useState(false);
  const [toast, setToast] = useState<string | null>(null);

  const fetchData = useCallback(async () => {
    if (!store?.id) return;
    try {
      const { data } = await apiClient.get(`/api/db?storeId=${store.id}`);
      setCustomers(data.customers || []);
      setBills(data.bills || []);
      setTransactions(data.transactions || []);
    } catch (err: any) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  }, [store?.id]);

  useEffect(() => {
    fetchData();
  }, [fetchData]);

  const totalRevenue = transactions
    .filter((t) => t.type === "payment")
    .reduce((s, t) => s + t.amount, 0);
  const totalBilled = bills.reduce((s, b) => s + b.total, 0);
  const paidBills = bills.filter((b) => b.status === "paid").length;

  const handleLogout = async () => {
    if (!confirm("Are you sure you want to logout?")) return;
    setLoggingOut(true);
    try {
      await apiClient.post("/api/auth/logout");
    } catch { /* ignore */ }
    logout();
    router.push("/login");
  };

  const copyToClipboard = (text: string) => {
    navigator.clipboard.writeText(text);
    setToast("Copied!");
    setTimeout(() => setToast(null), 2000);
  };

  if (loading) {
    return (
      <div className="px-4 py-5 space-y-4 page-enter">
        <Skeleton className="h-8 w-20 rounded-xl" />
        <Skeleton className="h-32 rounded-2xl" />
        <Skeleton className="h-40 rounded-2xl" />
        <Skeleton className="h-16 rounded-2xl" />
      </div>
    );
  }

  return (
    <div className="px-4 py-5 space-y-5 page-enter">
      {/* Store Profile */}
      <Card className="bg-gradient-to-br from-primary/5 to-transparent border-primary/10">
        <CardContent className="p-5 flex items-center gap-4">
          <div className="h-14 w-14 shrink-0 rounded-2xl bg-primary/10 flex items-center justify-center">
            <Store className="h-6 w-6 text-primary" />
          </div>
          <div className="flex-1 min-w-0">
            <h2 className="text-base font-extrabold text-foreground truncate">{store?.store_name}</h2>
            <p className="text-xs text-muted-foreground mt-0.5">Your Business</p>
            {user?.phone && (
              <p className="text-xs text-muted-foreground mt-1 flex items-center gap-1">
                <Phone className="h-3 w-3" />
                {formatPhone(user.phone)}
              </p>
            )}
          </div>
        </CardContent>
      </Card>

      {/* Quick Stats */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-2.5">
        <StatCard icon={<Users className="h-3.5 w-3.5" />} label="Customers" value={String(customers.length)} />
        <StatCard icon={<FileText className="h-3.5 w-3.5" />} label="Bills" value={String(bills.length)} />
        <StatCard icon={<TrendingUp className="h-3.5 w-3.5" />} label="Revenue" value={formatCurrency(totalRevenue)} />
      </div>

      {/* Business Summary */}
      <Card>
        <CardContent className="p-4 space-y-3">
          <h3 className="text-sm font-semibold text-foreground">Business Summary</h3>
          <div className="space-y-2.5">
            <div className="flex justify-between text-sm">
              <span className="text-muted-foreground">Total Billed</span>
              <span className="font-semibold">{formatCurrency(totalBilled)}</span>
            </div>
            <div className="flex justify-between text-sm">
              <span className="text-muted-foreground">Collected</span>
              <span className="font-semibold text-green-600 dark:text-green-400">{formatCurrency(totalRevenue)}</span>
            </div>
            <div className="flex justify-between text-sm">
              <span className="text-muted-foreground">Paid Bills</span>
              <span className="font-semibold">{paidBills} / {bills.length}</span>
            </div>
            <div className="flex justify-between text-sm">
              <span className="text-muted-foreground">Transactions</span>
              <span className="font-semibold">{transactions.length}</span>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Menu Items */}
      <Card>
        <CardContent className="p-2">
          {[
            { icon: <Copy className="h-4 w-4 text-primary" />, label: "Copy Store ID", action: () => copyToClipboard(store?.id || "") },
            { icon: <RefreshCw className="h-4 w-4 text-amber-500" />, label: "Refresh Data", action: () => { setLoading(true); fetchData(); } },
          ].map((item) => (
            <button
              key={item.label}
              onClick={item.action}
              className="w-full flex items-center gap-3 p-3 rounded-xl hover:bg-accent/50 transition-colors"
            >
              <div className="h-8 w-8 shrink-0 rounded-lg bg-accent flex items-center justify-center">
                {item.icon}
              </div>
              <span className="text-sm font-medium text-foreground flex-1">{item.label}</span>
              <ChevronRight className="h-4 w-4 text-muted-foreground" />
            </button>
          ))}
        </CardContent>
      </Card>

      {/* Logout */}
      <Button
        variant="outline"
        className="w-full h-12 text-red-500 border-red-200 dark:border-red-900/50 hover:bg-red-50 dark:hover:bg-red-900/10"
        onClick={handleLogout}
        disabled={loggingOut}
      >
        <LogOut className="h-4 w-4 mr-2" />
        {loggingOut ? "Logging out..." : "Logout"}
      </Button>

      {/* Version */}
      <p className="text-center text-[10px] text-muted-foreground pb-2">
        WhatsApp Billing Pro v1.0
      </p>

      {/* Toast */}
      {toast && (
        <div className="fixed bottom-24 left-1/2 -translate-x-1/2 bg-dark-800 text-white text-xs font-medium px-4 py-2 rounded-full shadow-lg z-50 animate-scale-in">
          {toast}
        </div>
      )}
    </div>
  );
}

function StatCard({ icon, label, value }: { icon: React.ReactNode; label: string; value: string }) {
  return (
    <Card>
      <CardContent className="p-3">
        <div className="h-7 w-7 rounded-lg bg-accent flex items-center justify-center mb-1 text-foreground">
          {icon}
        </div>
        <p className="text-[10px] text-muted-foreground font-medium">{label}</p>
        <p className="text-sm font-extrabold text-foreground mt-0.5 truncate">{value}</p>
      </CardContent>
    </Card>
  );
}
