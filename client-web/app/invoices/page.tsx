"use client";

import { useEffect, useState, useMemo } from "react";
import Link from "next/link";
import { apiClient } from "@/lib/api-client";
import { useAuthStore } from "@/stores/auth-store";
import { formatCurrency, formatDate, cn } from "@/lib/utils";
import { Bill, Customer, Transaction } from "@/types";
import { Button } from "@/components/ui/button";
import { Card, CardContent, Skeleton, Input } from "@/components/ui";
import {
  Plus,
  FileText,
  Search,
  Filter,
  Calendar,
  ChevronRight,
} from "lucide-react";
import { useSearchParams } from "next/navigation";

export default function InvoicesPage() {
  const { store } = useAuthStore();
  const [bills, setBills] = useState<Bill[]>([]);
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState<string>("all");
  const searchParams = useSearchParams();
  const selectedCustomerId = searchParams.get("customerId");

  useEffect(() => {
    const fetchData = async () => {
      if (!store?.id) return;
      try {
        const { data } = await apiClient.get(`/api/db?storeId=${store.id}`);
        setBills(data.bills || []);
        setCustomers(data.customers || []);
        setTransactions(data.transactions || []);
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

  const filtered = useMemo(() => {
    if (selectedCustomerId) {
      return bills.filter((b) => b.customer_id === selectedCustomerId);
    }
    if (filter === "all") return bills;
    // Calculate effective status with overdue
    const result = bills.map((b) => {
      let status = b.status;
      const isOverdue =
        b.status === "unpaid" &&
        b.created_at &&
        new Date(b.created_at) < new Date(Date.now() - 30 * 24 * 60 * 60 * 1000);
      if (isOverdue) status = "overdue";
      return { ...b, effectiveStatus: status };
    });
    return result.filter((b) => b.effectiveStatus === filter);
  }, [bills, filter, selectedCustomerId]);

  const sorted = useMemo(
    () => [...filtered].sort((a, b) => new Date(b.created_at).getTime() - new Date(a.created_at).getTime()),
    [filtered]
  );

  const filterOpts = [
    { value: "all", label: "All" },
    { value: "unpaid", label: "Unpaid" },
    { value: "paid", label: "Paid" },
    { value: "overdue", label: "Overdue" },
  ];

  if (loading) {
    return (
      <div className="px-4 py-5 space-y-3 page-enter">
        <div className="flex items-center justify-between">
          <Skeleton className="h-7 w-36" />
          <Skeleton className="h-9 w-9 rounded-xl" />
        </div>
        <Skeleton className="h-11 rounded-xl" />
        {[1, 2, 3, 4, 5].map((i) => (
          <Skeleton key={i} className="h-20 rounded-2xl" />
        ))}
      </div>
    );
  }

  return (
    <div className="px-4 py-5 space-y-4 page-enter">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-extrabold text-foreground">Invoices</h1>
        <Link href="/invoices/create">
          <Button size="icon" className="h-10 w-10 rounded-full">
            <Plus className="h-5 w-5" />
          </Button>
        </Link>
      </div>

      {/* Search */}
      <div className="relative">
        <Search className="absolute left-3.5 top-3.5 h-4 w-4 text-muted-foreground" />
        <Input
          placeholder="Search invoices..."
          className="pl-10 h-11"
          onChange={(e) => {
            const q = e.target.value.toLowerCase();
            if (q) {
              const found = customers.find(
                (c) => c.name.toLowerCase().includes(q)
              );
              if (found) {
                setFilter("all");
                // navigate to customer detail
              }
            }
          }}
        />
      </div>

      {/* Filter pills */}
      <div className="flex gap-2 overflow-x-auto pb-1">
        {filterOpts.map((opt) => (
          <button
            key={opt.value}
            onClick={() => setFilter(opt.value)}
            className={cn(
              "shrink-0 px-4 py-2 rounded-full text-xs font-semibold transition-all",
              filter === opt.value
                ? "bg-primary text-white shadow-lg shadow-primary/25"
                : "bg-white dark:bg-dark-800 border border-border text-foreground"
            )}
          >
            {opt.label}
          </button>
        ))}
      </div>

      {selectedCustomerId && (
        <div className="rounded-xl bg-primary/10 border border-primary/20 px-4 py-2 text-sm text-primary font-medium flex items-center gap-2">
          <Filter className="h-4 w-4" />
          Showing invoices for{" "}
          {getCustomer(selectedCustomerId)?.name ?? "customer"}
          <Link
            href="/invoices"
            className="ml-auto text-xs underlineunderline-offset-2 font-bold"
          >
            Clear filter
          </Link>
        </div>
      )}

      {sorted.length === 0 ? (
        <Card>
          <CardContent className="py-12">
            <div className="flex flex-col items-center text-center">
              <FileText className="h-10 w-10 text-muted-foreground/30 mb-3" />
              <p className="text-sm font-medium text-foreground">
                {filter === "all" ? "No invoices yet" : `No ${filter} invoices`}
              </p>
              <p className="text-xs text-muted-foreground mt-1">
                Create your first invoice to get started
              </p>
              <Link href="/invoices/create" className="mt-4">
                <Button size="sm">Create Invoice</Button>
              </Link>
            </div>
          </CardContent>
        </Card>
      ) : (
        <div className="space-y-2.5">
          {sorted.map((bill) => {
            const customer = getCustomer(bill.customer_id);
            let status = bill.status;
            if (status === "unpaid") {
              const isOverdue =
                bill.created_at &&
                new Date(bill.created_at) <
                  new Date(Date.now() - 30 * 24 * 60 * 60 * 1000);
              if (isOverdue) status = "overdue";
            }
            return (
              <Link key={bill.id} href={`/invoices/${bill.id}`}>
                <Card className="hover:shadow-md transition-all active:scale-[0.99]">
                  <CardContent className="p-4">
                    <div className="flex items-center justify-between">
                      <div className="flex items-center gap-3">
                        <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-2xl bg-primary/10 text-primary">
                          <FileText className="h-4 w-4" />
                        </div>
                        <div>
                          <p className="text-sm font-semibold text-foreground">
                            Invoice #{bill.id.slice(0, 8)}
                          </p>
                          <p className="text-xs text-muted-foreground mt-0.5">
                            {customer?.name ?? "Unknown"} ·{" "}
                            {formatDate(bill.created_at)}
                          </p>
                        </div>
                      </div>
                      <div className="text-right flex items-center gap-3">
                        <div>
                          <p className="text-sm font-bold text-foreground">
                            {formatCurrency(bill.total)}
                          </p>
                          <span
                            className={cn(
                              "text-[10px] font-bold px-2 py-0.5 rounded-full inline-block mt-0.5",
                              status === "paid"
                                ? "bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-400"
                                : status === "overdue"
                                ? "bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-400"
                                : "bg-amber-100 text-amber-700 dark:bg-amber-900/30 dark:text-amber-400"
                            )}
                          >
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
