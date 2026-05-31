"use client";

import { useEffect, useState, useMemo } from "react";
import { useParams, Link } from "next/navigation";
import { apiClient } from "@/lib/api-client";
import { useAuthStore } from "@/stores/auth-store";
import { formatCurrency, formatDate, formatDateTime, cn } from "@/lib/utils";
import { Bill, Customer } from "@/types";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle, Skeleton } from "@/components/ui";
import {
  ChevronLeft,
  Share2,
  Printer,
  CheckCircle2,
  Clock,
  AlertTriangle,
} from "lucide-react";

export default function InvoiceDetailPage() {
  const params = useParams();
  const { store } = useAuthStore();
  const billId = params.id as string;
  const [bill, setBill] = useState<Bill | null>(null);
  const [customer, setCustomer] = useState<Customer | null>(null);
  const [loading, setLoading] = useState(true);
  const [marking, setMarking] = useState(false);

  useEffect(() => {
    const fetchData = async () => {
      if (!store?.id) return;
      try {
        const { data } = await apiClient.get(`/api/db?storeId=${store.id}`);
        const foundBill = (data.bills || []).find((b: Bill) => b.id === billId);
        if (foundBill) {
          setBill(foundBill);
          const foundCustomer = (data.customers || []).find(
            (c: Customer) => c.id === foundBill.customer_id
          );
          if (foundCustomer) setCustomer(foundCustomer);
        }
      } catch (err) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, [store?.id, billId]);

  const statusInfo = useMemo(() => {
    if (!bill) return null;
    if (bill.status === "paid") {
      return {
        variant: "success" as const,
        icon: <CheckCircle2 className="h-3.5 w-3.5" />,
        label: "Paid",
      };
    }
    const isOverdue =
      bill.created_at &&
      new Date(bill.created_at) <
        new Date(Date.now() - 30 * 24 * 60 * 60 * 1000);
    if (isOverdue) {
      return {
        variant: "danger" as const,
        icon: <AlertTriangle className="h-3.5 w-3.5" />,
        label: "Overdue",
      };
    }
    return {
      variant: "warning" as const,
      icon: <Clock className="h-3.5 w-3.5" />,
      label: "Unpaid",
    };
  }, [bill]);

  const handleMarkPaid = async () => {
    if (!bill) return;
    setMarking(true);
    try {
      await apiClient.post("/api/bill/mark-paid", { customerId: bill.customer_id });
      setBill({ ...bill, status: "paid", paid_at: new Date().toISOString() });
    } catch (err: any) {
      alert(err?.response?.data?.message || "Failed to mark as paid");
    } finally {
      setMarking(false);
    }
  };

  if (loading) {
    return (
      <div className="px-4 py-5 space-y-4 page-enter">
        <Skeleton className="h-8 w-24 rounded-xl" />
        <Skeleton className="h-32 rounded-2xl" />
        <Skeleton className="h-48 rounded-2xl" />
      </div>
    );
  }

  if (!bill) {
    return (
      <div className="px-4 py-5 page-enter">
        <Link href="/invoices">
          <Button variant="ghost" className="pl-0">
            <ChevronLeft className="h-4 w-4 mr-1" /> Back
          </Button>
        </Link>
        <Card>
          <CardContent className="py-12 text-center text-muted-foreground">
            Invoice not found
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="px-4 py-5 space-y-4 page-enter">
      <div className="flex items-center justify-between">
        <Link href="/invoices">
          <Button variant="ghost" className="pl-0">
            <ChevronLeft className="h-4 w-4 mr-1" /> Invoices
          </Button>
        </Link>
        {statusInfo && (
          <span
            className={cn(
              "inline-flex items-center gap-1 px-3 py-1 rounded-full text-xs font-bold",
              statusInfo.variant === "success"
                ? "bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-400"
                : statusInfo.variant === "danger"
                ? "bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-400"
                : "bg-amber-100 text-amber-700 dark:bg-amber-900/30 dark:text-amber-400"
            )}
          >
            {statusInfo.icon}
            {statusInfo.label}
          </span>
        )}
      </div>

      {/* Invoice Header */}
      <Card className="bg-gradient-to-br from-primary/5 to-transparent border-primary/10">
        <CardContent className="p-5">
          <div className="flex items-center justify-between mb-3">
            <p className="text-xs text-muted-foreground">Invoice #</p>
            <p className="text-xs font-mono text-muted-foreground">
              {bill.id.slice(0, 12)}
            </p>
          </div>
          <div className="flex items-center gap-3 mb-4">
            <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-primary/10 text-primary font-bold text-sm">
              {customer?.name
                ?.split(" ")
                .map((w) => w[0])
                .join("")
                .slice(0, 2)}
            </div>
            <div>
              <p className="text-sm font-bold text-foreground">
                {customer?.name ?? "Unknown"}
              </p>
              <p className="text-xs text-muted-foreground">
                {formatDate(bill.created_at)}
              </p>
            </div>
          </div>
          <div className="flex items-end justify-between">
            <span className="text-sm text-muted-foreground">Total Amount</span>
            <span className="text-2xl font-extrabold text-foreground">
              {formatCurrency(bill.total)}
            </span>
          </div>
        </CardContent>
      </Card>

      {/* Items */}
      <Card>
        <CardHeader className="pb-3">
          <CardTitle className="text-base">Items</CardTitle>
        </CardHeader>
        <CardContent className="space-y-2.5">
          {bill.items.map((item, idx) => (
            <div
              key={idx}
              className="flex items-center justify-between p-3 rounded-xl bg-accent/30"
            >
              <div className="flex-1 min-w-0">
                <p className="text-sm font-medium text-foreground truncate">
                  {item.name}
                </p>
                <p className="text-xs text-muted-foreground">
                  {item.qty} × {formatCurrency(item.price)}
                </p>
              </div>
              <p className="text-sm font-bold text-foreground ml-2">
                {formatCurrency(item.qty * item.price)}
              </p>
            </div>
          ))}
          {/* Totals breakdown */}
          <div className="space-y-1.5 pt-3 border-t border-border">
            <div className="flex justify-between text-sm">
              <span className="text-muted-foreground">Subtotal</span>
              <span className="font-medium">{formatCurrency(bill.total)}</span>
            </div>
            {bill.grand_total && bill.grand_total !== bill.total && (
              <div className="flex justify-between text-sm">
                <span className="text-muted-foreground">Grand Total (incl. GST)</span>
                <span className="font-bold">{formatCurrency(bill.grand_total)}</span>
              </div>
            )}
          </div>
        </CardContent>
      </Card>

      {bill.paid_at && (
        <p className="text-xs text-center text-muted-foreground">
          Paid on {formatDateTime(bill.paid_at)}
        </p>
      )}

      {/* Actions */}
      <div className="flex gap-2.5 pb-24">
        {bill.status !== "paid" && (
          <Button
            onClick={handleMarkPaid}
            disabled={marking}
            className="flex-1 h-11"
          >
            {marking ? (
              <span className="flex items-center gap-2">
                <span className="h-4 w-4 rounded-full border-2 border-white/30 border-t-white animate-spin" />
                ...
              </span>
            ) : (
              <>
                <CheckCircle2 className="h-4 w-4 mr-1.5" />
                Mark as Paid
              </>
            )}
          </Button>
        )}
        <Button
          variant="outline"
          className="flex-1 h-11"
          onClick={() => window.open(`/api/bill/${billId}/pdf`, "_blank")}
        >
          <Share2 className="h-4 w-4 mr-1.5" />
          Share PDF
        </Button>
      </div>
    </div>
  );
}
