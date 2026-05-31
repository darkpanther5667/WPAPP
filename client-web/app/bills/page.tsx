"use client";

import { useEffect, useState, useMemo } from "react";
import Link from "next/link";
import { apiClient } from "@/lib/api-client";
import { useAuthStore } from "@/stores/auth-store";
import { formatCurrency, formatDate, cn } from "@/lib/utils";
import { Bill, Customer } from "@/types";
import { Button } from "@/components/ui/button";
import { Card, CardContent, Skeleton } from "@/components/ui";
import { Plus, FileText, ChevronRight, User } from "lucide-react";

export default function BillsPage() {
  const { store } = useAuthStore();
  const [bills, setBills] = useState<Bill[]>([]);
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      if (!store?.id) return;
      try {
        const { data } = await apiClient.get(`/api/db?storeId=${store.id}`);
        setBills(data.bills || []);
        setCustomers(data.customers || []);
      } catch (err: any) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, [store?.id]);

  const getCustomer = useMemo(
    () => (id: string) => customers.find((c) => c.id === id),
    [customers]
  );

  const sorted = useMemo(
    () => [...bills].sort((a, b) => new Date(b.created_at).getTime() - new Date(a.created_at).getTime()),
    [bills]
  );

  if (loading) {
    return (
      <div className="px-4 py-5 space-y-3 page-enter">
        <Skeleton className="h-7 w-24 rounded-xl" />
        {[1, 2, 3].map((i) => (
          <Skeleton key={i} className="h-20 rounded-2xl" />
        ))}
      </div>
    );
  }

  return (
    <div className="px-4 py-5 space-y-4 page-enter">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-extrabold text-foreground">Bills</h1>
        <Link href="/invoices/create">
          <Button size="icon" className="h-10 w-10 rounded-full">
            <Plus className="h-5 w-5" />
          </Button>
        </Link>
      </div>

      {sorted.length === 0 ? (
        <Card>
          <CardContent className="py-12 text-center">
            <div className="p-4 rounded-full bg-primary/10 mb-3 inline-block">
              <FileText className="h-8 w-8 text-primary/50" />
            </div>
            <p className="text-sm font-medium text-foreground">No bills yet</p>
            <p className="text-xs text-muted-foreground mt-1">Create your first invoice</p>
            <Link href="/invoices/create" className="mt-3 inline-block">
              <Button size="sm">New Invoice</Button>
            </Link>
          </CardContent>
        </Card>
      ) : (
        <div className="space-y-2.5">
          {sorted.map((bill) => {
            const customer = getCustomer(bill.customer_id);
            let status = bill.status;
            if (status === "unpaid" && bill.created_at && new Date(bill.created_at) < new Date(Date.now() - 30 * 24 * 60 * 60 * 1000)) {
              status = "overdue";
            }
            return (
              <Link key={bill.id} href={`/invoices/${bill.id}`}>
                <Card className="hover:shadow-md transition-all active:scale-[0.99]">
                  <CardContent className="p-4">
                    <div className="flex items-center gap-3">
                      <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-2xl bg-primary/10 text-primary">
                        <User className="h-4 w-4" />
                      </div>
                      <div className="flex-1 min-w-0">
                        <p className="text-sm font-semibold text-foreground truncate">
                          {customer?.name ?? "Unknown"}
                        </p>
                        <p className="text-xs text-muted-foreground">
                          {formatDate(bill.created_at)}
                        </p>
                      </div>
                      <div className="text-right flex items-center gap-2">
                        <div>
                          <p className="text-sm font-bold">{formatCurrency(bill.total)}</p>
                          <span className={cn("text-[10px] font-bold px-2 py-0.5 rounded-full inline-block", status === "paid" ? "bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-400" : status === "overdue" ? "bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-400" : "bg-amber-100 text-amber-700 dark:bg-amber-900/30 dark:text-amber-400")}>
                            {status}
                          </span>
                        </div>
                        <ChevronRight className="h-4 w-4 text-muted-foreground" />
                      </div>
                    </div>
                  </CardContent>
                </Card>
              </Link>
            );
          })}
        </div>
      )}
    </div>
  );
}
