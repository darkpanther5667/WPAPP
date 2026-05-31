"use client";

import { useEffect, useState, useMemo } from "react";
import { useRouter } from "next/navigation";
import { apiClient } from "@/lib/auth-store";
import { useAuthStore } from "@/stores/auth-store";
import { formatCurrency, cn } from "@/lib/utils";
import { Customer, BillItem } from "@/types";
import { Button } from "@/components/ui/button";
import { Card, CardContent, Input, Skeleton, Badge } from "@/components/ui";
import { ChevronLeft, Trash2, Plus, ShoppingCart, User } from "lucide-react";

export default function CreateInvoicePage() {
  const router = useRouter();
  const { store } = useAuthStore();
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [selectedCustomerId, setSelectedCustomerId] = useState("");
  const [items, setItems] = useState<BillItem[]>([
    { name: "", qty: 1, price: 0 },
  ]);
  const [notes, setNotes] = useState("");
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    const fetchData = async () => {
      if (!store?.id) return;
      try {
        const { data } = await apiClient.get(`/api/db?storeId=${store.id}`);
        setCustomers(data.customers || []);
      } catch (err: any) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, [store?.id]);

  const addItem = () => {
    setItems([...items, { name: "", qty: 1, price: 0 }]);
  };

  const removeItem = (idx: number) => {
    if (items.length > 1) setItems(items.filter((_, i) => i !== idx));
  };

  const updateItem = (idx: number, field: keyof BillItem, value: string | number) => {
    const updated = [...items];
    updated[idx] = { ...updated[idx], [field]: value };
    setItems(updated);
  };

  const total = useMemo(
    () => items.reduce((s, it) => s + it.qty * it.price, 0),
    [items]
  );

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedCustomerId) {
      setError("Please select a customer");
      return;
    }
    if (items.some((it) => !it.name.trim() || it.price <= 0)) {
      setError("Please fill all item details");
      return;
    }
    setSubmitting(true);
    setError("");
    try {
      const { data } = await apiClient.post("/api/bill/create", {
        customerId: selectedCustomerId,
        amount: total,
        items: items.filter((it) => it.name.trim()),
      });
      if (data.success) {
        router.push(`/invoices/${data.billId || data.id}`);
      } else {
        setError(data.message || "Failed to create invoice");
      }
    } catch (err: any) {
      setError("Could not create invoice. Try again.");
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return (
      <div className="px-4 py-5 space-y-4 page-enter">
        <Skeleton className="h-8 w-24 rounded-xl" />
        <Skeleton className="h-14 rounded-2xl" />
        {[1, 2].map((i) => (
          <Skeleton key={i} className="h-24 rounded-2xl" />
        ))}
        <Skeleton className="h-12 rounded-2xl" />
      </div>
    );
  }

  return (
    <div className="px-4 py-5 space-y-4 page-enter">
      <Link href="/invoices">
        <Button variant="ghost" className="pl-0">
          <ChevronLeft className="h-4 w-4 mr-1" /> Invoices
        </Button>
      </Link>

      <h1 className="text-2xl font-extrabold text-foreground">Create Invoice</h1>

      {error && (
        <p className="text-sm text-red-500 bg-red-50 dark:bg-red-900/20 px-3 py-2 rounded-lg">
          {error}
        </p>
      )}

      {/* Customer Select */}
      <Card>
        <CardContent className="p-4">
          <label className="text-sm font-semibold text-foreground block mb-2">
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
                {c.name} — {c.phone}
              </option>
            ))}
          </select>
        </CardContent>
      </Card>

      {/* Items */}
      <Card>
        <CardContent className="p-4 space-y-3">
          <div className="flex items-center gap-2">
            <ShoppingCart className="h-4 w-4 text-primary" />
            <h2 className="text-sm font-semibold text-foreground">Items</h2>
          </div>
          {items.map((item, idx) => (
            <div key={idx} className="grid grid-cols-[1fr_60px_80px_36px] gap-2 items-center">
              <Input
                placeholder="Item name"
                value={item.name}
                onChange={(e: React.ChangeEvent<HTMLInputElement>) =>
                  updateItem(idx, "name", e.target.value)
                }
                className="h-9 text-xs"
              />
              <Input
                type="number"
                min="1"
                placeholder="Qty"
                value={item.qty}
                onChange={(e: React.ChangeEvent<HTMLInputElement>) =>
                  updateItem(idx, "qty", parseInt(e.target.value) || 1)
                }
                className="h-9 text-xs"
              />
              <Input
                type="number"
                min="0"
                placeholder="Price"
                value={item.price}
                onChange={(e: React.ChangeEvent<HTMLInputElement>) =>
                  updateItem(idx, "price", parseFloat(e.target.value) || 0)
                }
                className="h-9 text-xs"
              />
              <button
                type="button"
                onClick={() => removeItem(idx)}
                className="h-9 w-9 flex items-center justify-center rounded-lg text-red-400 hover:text-red-600 hover:bg-red-50 dark:hover:bg-red-900/20 transition-colors"
              >
                <Trash2 className="h-3.5 w-3.5" />
              </button>
            </div>
          ))}

          <button
            type="button"
            onClick={addItem}
            className="flex items-center gap-1.5 text-xs font-semibold text-primary hover:text-primary/80 transition-colors"
          >
            <Plus className="h-3.5 w-3.5" /> Add Item
          </button>

          {/* Total */}
          <div className="flex items-center justify-between pt-2 border-t border-border">
            <span className="text-sm font-medium text-muted-foreground">
              Total
            </span>
            <span className="text-lg font-extrabold text-foreground">
              {formatCurrency(total)}
            </span>
          </div>
        </CardContent>
      </Card>

      <form onSubmit={handleSubmit}>
        <Button
          type="submit"
          className="w-full h-12 text-base font-bold"
          disabled={submitting}
        >
          {submitting ? (
            <span className="flex items-center gap-2">
              <span className="h-4 w-4 rounded-full border-2 border-white/30 border-t-white animate-spin" />
              Creating...
            </span>
          ) : (
            "Create Invoice"
          )}
        </Button>
      </form>
    </div>
  );
}
