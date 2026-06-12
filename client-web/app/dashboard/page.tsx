"use client";

import { useEffect, useState, useCallback } from "react";
import Link from "next/link";
import { apiClient } from "@/lib/api-client";
import { useAuthStore } from "@/stores/auth-store";
import { formatCurrency, formatDateTime, formatPhone, cn, formatDate } from "@/lib/utils";
import {
  FullDatabase,
  Transaction,
  Customer,
  Bill,
  DailyReport,
} from "@/types";
import {
  Skeleton,
  Button,
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from "@/components/ui";
import {
  Users,
  Receipt,
  TrendingUp,
  AlertCircle,
  ArrowUpRight,
  ArrowDownRight,
  Plus,
  RefreshCw,
  Wallet,
} from "lucide-react";

export default function DashboardPage() {
  const { store } = useAuthStore();
  const [db, setDb] = useState<FullDatabase | null>(null);
  const [report, setReport] = useState<DailyReport | null>(null);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [hydrated, setHydrated] = useState(false);

  useEffect(() => {
    if (useAuthStore.persist.hasHydrated()) {
      setHydrated(true);
      return;
    }

    const unsubscribe = useAuthStore.persist.onFinishHydration(() => {
      setHydrated(true);
    });

    return () => {
      unsubscribe();
    };
  }, []);

  const fetchData = useCallback(async () => {
    try {
      const [dbRes, reportRes] = await Promise.all([
        apiClient.get("/api/db"),
        apiClient.get("/api/report"),
      ]);
      setDb(dbRes.data);
      if (reportRes.data) setReport(reportRes.data);
    } catch {
      // Keep existing data, show error toast
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, []);

  useEffect(() => {
    if (!hydrated) return;
    fetchData();
  }, [hydrated, fetchData]);

  const onRefresh = async () => {
    setRefreshing(true);
    await fetchData();
  };

  if (loading || !hydrated) {
    return (
      <div className="px-4 py-5 space-y-5 page-enter">
        <div className="flex items-center justify-between">
          <div>
            <Skeleton className="h-7 w-40 mb-1" />
            <Skeleton className="h-4 w-56" />
          </div>
          <Skeleton className="h-10 w-10 rounded-full" />
        </div>
        <div className="grid grid-cols-2 gap-3">
          {[1, 2, 3, 4].map((i) => (
            <Skeleton key={i} className="h-28 rounded-2xl" />
          ))}
        </div>
        <Skeleton className="h-48 rounded-2xl" />
      </div>
    );
  }

  const customers = db?.customers ?? [];
  const transactions = db?.transactions ?? [];
  const bills = db?.bills ?? [];

  const outstandingTotal = customers.reduce((sum, c) => {
    return (
      sum +
      (transactions
        .filter((t) => t.customer_id === c.id)
        .reduce(
          (s, t) => s + (t.type === "credit" ? t.amount : -t.amount),
          0
        ) +
        bills
          .filter((b) => b.customer_id === c.id && (b.status === "unpaid" || b.status === "overdue" || b.status === "partial"))
          .reduce((s, b) => s + b.total, 0))
    );
  }, 0);

  const paidToday = transactions
    .filter((t) => t.type === "payment" && t.timestamp.startsWith(new Date().toISOString().slice(0, 10)))
    .reduce((s, t) => s + t.amount, 0);

  const pendingBills = bills.filter((b) => b.status === "unpaid" || b.status === "overdue" || b.status === "partial").length;

  const recentTransactions: Transaction[] = [...transactions]
    .sort((a, b) => new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime())
    .slice(0, 5);

  return (
    <div className="px-4 py-5 space-y-5 page-enter">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-extrabold text-foreground">
            {formatDate(new Date().toISOString())}
          </h1>
          <p className="text-sm text-muted-foreground mt-0.5">
            {store?.store_name || "My Store"}
          </p>
        </div>
        <button
          onClick={onRefresh}
          disabled={refreshing}
          className={cn(
            "p-2.5 rounded-full bg-primary/10 text-primary transition-all",
            refreshing && "animate-spin"
          )}
        >
          <RefreshCw className="h-5 w-5" />
        </button>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-2 gap-3">
        <StatCard
          icon={<Wallet className="h-5 w-5" />}
          label="Outstanding"
          value={formatCurrency(outstandingTotal)}
          color="text-red-500 bg-red-50 dark:bg-red-900/20"
          trend={null}
        />
        <StatCard
          icon={<TrendingUp className="h-5 w-5" />}
          label="Today Collected"
          value={formatCurrency(paidToday)}
          color="text-green-600 bg-green-50 dark:bg-green-900/20"
          trend="up"
        />
        <StatCard
          icon={<AlertCircle className="h-5 w-5" />}
          label="Pending Bills"
          value={String(pendingBills)}
          color="text-amber-600 bg-amber-50 dark:bg-amber-900/20"
          trend={null}
        />
        <StatCard
          icon={<Users className="h-5 w-5" />}
          label="Customers"
          value={String(customers.length)}
          color="text-blue-600 bg-blue-50 dark:bg-blue-900/20"
          trend={null}
        />
      </div>

      {/* Quick Actions */}
      <div>
        <h2 className="text-sm font-semibold text-foreground mb-2.5">
          Quick Actions
        </h2>
        <div className="grid grid-cols-3 gap-2.5">
          <Link
            href="/invoices/create"
            className="flex flex-col items-center gap-2 p-4 rounded-2xl bg-primary text-white shadow-lg shadow-primary/25 hover:shadow-xl hover:shadow-primary/30 transition-all"
          >
            <Plus className="h-5 w-5" />
            <span className="text-xs font-semibold">New Invoice</span>
          </Link>
          <Link
            href="/customers"
            className="flex flex-col items-center gap-2 p-4 rounded-2xl bg-white dark:bg-dark-800 border border-border shadow-sm hover:shadow-md transition-all"
          >
            <Users className="h-5 w-5 text-primary" />
            <span className="text-xs font-semibold text-foreground">
              Customers
            </span>
          </Link>
          <Link
            href="/payments"
            className="flex flex-col items-center gap-2 p-4 rounded-2xl bg-white dark:bg-dark-800 border border-border shadow-sm hover:shadow-md transition-all"
          >
            <Wallet className="h-5 w-5 text-primary" />
            <span className="text-xs font-semibold text-foreground">
              Payments
            </span>
          </Link>
        </div>
      </div>

      {/* Recent Transactions */}
      <Card>
        <CardHeader className="pb-3">
          <CardTitle className="text-base">Recent Activity</CardTitle>
        </CardHeader>
        <CardContent className="space-y-2">
          {recentTransactions.length === 0 ? (
            <EmptyState
              icon={<Receipt className="h-8 w-8 text-muted-foreground/50" />}
              title="No transactions yet"
              subtitle="Start by creating an invoice"
            />
          ) : (
            recentTransactions.map((tx: Transaction) => {
              const customer = customers.find(
                (c) => c.id === tx.customer_id
              );
              return (
                <div
                  key={tx.id}
                  className="flex items-center gap-3 p-3 rounded-xl hover:bg-accent/50 transition-colors"
                >
                  <div
                    className={cn(
                      "flex h-9 w-9 shrink-0 items-center justify-center rounded-full",
                      tx.type === "payment"
                        ? "bg-green-100 text-green-600 dark:bg-green-900/30"
                        : "bg-red-100 text-red-600 dark:bg-red-900/30"
                    )}
                  >
                    {tx.type === "payment" ? (
                      <ArrowDownRight className="h-4 w-4" />
                    ) : (
                      <ArrowUpRight className="h-4 w-4" />
                    )}
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-medium text-foreground truncate">
                      {customer?.name ?? "Unknown"}
                    </p>
                    <p className="text-xs text-muted-foreground">
                      {tx.note || tx.payment_mode}
                    </p>
                  </div>
                  <div className="text-right">
                    <p
                      className={cn(
                        "text-sm font-bold",
                        tx.type === "payment"
                          ? "text-green-600"
                          : "text-red-500"
                      )}
                    >
                      {tx.type === "payment" ? "+" : "-"}
                      {formatCurrency(tx.amount)}
                    </p>
                    <p className="text-[10px] text-muted-foreground">
                      {formatDateTime(tx.timestamp)}
                    </p>
                  </div>
                </div>
              );
            })
          )}
        </CardContent>
      </Card>

      {/* Outstanding Customers */}
      {outstandingTotal > 0 && (
        <Card>
          <CardHeader className="pb-3">
            <CardTitle className="text-base flex items-center gap-2">
              <AlertCircle className="h-4 w-4 text-red-500" />
              Outstanding Payments
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-2">
            {customers
              .map((c) => ({
                customer: c,
                balance: (() => {
                  const txBalance = transactions
                    .filter((t) => t.customer_id === c.id)
                    .reduce(
                      (s, t) => s + (t.type === "credit" ? t.amount : -t.amount),
                      0
                    );
                  const billTotal = bills
                    .filter((b) => b.customer_id === c.id && (b.status === "unpaid" || b.status === "overdue" || b.status === "partial"))
                    .reduce((s, b) => s + b.total, 0);
                  return txBalance + billTotal;
                })(),
              }))
              .filter(({ balance }) => balance > 0)
              .slice(0, 5)
              .map(({ customer, balance }) => (
                <Link
                  key={customer.id}
                  href={`/customers/${customer.id}`}
                  className="flex items-center gap-3 p-3 rounded-xl hover:bg-accent/50 transition-colors"
                >
                  <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-primary/10 text-primary font-bold text-xs">
                    {customer.name
                      .split(" ")
                      .map((w) => w[0])
                      .join("")
                      .slice(0, 2)}
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-medium text-foreground truncate">
                      {customer.name}
                    </p>
                    <p className="text-xs text-muted-foreground">
                      {formatPhone(customer.phone)}
                    </p>
                  </div>
                  <span className="text-sm font-bold text-red-500">
                    {formatCurrency(balance)}
                  </span>
                </Link>
              ))}
          </CardContent>
        </Card>
      )}
    </div>
  );
}

function StatCard({
  icon,
  label,
  value,
  color,
  trend,
}: {
  icon: React.ReactNode;
  label: string;
  value: string;
  color: string;
  trend?: "up" | "down" | null;
}) {
  return (
    <Card className="relative overflow-hidden">
      <CardContent className="p-4">
        <div
          className={cn("h-9 w-9 rounded-xl flex items-center justify-center mb-2.5", color)}
        >
          {icon}
        </div>
        <p className="text-xs text-muted-foreground font-medium">{label}</p>
        <p className="text-lg font-extrabold text-foreground mt-0.5">{value}</p>
      </CardContent>
    </Card>
  );
}

function EmptyState({
  icon,
  title,
  subtitle,
}: {
  icon: React.ReactNode;
  title: string;
  subtitle: string;
}) {
  return (
    <div className="flex flex-col items-center justify-center py-8 text-center">
      {icon}
      <p className="text-sm font-medium text-foreground mt-2">{title}</p>
      <p className="text-xs text-muted-foreground mt-0.5">{subtitle}</p>
    </div>
  );
}
