"use client";

import { useEffect, useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import Link from "next/link";
import { apiClient } from "@/lib/api-client";
import { useAuthStore } from "@/stores/auth-store";
import { formatCurrency, formatPhone, cn, getCustomerOutstanding } from "@/lib/utils";
import { Customer, Transaction, Bill } from "@/types";
import { Button } from "@/components/ui/button";
import { Card, CardContent, Input, Skeleton } from "@/components/ui";
import {
  ChevronLeft,
  ArrowDownRight,
  ArrowUpRight,
  Wallet,
  Users,
} from "lucide-react";

export default function PaymentsPage() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const preSelectedId = searchParams.get("customerId");
  const { store } = useAuthStore();
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [selectedCustomerId, setSelectedCustomerId] = useState(preSelectedId || "");
  const [amount, setAmount] = useState("");
  const [type, setType] = useState<"payment" | "credit">("payment");
  const [mode, setMode] = useState("cash");
  const [note, setNote] = useState("");
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [result, setResult] = useState<string | null>(null);

  const [db, setDb] = useState<{ customers: Customer[]; transactions: Transaction[]; bills: Bill[] } | null>(null);

  useEffect(() => {
    const fetchData = async () => {
      if (!store?.id) return;
      try {
        const { data } = await apiClient.get(`/api/db?storeId=${store.id}`);
        setCustomers(data.customers || []);
        setDb({ customers: data.customers, transactions: data.transactions, bills: data.bills });
      } catch (err: any) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, [store?.id]);

  const modes = [
    { value: "cash", label: "Cash" },
    { value: "upi", label: "UPI" },
    { value: "qr", label: "QR" },
    { value: "cheque", label: "Cheque" },
  ];

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedCustomerId || !amount || parseFloat(amount) <= 0) return;
    setSubmitting(true);
    setResult(null);
    try {
      const { data } = await apiClient.post("/api/payment/add", {
        customerId: selectedCustomerId,
        amount: parseFloat(amount),
        note: note || undefined,
        payment_mode: mode,
        type,
      });
      if (data.success) {
        setResult(
          type === "payment"
            ? `Payment of ${formatCurrency(data.amount)} recorded! Remaining: ${formatCurrency(data.remainingOutstanding)}`
            : `Credit of ${formatCurrency(data.amount)} added! New balance: ${formatCurrency(data.remainingOutstanding)}`
        );
        setAmount("");
        setNote("");
        setType("payment");
      } else {
        setResult(data.message || "Failed");
      }
    } catch (err: any) {
      setResult("Could not record. Try again.");
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return (
      <div className="px-4 py-5 space-y-4 page-enter">
        <Skeleton className="h-8 w-28 rounded-xl" />
        <Skeleton className="h-48 rounded-2xl" />
        <Skeleton className="h-12 rounded-2xl" />
      </div>
    );
  }

  // Calculate pending for selected customer
  const pendingAmount = db && selectedCustomerId
    ? getCustomerOutstanding(selectedCustomerId, db.transactions, db.bills)
    : 0;

  return (
    <div className="px-4 py-5 space-y-4 page-enter">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-extrabold text-foreground">Payments</h1>
        <Link href="/customers">
          <Button variant="ghost" size="icon" className="h-9 w-9">
            <Users className="h-4 w-4" />
          </Button>
        </Link>
      </div>

      <form onSubmit={handleSubmit} className="space-y-4">
        {/* Customer */}
        <Card>
          <CardContent className="p-4 space-y-3">
            <label className="text-sm font-semibold text-foreground block">
              Select Customer
            </label>
            <select
              value={selectedCustomerId}
              onChange={(e) => setSelectedCustomerId(e.target.value)}
              className="w-full h-11 rounded-xl border border-input bg-white dark:bg-dark-800 px-4 text-sm focus:outline-none focus:ring-2 focus:ring-primary/50"
              required
            >
              <option value="">Choose a customer...</option>
              {customers.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.name} — {formatPhone(c.phone)}
                </option>
              ))}
            </select>
            {selectedCustomerId && (
              <div className={cn(
                "text-xs font-medium px-3 py-2 rounded-lg",
                pendingAmount > 0 ? "bg-red-50 text-red-600 dark:bg-red-900/20 dark:text-red-400" : "bg-green-50 text-green-600 dark:bg-green-900/20 dark:text-green-400"
              )}>
                {pendingAmount > 0 ? `Outstanding: ${formatCurrency(pendingAmount)}` : "All clear!"}
              </div>
            )}
          </CardContent>
        </Card>

        {/* Type Toggle */}
        <div className="grid grid-cols-2 gap-2.5">
          <button
            type="button"
            onClick={() => setType("payment")}
            className={cn(
              "flex items-center justify-center gap-2 p-3.5 rounded-2xl border-2 transition-all",
              type === "payment"
                ? "border-green-500 bg-green-50 text-green-700 dark:bg-green-900/20 dark:text-green-400"
                : "border-border"
            )}
          >
            <ArrowDownRight className="h-4 w-4" />
            <span className="text-sm font-bold">Payment Received</span>
          </button>
          <button
            type="button"
            onClick={() => setType("credit")}
            className={cn(
              "flex items-center justify-center gap-2 p-3.5 rounded-2xl border-2 transition-all",
              type === "credit"
                ? "border-red-500 bg-red-50 text-red-700 dark:bg-red-900/20 dark:text-red-400"
                : "border-border"
            )}
          >
            <ArrowUpRight className="h-4 w-4" />
            <span className="text-sm font-bold">Credit Given</span>
          </button>
        </div>

        {/* Amount */}
        <Card>
          <CardContent className="p-4 space-y-3">
            <label className="text-sm font-semibold text-foreground block">
              Amount
            </label>
            <Input
              type="number"
              value={amount}
              onChange={(e) => setAmount(e.target.value)}
              placeholder="0"
              className="h-12 text-lg font-bold text-center"
              required
              min="0"
              step="0.01"
            />
          </CardContent>
        </Card>

        {/* Mode */}
        <Card>
          <CardContent className="p-4 space-y-3">
            <label className="text-sm font-semibold text-foreground block">
              Payment Mode
            </label>
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-2">
              {modes.map((m) => (
                <button
                  key={m.value}
                  type="button"
                  onClick={() => setMode(m.value)}
                  className={cn(
                    "py-2 rounded-xl text-xs font-semibold transition-all border",
                    mode === m.value
                      ? "border-primary bg-primary/10 text-primary"
                      : "border-border"
                  )}
                >
                  {m.label}
                </button>
              ))}
            </div>
          </CardContent>
        </Card>

        {/* Note */}
        <Card>
          <CardContent className="p-4">
            <label className="text-sm font-semibold text-foreground block mb-2">
              Note (optional)
            </label>
            <textarea
              value={note}
              onChange={(e) => setNote(e.target.value)}
              placeholder="Add a note..."
              className="w-full h-16 rounded-xl border border-input bg-white dark:bg-dark-800 px-4 py-3 text-sm resize-none focus:outline-none focus:ring-2 focus:ring-primary/50"
            />
          </CardContent>
        </Card>

        {result && (
          <p className={cn(
            "text-sm font-medium px-3 py-2 rounded-lg",
            result.includes("recorded") || result.includes("added")
              ? "bg-green-50 text-green-600 dark:bg-green-900/20 dark:text-green-400"
              : "bg-red-50 text-red-500"
          )}>
            {result}
          </p>
        )}

        <Button
          type="submit"
          disabled={submitting || !selectedCustomerId || !amount}
          className="w-full h-12 text-base font-bold"
        >
          {submitting ? (
            <span className="flex items-center gap-2">
              <span className="h-4 w-4 rounded-full border-2 border-white/30 border-t-white animate-spin" />
              Saving...
            </span>
          ) : type === "payment" ? (
            `Record Payment`
          ) : (
            `Record Credit`
          )}
        </Button>
      </form>
    </div>
  );
}
