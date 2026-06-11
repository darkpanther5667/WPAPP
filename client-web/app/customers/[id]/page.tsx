"use client";

import { useEffect, useState, useMemo } from "react";
import { useParams } from "next/navigation";
import Link from "next/link";
import { apiClient } from "@/lib/api-client";
import { useAuthStore } from "@/stores/auth-store";
import { formatCurrency, getCustomerOutstanding, formatPhone, cn } from "@/lib/utils";
import { Customer, Transaction, Bill } from "@/types";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle, Skeleton } from "@/components/ui";
import {
  ChevronLeft,
  Wallet,
  Share2,
  MessageSquare,
  ArrowDownRight,
  ArrowUpRight,
  FileText,
} from "lucide-react";

export default function CustomerDetailPage() {
  const params = useParams();
  const { store } = useAuthStore();
  const customerId = params.id as string;
  const [customer, setCustomer] = useState<Customer | null>(null);
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [bills, setBills] = useState<Bill[]>([]);
  const [loading, setLoading] = useState(true);

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
          </div>
        </CardContent>
      </Card>

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
                <div key={tx.id} className="flex items-center gap-3 p-3 rounded-xl bg-accent/30">
                  <div className={cn("flex h-8 w-8 items-center justify-center rounded-full", tx.type === "payment" ? "bg-green-100 text-green-600 dark:bg-green-900/30" : "bg-red-100 text-red-600 dark:bg-red-900/30")}>
                    {tx.type === "payment" ? <ArrowDownRight className="h-3.5 w-3.5" /> : <ArrowUpRight className="h-3.5 w-3.5" />}
                  </div>
                  <div className="flex-1">
                    <p className="text-sm font-medium">{tx.type === "payment" ? "Payment" : "Credit"}</p>
                    <p className="text-xs text-muted-foreground">{tx.note || tx.payment_mode}</p>
                  </div>
                  <div>
                    <p className={cn("text-sm font-bold", tx.type === "payment" ? "text-green-600" : "text-red-500")}>
                      {tx.type === "payment" ? "+" : "-"}{formatCurrency(tx.amount)}
                    </p>
                    <p className="text-[10px] text-muted-foreground">
                      {new Date(tx.timestamp).toLocaleDateString("en-IN", { day: "2-digit", month: "short" })}
                    </p>
                  </div>
                </div>
              ))
          )}
        </CardContent>
      </Card>

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
                  <div className="flex items-center justify-between p-3 rounded-xl bg-accent/30">
                    <div>
                      <p className="text-sm font-medium">Invoice #{bill.id.slice(0, 8)}</p>
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
    </div>
  );
}
