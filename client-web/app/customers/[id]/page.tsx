"use client";

import { useEffect, useState, useMemo } from "react";
import { useParams, useRouter } from "next/navigation";
import Link from "next/link";
import { apiClient } from "@/lib/api-client";
import { useAuthStore } from "@/stores/auth-store";
import { formatCurrency, getCustomerOutstanding, formatPhone, cn } from "@/lib/utils";
import { toast } from "@/lib/use-toast";
import { Customer, Transaction, Bill } from "@/types";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  Skeleton,
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from "@/components/ui";
import {
  ChevronLeft,
  Wallet,
  Share2,
  MessageSquare,
  ArrowDownRight,
  ArrowUpRight,
  FileText,
  Trash2,
  AlertTriangle,
} from "lucide-react";

export default function CustomerDetailPage() {
  const params = useParams();
  const router = useRouter();
  const { store } = useAuthStore();
  const customerId = params.id as string;
  const [customer, setCustomer] = useState<Customer | null>(null);
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [bills, setBills] = useState<Bill[]>([]);
  const [loading, setLoading] = useState(true);
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
  const [deletingCustomer, setDeletingCustomer] = useState(false);
  const [deletingTxId, setDeletingTxId] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState<"ledger" | "transactions" | "bills">("ledger");

  const ledgerEntries = useMemo(() => {
    // Map bills
    const billEntries = bills.map((b) => ({
      id: b.id,
      date: new Date(b.created_at),
      type: "bill" as const,
      label: `Invoice #${b.invoice_number || b.id.slice(0, 8)}`,
      debit: b.total,
      credit: 0,
      refUrl: `/invoices/${b.id}`,
      details: b.items.map(i => `${i.name} (${i.qty}x)`).join(", ")
    }));

    // Map payments
    const paymentEntries = transactions
      .filter((t) => t.type === "payment")
      .map((t) => ({
        id: t.id,
        date: new Date(t.timestamp),
        type: "payment" as const,
        label: `Payment received (${t.payment_mode || 'cash'})`,
        debit: 0,
        credit: t.amount,
        refUrl: null,
        details: t.note || ""
      }));

    // Combine and sort chronologically
    const combined = [...billEntries, ...paymentEntries].sort(
      (a, b) => a.date.getTime() - b.date.getTime()
    );

    // Calculate running balance
    let balance = 0;
    const entriesWithBalance = combined.map((entry) => {
      balance += entry.debit - entry.credit;
      return {
        ...entry,
        runningBalance: balance,
      };
    });

    // Return sorted newest first
    return entriesWithBalance.reverse();
  }, [bills, transactions]);

  useEffect(() => {
    const fetchData = async () => {
      if (!store?.id) return;
      try {
        const { data } = await apiClient.get(`/api/db?storeId=${store.id}`);
        const cust = data.customers?.find((c: Customer) => c.id === customerId);
        if (cust) {
          setCustomer(cust);
          setTransactions((data.transactions || []).filter((t: Transaction) => t.customer_id === customerId));
          setBills((data.bills || []).filter((b: Bill) => b.customer_id === customerId));
        }
      } catch (err) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, [store?.id, customerId]);

  const balance = useMemo(() => {
    if (!customer) return 0;
    return getCustomerOutstanding(customer.id, transactions, bills);
  }, [customer, transactions, bills]);

  const handleSendReminder = async () => {
    try {
      await apiClient.post("/api/whatsapp/send-reminder", { customerId });
    } catch {}
  };

  const handleDeleteCustomer = async () => {
    setDeletingCustomer(true);
    try {
      await apiClient.delete(`/api/customer/${customerId}`);
      router.push("/customers");
    } catch (err: any) {
      toast({ title: "Error", description: err?.response?.data?.message || "Failed to delete customer", variant: "error" });
      setDeletingCustomer(false);
      setShowDeleteConfirm(false);
    }
  };

  const handleDeleteTransaction = async (txId: string) => {
    setDeletingTxId(txId);
    try {
      await apiClient.delete(`/api/transaction/${txId}`);
      setTransactions((prev) => prev.filter((t) => t.id !== txId));
      toast({ title: "Transaction deleted", variant: "success" });
    } catch (err: any) {
      toast({ title: "Error", description: err?.response?.data?.message || "Failed to delete transaction", variant: "error" });
    } finally {
      setDeletingTxId(null);
    }
  };

  if (loading) {
    return (
      <div className="px-4 py-5 space-y-4 page-enter">
        <Skeleton className="h-8 w-20 rounded-xl" />
        <Skeleton className="h-24 rounded-2xl" />
        <Skeleton className="h-48 rounded-2xl" />
      </div>
    );
  }

  if (!customer) {
    return (
      <div className="px-4 py-5 page-enter">
        <Link href="/customers">
          <Button variant="ghost" className="pl-0">
            <ChevronLeft className="h-4 w-4 mr-1" /> Back
          </Button>
        </Link>
        <Card>
          <CardContent className="py-12 text-center text-muted-foreground">
            Customer not found
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="px-4 py-5 space-y-4 page-enter">
      <Link href="/customers">
        <Button variant="ghost" className="pl-0 mb-2">
          <ChevronLeft className="h-4 w-4 mr-1" /> Back to Customers
        </Button>
      </Link>

      <Card>
        <CardContent className="p-5">
          <div className="flex items-center gap-4">
            <div className="flex h-14 w-14 shrink-0 items-center justify-center rounded-2xl bg-primary/10 text-primary font-bold text-lg">
              {customer.name.split(" ").map((w) => w[0]).join("").slice(0, 2)}
            </div>
            <div className="flex-1">
              <h2 className="text-lg font-bold text-foreground">{customer.name}</h2>
              <p className="text-sm text-muted-foreground mt-0.5">{formatPhone(customer.phone)}</p>
            </div>
            <div>
              <span className={cn("text-lg font-extrabold", balance > 0 ? "text-red-500" : "text-green-600")}>
                {formatCurrency(balance)}
              </span>
              <p className="text-[10px] text-muted-foreground">{balance > 0 ? "Outstanding" : "Clear"}</p>
            </div>
          </div>

          <div className="flex gap-2 mt-4">
            <Button variant="outline" className="flex-1 h-9 text-xs" onClick={handleSendReminder}>
              <MessageSquare className="h-3.5 w-3.5 mr-1.5" />
              Remind
            </Button>
            <Button variant="outline" className="flex-1 h-9 text-xs" onClick={() => window.open(`/api/customer/${customerId}/statement/pdf`, "_blank")}>
              <Share2 className="h-3.5 w-3.5 mr-1.5" />
              Statement
            </Button>
            <Link href={`/payments?customerId=${customerId}`} className="flex-1">
              <Button className="w-full h-9 text-xs">
                <Wallet className="h-3.5 w-3.5 mr-1.5" />
                Pay
              </Button>
            </Link>
            <Button
              variant="outline"
              className="h-9 w-9 p-0 text-red-500 border-red-200 dark:border-red-900 hover:bg-red-50 dark:hover:bg-red-900/20"
              onClick={() => setShowDeleteConfirm(true)}
            >
              <Trash2 className="h-3.5 w-3.5" />
            </Button>
          </div>
        </CardContent>
      </Card>

      {/* Tab controls */}
      <div className="flex border-b border-gray-200 dark:border-dark-800 gap-4 mt-2">
        {(["ledger", "transactions", "bills"] as const).map((tab) => (
          <button
            key={tab}
            onClick={() => setActiveTab(tab)}
            className={cn(
              "pb-2 text-sm font-semibold border-b-2 px-1 transition-all capitalize",
              activeTab === tab
                ? "border-primary text-primary"
                : "border-transparent text-muted-foreground hover:text-foreground"
            )}
          >
            {tab}
          </button>
        ))}
      </div>

      {activeTab === "ledger" && (
        <Card>
          <CardHeader className="pb-3">
            <CardTitle className="text-base flex items-center gap-2">Customer Ledger</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            {ledgerEntries.length === 0 ? (
              <p className="text-sm text-center text-muted-foreground py-4">No ledger entries yet</p>
            ) : (
              <div className="relative border-l border-gray-200 dark:border-dark-800 ml-4 pl-6 space-y-6">
                {ledgerEntries.map((entry) => (
                  <div key={entry.id} className="relative group">
                    {/* Circle marker */}
                    <span className={cn(
                      "absolute -left-[31px] top-1.5 flex h-4 w-4 items-center justify-center rounded-full ring-4 ring-white dark:ring-dark-900",
                      entry.type === "bill" ? "bg-red-500" : "bg-green-500"
                    )} />

                    <div className="flex flex-col md:flex-row md:items-center justify-between gap-3 p-3.5 rounded-xl bg-accent/30 hover:bg-accent/40 transition-all">
                      <div className="min-w-0 flex-1">
                        <div className="flex items-center gap-2 flex-wrap">
                          <p className="text-sm font-bold text-foreground truncate">
                            {entry.refUrl ? (
                              <Link href={entry.refUrl} className="hover:underline text-primary">
                                {entry.label}
                              </Link>
                            ) : (
                              entry.label
                            )}
                          </p>
                          <span className="text-[10px] text-muted-foreground">
                            {new Date(entry.date).toLocaleDateString("en-IN", { day: "2-digit", month: "short", year: "numeric", hour: "2-digit", minute: "2-digit" })}
                          </span>
                        </div>
                        <p className="text-xs text-muted-foreground mt-0.5 truncate">{entry.details}</p>
                      </div>

                      <div className="flex items-center gap-6 text-right shrink-0 mt-2 md:mt-0">
                        {/* Debit column */}
                        <div className="w-24">
                          <p className="text-[10px] text-muted-foreground uppercase tracking-wider font-semibold">Debit (Give)</p>
                          <p className="text-sm font-extrabold text-red-500 font-mono-amount">
                            {entry.debit > 0 ? formatCurrency(entry.debit) : "—"}
                          </p>
                        </div>

                        {/* Credit column */}
                        <div className="w-24">
                          <p className="text-[10px] text-muted-foreground uppercase tracking-wider font-semibold">Credit (Got)</p>
                          <p className="text-sm font-extrabold text-green-600 font-mono-amount">
                            {entry.credit > 0 ? formatCurrency(entry.credit) : "—"}
                          </p>
                        </div>

                        {/* Balance column */}
                        <div className="w-28 border-l border-gray-200 dark:border-dark-800 pl-4">
                          <p className="text-[10px] text-muted-foreground uppercase tracking-wider font-semibold">Balance</p>
                          <p className={cn("text-sm font-black font-mono-amount", entry.runningBalance > 0 ? "text-red-500" : "text-green-600")}>
                            {formatCurrency(entry.runningBalance)}
                          </p>
                        </div>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </CardContent>
        </Card>
      )}

      {activeTab === "transactions" && (
        <Card>
          <CardHeader className="pb-3">
            <CardTitle className="text-base">Transactions</CardTitle>
          </CardHeader>
          <CardContent className="space-y-2">
            {transactions.length === 0 ? (
              <p className="text-sm text-center text-muted-foreground py-4">No transactions yet</p>
            ) : (
              [...transactions]
                .sort((a, b) => new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime())
                .map((tx) => (
                  <div key={tx.id} className="flex items-center gap-3 p-3 rounded-xl bg-accent/30 group">
                    <div className={cn("flex h-8 w-8 items-center justify-center rounded-full", tx.type === "payment" ? "bg-green-100 text-green-600 dark:bg-green-900/30" : "bg-red-100 text-red-600 dark:bg-red-900/30")}>
                      {tx.type === "payment" ? <ArrowDownRight className="h-3.5 w-3.5" /> : <ArrowUpRight className="h-3.5 w-3.5" />}
                    </div>
                    <div className="flex-1">
                      <p className="text-sm font-medium">{tx.type === "payment" ? "Payment" : "Credit"}</p>
                      <p className="text-xs text-muted-foreground">{tx.note || tx.payment_mode}</p>
                    </div>
                    <div className="text-right">
                      <p className={cn("text-sm font-bold", tx.type === "payment" ? "text-green-600" : "text-red-500")}>
                        {tx.type === "payment" ? "+" : "-"}{formatCurrency(tx.amount)}
                      </p>
                      <p className="text-[10px] text-muted-foreground">
                        {new Date(tx.timestamp).toLocaleDateString("en-IN", { day: "2-digit", month: "short" })}
                      </p>
                    </div>
                    <button
                      onClick={() => handleDeleteTransaction(tx.id)}
                      disabled={deletingTxId === tx.id}
                      className="shrink-0 h-7 w-7 rounded-full flex items-center justify-center opacity-0 group-hover:opacity-100 hover:bg-red-100 dark:hover:bg-red-900/30 text-muted-foreground hover:text-red-500 transition-all"
                    >
                      {deletingTxId === tx.id ? (
                        <span className="h-3 w-3 rounded-full border-2 border-red-300 border-t-red-500 animate-spin" />
                      ) : (
                        <Trash2 className="h-3 w-3" />
                      )}
                    </button>
                  </div>
                ))
            )}
          </CardContent>
        </Card>
      )}

      {activeTab === "bills" && (
        <Card>
          <CardHeader className="pb-3">
            <CardTitle className="text-base flex items-center gap-2"><FileText className="h-4 w-4" /> Bills</CardTitle>
          </CardHeader>
          <CardContent className="space-y-2">
            {bills.length === 0 ? (
              <p className="text-sm text-center text-muted-foreground py-4">No bills</p>
            ) : (
              [...bills]
                .sort((a, b) => new Date(b.created_at).getTime() - new Date(a.created_at).getTime())
                .map((bill) => (
                  <Link key={bill.id} href={`/invoices/${bill.id}`}>
                    <div className="flex items-center justify-between p-3 rounded-xl bg-accent/30 hover:bg-accent/40 transition-all">
                      <div>
                        <p className="text-sm font-medium">Invoice #{bill.invoice_number || bill.id.slice(0, 8)}</p>
                        <p className="text-xs text-muted-foreground">
                          {new Date(bill.created_at).toLocaleDateString("en-IN", { day: "2-digit", month: "short", year: "numeric" })}
                        </p>
                      </div>
                      <div className="text-right">
                        <p className="text-sm font-bold">{formatCurrency(bill.total)}</p>
                        <span className={cn("text-[10px] font-semibold px-2 py-0.5 rounded-full", bill.status === "paid" ? "bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-400" : "bg-amber-100 text-amber-700 dark:bg-amber-900/30 dark:text-amber-400")}>
                          {bill.status}
                        </span>
                      </div>
                    </div>
                  </Link>
                ))
            )}
          </CardContent>
        </Card>
      )}

      {/* Delete Customer Confirmation Dialog */}
      <Dialog open={showDeleteConfirm} onOpenChange={setShowDeleteConfirm}>
        <DialogContent className="max-w-sm">
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2">
              <AlertTriangle className="h-4 w-4 text-red-500" />
              Delete Customer
            </DialogTitle>
          </DialogHeader>
          <p className="text-sm text-muted-foreground">
            Are you sure you want to delete <strong>{customer.name}</strong>? This will also remove all their transactions and invoices. This action cannot be undone.
          </p>
          <div className="flex gap-3 pt-2">
            <Button
              variant="outline"
              className="flex-1"
              onClick={() => setShowDeleteConfirm(false)}
              disabled={deletingCustomer}
            >
              Cancel
            </Button>
            <Button
              variant="destructive"
              className="flex-1"
              onClick={handleDeleteCustomer}
              disabled={deletingCustomer}
            >
              {deletingCustomer ? (
                <span className="flex items-center gap-2">
                  <span className="h-4 w-4 rounded-full border-2 border-white/30 border-t-white animate-spin" />
                  Deleting...
                </span>
              ) : (
                "Delete Customer"
              )}
            </Button>
          </div>
        </DialogContent>
      </Dialog>
    </div>
  );
}
