"use client";

import { useEffect, useState, useMemo } from "react";
import { apiClient } from "@/lib/api-client";
import { useAuthStore } from "@/stores/auth-store";
import { formatCurrency, formatDate, cn } from "@/lib/utils";
import { Transaction, Bill, Customer } from "@/types";
import { Card, CardContent, CardHeader, CardTitle, Skeleton } from "@/components/ui";
import {
  Wallet,
  AlertTriangle,
  RefreshCw,
  Upload,
  Calendar,
} from "lucide-react";
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  ResponsiveContainer,
  CartesianGrid,
} from "recharts";

export default function ReportsPage() {
  const { store } = useAuthStore();
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [bills, setBills] = useState<Bill[]>([]);
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);

  const fetchData = async () => {
    if (!store?.id) return;
    try {
      const { data } = await apiClient.get(`/api/db?storeId=${store.id}`);
      setTransactions(data.transactions || []);
      setBills(data.bills || []);
      setCustomers(data.customers || []);
    } catch (err: any) {
      console.error(err);
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, [store?.id]);

  const todayStr = new Date().toISOString().slice(0, 10);
  const todayPayments = transactions
    .filter((t) => t.type === "payment" && t.timestamp.startsWith(todayStr))
    .reduce((s, t) => s + t.amount, 0);
  const todayBills = bills
    .filter((b) => b.created_at?.startsWith(todayStr))
    .reduce((s, b) => s + b.total, 0);
  const outstandingTotal = customers.reduce((sum, c) => {
    const txB = transactions
      .filter((t) => t.customer_id === c.id)
      .reduce((s, t) => s + (t.type === "credit" ? t.amount : -t.amount), 0);
    const billT = bills
      .filter((b) => b.customer_id === c.id && b.status === "unpaid")
      .reduce((s, b) => s + b.total, 0);
    return sum + txB + billT;
  }, 0);

  // Last 7 days chart data
  const chartData = useMemo(() => {
    const days: { date: string; bills: number; payments: number }[] = [];
    for (let i = 6; i >= 0; i--) {
      const d = new Date();
      d.setDate(d.getDate() - i);
      const ds = d.toISOString().slice(0, 10);
      const dayBills = bills
        .filter((b) => b.created_at?.startsWith(ds))
        .reduce((s, b) => s + b.total, 0);
      const dayPayments = transactions
        .filter((t) => t.type === "payment" && t.timestamp.startsWith(ds))
        .reduce((s, t) => s + t.amount, 0);
      days.push({
        date: d.toLocaleDateString("en-IN", { day: "2-digit", month: "short" }),
        bills: dayBills,
        payments: dayPayments,
      });
    }
    return days;
  }, [bills, transactions]);

  // Top outstanding customers
  const topOutstanding = useMemo(() => {
    return customers
      .map((c) => {
        const txB = transactions
          .filter((t) => t.customer_id === c.id)
          .reduce((s, t) => s + (t.type === "credit" ? t.amount : -t.amount), 0);
        const billT = bills
          .filter((b) => b.customer_id === c.id && b.status === "unpaid")
          .reduce((s, b) => s + b.total, 0);
        return { customer: c, balance: txB + billT };
      })
      .filter((x) => x.balance > 0)
      .sort((a, b) => b.balance - a.balance)
      .slice(0, 8);
  }, [customers, transactions, bills]);

  if (loading) {
    return (
      <div className="px-4 py-5 space-y-4 page-enter">
        <Skeleton className="h-8 w-20 rounded-xl" />
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-2.5">
          {[1, 2, 3].map((i) => (
            <Skeleton key={i} className="h-24 rounded-2xl" />
          ))}
        </div>
        <Skeleton className="h-56 rounded-2xl" />
        <Skeleton className="h-40 rounded-2xl" />
      </div>
    );
  }

  return (
    <div className="px-4 py-5 space-y-5 page-enter">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-extrabold text-foreground">Reports</h1>
        <button
          onClick={() => { setRefreshing(true); fetchData(); }}
          disabled={refreshing}
          className="p-2 rounded-full bg-primary/10 text-primary"
        >
          <RefreshCw className={cn("h-4 w-4", refreshing && "animate-spin")} />
        </button>
      </div>

      {/* Summary Cards */}
      <div className="grid grid-cols-3 gap-2.5">
        <StatCard icon={<Upload className="h-4 w-4" />} label="Today Sales" value={formatCurrency(todayBills)} color="text-blue-600 bg-blue-50 dark:bg-blue-900/20" />
        <StatCard icon={<Wallet className="h-4 w-4" />} label="Collected" value={formatCurrency(todayPayments)} color="text-green-600 bg-green-50 dark:bg-green-900/20" />
        <StatCard icon={<AlertTriangle className="h-4 w-4" />} label="Outstanding" value={formatCurrency(outstandingTotal)} color="text-red-500 bg-red-50 dark:bg-red-900/20" />
      </div>

      {/* Chart */}
      <Card>
        <CardHeader className="pb-2">
          <CardTitle className="text-sm flex items-center gap-2">
            <Calendar className="h-4 w-4" />
            Last 7 Days
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="h-48">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={chartData}>
                <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" />
                <XAxis dataKey="date" tick={{ fontSize: 10 }} stroke="#94a3b8" />
                <YAxis tick={{ fontSize: 10 }} stroke="#94a3b8" />
                <Tooltip
                  formatter={(value: any) => formatCurrency(value)}
                  contentStyle={{ borderRadius: 12, border: "1px solid #e5e7eb", fontSize: 12 }}
                />
                <Bar dataKey="bills" fill="#25d366" radius={[4, 4, 0, 0]} name="Bills" />
                <Bar dataKey="payments" fill="#128c7e" radius={[4, 4, 0, 0]} name="Payments" />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </CardContent>
      </Card>

      {/* Outstanding Customers */}
      <Card>
        <CardHeader className="pb-3">
          <CardTitle className="text-base flex items-center gap-2">
            <AlertTriangle className="h-4 w-4 text-amber-500" />
            Outstanding Customers ({topOutstanding.length})
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-2">
          {topOutstanding.length === 0 ? (
            <p className="text-sm text-muted-foreground text-center py-4">
              No outstanding balances
            </p>
          ) : (
            topOutstanding.map(({ customer, balance }) => (
              <div key={customer.id} className="flex items-center justify-between p-3 rounded-xl bg-accent/30">
                <div>
                  <p className="text-sm font-medium text-foreground">{customer.name}</p>
                  <p className="text-xs text-muted-foreground">{customer.phone}</p>
                </div>
                <span className="text-sm font-extrabold text-red-500">{formatCurrency(balance)}</span>
              </div>
            ))
          )}
        </CardContent>
      </Card>
    </div>
  );
}

function StatCard({ icon, label, value, color }: { icon: React.ReactNode; label: string; value: string; color: string }) {
  return (
    <Card>
      <CardContent className="p-3">
        <div className={cn("h-8 w-8 rounded-lg flex items-center justify-center mb-1.5", color)}>{icon}</div>
        <p className="text-[10px] text-muted-foreground font-medium leading-none">{label}</p>
        <p className="text-sm font-extrabold text-foreground mt-1 truncate">{value}</p>
      </CardContent>
    </Card>
  );
}
