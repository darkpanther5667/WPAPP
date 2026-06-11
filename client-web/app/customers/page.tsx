"use client";

import { useEffect, useState, useMemo } from "react";
import Link from "next/link";
import { apiClient } from "@/lib/api-client";
import { useAuthStore } from "@/stores/auth-store";
import { formatCurrency, getCustomerOutstanding, formatPhone, cn } from "@/lib/utils";
import { Customer, Transaction, Bill } from "@/types";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
  Card,
  CardContent,
  Skeleton,
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from "@/components/ui";
import {
  Search,
  Plus,
  UserPlus,
  Phone,
  MessageCircle,
  AlertCircle,
  User,
} from "lucide-react";

export default function CustomersPage() {
  const { store, isAuthenticated } = useAuthStore();
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [bills, setBills] = useState<Bill[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState("");
  const [showAdd, setShowAdd] = useState(false);
  const [newName, setNewName] = useState("");
  const [newPhone, setNewPhone] = useState("");
  const [adding, setAdding] = useState(false);
  const [addError, setAddError] = useState("");

  const fetchData = async () => {
    if (!store?.id) return;
    try {
      const { data } = await apiClient.get(`/api/db?storeId=${store.id}`);
      setCustomers(data.customers || []);
      setTransactions(data.transactions || []);
      setBills(data.bills || []);
    } catch (err: any) {
      // Error shown via empty state
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (isAuthenticated) fetchData();
  }, [isAuthenticated, store?.id]);

  const filtered = useMemo(() => {
    if (!search.trim()) return customers;
    const q = search.toLowerCase();
    return customers.filter(
      (c) =>
        c.name.toLowerCase().includes(q) ||
        (c.phone && c.phone.includes(q))
    );
  }, [customers, search]);

  const handleAdd = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newName.trim() || !newPhone.trim()) return;
    setAdding(true);
    setAddError("");
    try {
      const { data } = await apiClient.post("/api/customer/add", {
        name: newName.trim(),
        phone: newPhone.replace(/\D/g, ""),
      });
      if (data.success) {
        setNewName("");
        setNewPhone("");
        setShowAdd(false);
        await fetchData();
      } else {
        setAddError(data.message || "Failed to add customer");
      }
    } catch (err: any) {
      setAddError("Could not add customer. Try again.");
    } finally {
      setAdding(false);
    }
  };

  if (loading) {
    return (
      <div className="px-4 py-5 space-y-3 page-enter">
        <div className="flex items-center gap-3">
          <Skeleton className="h-10 flex-1 rounded-xl" />
          <Skeleton className="h-10 w-10 rounded-xl" />
        </div>
        {[1, 2, 3, 4, 5].map((i) => (
          <Skeleton key={i} className="h-20 rounded-2xl" />
        ))}
      </div>
    );
  }

  return (
    <div className="px-4 py-5 space-y-4 page-enter">
      {/* Header */}
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-extrabold text-foreground">Customers</h1>
        <Button size="icon" className="h-10 w-10 rounded-full" onClick={() => setShowAdd(true)}>
          <Plus className="h-5 w-5" />
        </Button>
      </div>

      {/* Search */}
      <div className="relative">
        <Search className="absolute left-3.5 top-3.5 h-4 w-4 text-muted-foreground" />
        <Input
          placeholder="Search by name or phone..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          className="pl-10 h-11"
        />
      </div>

      {/* Customer List */}
      {filtered.length === 0 ? (
        <Card>
          <CardContent className="py-12">
            <div className="flex flex-col items-center text-center">
              <div className="p-4 rounded-full bg-primary/10 mb-3">
                <User className="h-8 w-8 text-primary/50" />
              </div>
              <p className="text-sm font-medium text-foreground">
                {search ? "No customers found" : "No customers yet"}
              </p>
              <p className="text-xs text-muted-foreground mt-1">
                {search ? "Try a different search term" : "Add your first customer to get started"}
              </p>
            </div>
          </CardContent>
        </Card>
      ) : (
        <div className="space-y-2.5">
          {filtered.map((c) => {
            const balance = getCustomerOutstanding(c.id, transactions, bills);
            return (
              <Link key={c.id} href={`/customers/${c.id}`}>
                <Card className="hover:shadow-md transition-all cursor-pointer active:scale-[0.98]">
                  <CardContent className="p-4">
                    <div className="flex items-center gap-3">
                      <div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-2xl bg-primary/10 text-primary font-bold text-sm">
                        {c.name.split(" ").map((w) => w[0]).join("").slice(0, 2)}
                      </div>
                      <div className="flex-1 min-w-0">
                        <p className="text-sm font-semibold text-foreground truncate">
                          {c.name}
                        </p>
                        <p className="text-xs text-muted-foreground mt-0.5">
                          {formatPhone(c.phone)}
                        </p>
                      </div>
                      <div className="text-right">
                        <span
                          className={cn(
                            "text-sm font-extrabold",
                            balance > 0 ? "text-red-500" : "text-green-600"
                          )}
                        >
                          {balance > 0 ? formatCurrency(balance) : "Clear"}
                        </span>
                        {balance > 0 && (
                          <p className="text-[10px] text-red-400 mt-0.5">
                            Outstanding
                          </p>
                        )}
                      </div>
                    </div>
                  </CardContent>
                </Card>
              </Link>
            );
          })}
        </div>
      )}

      {/* Add Customer Dialog */}
      <Dialog open={showAdd} onOpenChange={setShowAdd}>
        <DialogContent className="max-w-sm">
          <DialogHeader>
            <DialogTitle>Add New Customer</DialogTitle>
          </DialogHeader>
          <form onSubmit={handleAdd} className="space-y-4 mt-2">
            <div className="space-y-1.5">
              <label className="text-sm font-medium">Full Name</label>
              <div className="relative">
                <User className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
                <Input
                  value={newName}
                  onChange={(e) => setNewName(e.target.value)}
                  placeholder="Rajesh Sharma"
                  className="pl-9 h-11"
                  required
                />
              </div>
            </div>
            <div className="space-y-1.5">
              <label className="text-sm font-medium">Phone Number</label>
              <div className="relative">
                <Phone className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
                <Input
                  value={newPhone}
                  onChange={(e) => setNewPhone(e.target.value)}
                  placeholder="9876543210"
                  className="pl-9 h-11"
                  required
                  pattern="[6-9]{1}[0-9]{9}"
                />
              </div>
            </div>
            {addError && (
              <p className="text-sm text-red-500">{addError}</p>
            )}
            <div className="flex gap-3 pt-1">
              <Button
                type="button"
                variant="outline"
                className="flex-1"
                onClick={() => setShowAdd(false)}
              >
                Cancel
              </Button>
              <Button type="submit" className="flex-1" disabled={adding}>
                {adding ? "Adding..." : "Add Customer"}
              </Button>
            </div>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  );
}
