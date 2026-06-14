"use client";

import { useEffect, useState } from "react";
import { apiClient } from "@/lib/api-client";
import { useAuthStore } from "@/stores/auth-store";
import { formatCurrency, formatDate, cn } from "@/lib/utils";
import { toast } from "@/lib/use-toast";
import { Purchase, PurchaseItem } from "@/types";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  Skeleton,
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from "@/components/ui";
import { Input } from "@/components/ui/input";
import {
  Plus,
  Trash2,
  ShoppingCart,
  RefreshCw,
  Search,
  ChevronDown,
  ChevronUp,
} from "lucide-react";

interface LocalPurchaseItem {
  name: string;
  qty: string;
  price: string;
}

export default function PurchasesPage() {
  const { store } = useAuthStore();
  const [purchases, setPurchases] = useState<Purchase[]>([]);
  const [loading, setLoading] = useState(true);
  const [showAdd, setShowAdd] = useState(false);
  const [search, setSearch] = useState("");

  // Form states
  const [supplierName, setSupplierName] = useState("");
  const [supplierPhone, setSupplierPhone] = useState("");
  const [paidAmount, setPaidAmount] = useState("");
  const [notes, setNotes] = useState("");
  const [items, setItems] = useState<LocalPurchaseItem[]>([{ name: "", qty: "1", price: "" }]);
  const [adding, setAdding] = useState(false);
  const [deletingId, setDeletingId] = useState<string | null>(null);
  const [expandedId, setExpandedId] = useState<string | null>(null);

  const fetchPurchases = async () => {
    if (!store?.id) return;
    try {
      const { data } = await apiClient.get(`/api/db?storeId=${store.id}`);
      setPurchases(data.purchases || []);
    } catch {
      // handled via empty state
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchPurchases();
  }, [store?.id]);

  // Calculations
  const calculatedTotalAmount = items.reduce((sum, item) => {
    const qty = parseInt(item.qty) || 0;
    const price = parseFloat(item.price) || 0;
    return sum + qty * price;
  }, 0);

  const totalPurchases = purchases.reduce((s, p) => s + p.totalAmount, 0);
  const totalPendingPurchases = purchases.reduce((s, p) => s + (p.totalAmount - p.paidAmount), 0);

  const handleAddItemRow = () => {
    setItems([...items, { name: "", qty: "1", price: "" }]);
  };

  const handleRemoveItemRow = (index: number) => {
    if (items.length === 1) return;
    setItems(items.filter((_, i) => i !== index));
  };

  const handleItemChange = (index: number, field: keyof LocalPurchaseItem, value: string) => {
    const updated = [...items];
    updated[index] = { ...updated[index], [field]: value };
    setItems(updated);
  };

  const handleAdd = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!supplierName.trim() || calculatedTotalAmount <= 0) {
      toast({
        title: "Validation Error",
        description: "Supplier name is required, and total amount must be greater than zero.",
        variant: "error",
      });
      return;
    }
    setAdding(true);
    try {
      const purchaseItems = items
        .filter((item) => item.name.trim() !== "")
        .map((item) => {
          const qty = parseInt(item.qty) || 1;
          const price = parseFloat(item.price) || 0;
          return {
            name: item.name.trim(),
            qty,
            price,
            amount: qty * price,
          };
        });

      await apiClient.post("/api/purchases/add", {
        supplierName: supplierName.trim(),
        supplierPhone: supplierPhone.trim(),
        totalAmount: calculatedTotalAmount,
        paidAmount: parseFloat(paidAmount) || 0,
        status: parseFloat(paidAmount) >= calculatedTotalAmount ? "paid" : parseFloat(paidAmount) > 0 ? "partial" : "unpaid",
        items: purchaseItems,
        notes: notes.trim(),
      });

      setSupplierName("");
      setSupplierPhone("");
      setPaidAmount("");
      setNotes("");
      setItems([{ name: "", qty: "1", price: "" }]);
      setShowAdd(false);
      toast({ title: "Purchase recorded successfully", variant: "success" });
      await fetchPurchases();
    } catch (err: any) {
      toast({
        title: "Error",
        description: err?.response?.data?.message || "Failed to record purchase",
        variant: "error",
      });
    } finally {
      setAdding(false);
    }
  };

  const handleDelete = async (id: string, e: React.MouseEvent) => {
    e.stopPropagation();
    if (!confirm("Are you sure you want to delete this purchase?")) return;
    setDeletingId(id);
    try {
      await apiClient.delete(`/api/purchases/${id}`);
      setPurchases((prev) => prev.filter((p) => p.id !== id));
      toast({ title: "Purchase deleted successfully", variant: "success" });
    } catch (err: any) {
      toast({
        title: "Error",
        description: err?.response?.data?.message || "Failed to delete purchase",
        variant: "error",
      });
    } finally {
      setDeletingId(null);
    }
  };

  const filtered = purchases.filter((p) =>
    p.supplierName.toLowerCase().includes(search.toLowerCase())
  );

  const sorted = [...filtered].sort(
    (a, b) => new Date(b.created_at).getTime() - new Date(a.created_at).getTime()
  );

  const toggleExpand = (id: string) => {
    setExpandedId(expandedId === id ? null : id);
  };

  if (loading) {
    return (
      <div className="px-4 py-5 space-y-4 page-enter">
        <Skeleton className="h-7 w-24 rounded-xl" />
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <Skeleton className="h-24 rounded-2xl" />
          <Skeleton className="h-24 rounded-2xl" />
        </div>
        {[1, 2, 3].map((i) => (
          <Skeleton key={i} className="h-16 rounded-2xl" />
        ))}
      </div>
    );
  }

  return (
    <div className="px-4 py-5 space-y-4 page-enter">
      {/* Header */}
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-extrabold text-foreground">Supplier Purchases</h1>
        <Button
          size="icon"
          className="h-10 w-10 rounded-full"
          onClick={() => setShowAdd(true)}
        >
          <Plus className="h-5 w-5" />
        </Button>
      </div>

      {/* Summary Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <Card className="bg-gradient-to-br from-blue-50 to-transparent dark:from-blue-900/10 border-blue-100 dark:border-blue-900/20">
          <CardContent className="p-5">
            <div className="flex items-center gap-3">
              <div className="h-10 w-10 rounded-xl bg-blue-100 dark:bg-blue-900/30 flex items-center justify-center">
                <ShoppingCart className="h-5 w-5 text-blue-500" />
              </div>
              <div>
                <p className="text-xs text-muted-foreground font-medium">Total Billed Purchases</p>
                <p className="text-2xl font-extrabold text-blue-500 font-mono-amount">
                  {formatCurrency(totalPurchases)}
                </p>
              </div>
            </div>
          </CardContent>
        </Card>

        <Card className="bg-gradient-to-br from-amber-50 to-transparent dark:from-amber-900/10 border-amber-100 dark:border-amber-900/20">
          <CardContent className="p-5">
            <div className="flex items-center gap-3">
              <div className="h-10 w-10 rounded-xl bg-amber-100 dark:bg-amber-900/30 flex items-center justify-center">
                <ShoppingCart className="h-5 w-5 text-amber-500" />
              </div>
              <div>
                <p className="text-xs text-muted-foreground font-medium">Total Outstanding Balance</p>
                <p className="text-2xl font-extrabold text-amber-500 font-mono-amount">
                  {formatCurrency(totalPendingPurchases)}
                </p>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Filter / Search */}
      <div className="relative">
        <Search className="absolute left-3 top-2.5 h-4 w-4 text-muted-foreground" />
        <Input
          placeholder="Search supplier..."
          className="pl-9"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />
      </div>

      {/* Purchases List */}
      {sorted.length === 0 ? (
        <Card>
          <CardContent className="py-12 text-center">
            <div className="p-4 rounded-full bg-primary/10 mb-3 inline-block">
              <ShoppingCart className="h-8 w-8 text-primary/50" />
            </div>
            <p className="text-sm font-medium text-foreground">No purchases found</p>
            <p className="text-xs text-muted-foreground mt-1">
              Add purchases to track supplier bills and inventory costs
            </p>
            <Button size="sm" className="mt-4" onClick={() => setShowAdd(true)}>
              Add Purchase
            </Button>
          </CardContent>
        </Card>
      ) : (
        <div className="space-y-2">
          {sorted.map((purchase) => {
            const isExpanded = expandedId === purchase.id;
            const balance = purchase.totalAmount - purchase.paidAmount;

            return (
              <Card
                key={purchase.id}
                className="group cursor-pointer hover:border-primary/20 transition-all"
                onClick={() => toggleExpand(purchase.id)}
              >
                <CardContent className="p-4 space-y-3">
                  <div className="flex items-center justify-between gap-3">
                    <div className="min-w-0 flex-1">
                      <p className="text-sm font-bold text-foreground truncate">
                        {purchase.supplierName}
                      </p>
                      <p className="text-xs text-muted-foreground flex items-center gap-1.5 mt-0.5">
                        <span>{formatDate(purchase.created_at)}</span>
                        {purchase.supplierPhone && (
                          <>
                            <span>·</span>
                            <span>{purchase.supplierPhone}</span>
                          </>
                        )}
                        <span
                          className={cn(
                            "ml-1 px-1.5 py-0.5 rounded-full text-[10px] font-semibold",
                            purchase.status === "paid"
                              ? "bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-400"
                              : purchase.status === "partial"
                              ? "bg-amber-100 text-amber-700 dark:bg-amber-900/30 dark:text-amber-400"
                              : "bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-400"
                          )}
                        >
                          {purchase.status.toUpperCase()}
                        </span>
                      </p>
                    </div>

                    <div className="text-right flex items-center gap-3">
                      <div>
                        <p className="text-sm font-bold text-foreground font-mono-amount">
                          {formatCurrency(purchase.totalAmount)}
                        </p>
                        {balance > 0 && (
                          <p className="text-[10px] text-amber-600 font-semibold font-mono-amount">
                            Bal: {formatCurrency(balance)}
                          </p>
                        )}
                      </div>
                      <div className="text-muted-foreground">
                        {isExpanded ? <ChevronUp className="h-4 w-4" /> : <ChevronDown className="h-4 w-4" />}
                      </div>
                      <button
                        onClick={(e) => handleDelete(purchase.id, e)}
                        disabled={deletingId === purchase.id}
                        className="shrink-0 h-8 w-8 rounded-full flex items-center justify-center opacity-0 group-hover:opacity-100 hover:bg-red-100 dark:hover:bg-red-900/30 text-muted-foreground hover:text-red-500 transition-all"
                      >
                        {deletingId === purchase.id ? (
                          <span className="h-3.5 w-3.5 rounded-full border-2 border-red-300 border-t-red-500 animate-spin" />
                        ) : (
                          <Trash2 className="h-3.5 w-3.5" />
                        )}
                      </button>
                    </div>
                  </div>

                  {isExpanded && (
                    <div className="pt-2 border-t border-gray-100 dark:border-dark-800 space-y-2 text-xs page-enter">
                      {purchase.items && purchase.items.length > 0 && (
                        <div className="space-y-1 bg-accent/20 p-2.5 rounded-xl">
                          <p className="font-semibold text-muted-foreground mb-1 text-[10px] uppercase tracking-wider">Items</p>
                          {purchase.items.map((item, idx) => (
                            <div key={idx} className="flex justify-between items-center text-foreground py-0.5">
                              <span>{item.name} ({item.qty} x {formatCurrency(item.price)})</span>
                              <span className="font-bold font-mono-amount">{formatCurrency(item.amount)}</span>
                            </div>
                          ))}
                        </div>
                      )}
                      {purchase.notes && (
                        <div className="p-2.5 rounded-xl bg-muted/30">
                          <p className="font-semibold text-muted-foreground text-[10px] uppercase tracking-wider mb-0.5">Notes</p>
                          <p className="text-muted-foreground italic">{purchase.notes}</p>
                        </div>
                      )}
                    </div>
                  )}
                </CardContent>
              </Card>
            );
          })}
        </div>
      )}

      {/* Add Purchase Dialog */}
      <Dialog open={showAdd} onOpenChange={setShowAdd}>
        <DialogContent className="max-w-md max-h-[90vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle>Add Supplier Purchase</DialogTitle>
          </DialogHeader>
          <form onSubmit={handleAdd} className="space-y-4 mt-2">
            <div className="space-y-1.5">
              <label className="text-sm font-medium">Supplier Name</label>
              <Input
                value={supplierName}
                onChange={(e) => setSupplierName(e.target.value)}
                placeholder="Supplier or Wholesaler Name"
                required
              />
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div className="space-y-1.5">
                <label className="text-sm font-medium">Supplier Phone</label>
                <Input
                  value={supplierPhone}
                  onChange={(e) => setSupplierPhone(e.target.value)}
                  placeholder="e.g. 9876543210"
                />
              </div>
              <div className="space-y-1.5">
                <label className="text-sm font-medium">Amount Paid (₹)</label>
                <Input
                  type="number"
                  step="0.01"
                  min="0"
                  value={paidAmount}
                  onChange={(e) => setPaidAmount(e.target.value)}
                  placeholder="0.00"
                />
              </div>
            </div>

            {/* Dynamic Items List */}
            <div className="space-y-2">
              <div className="flex justify-between items-center">
                <label className="text-sm font-semibold text-foreground">Purchase Items</label>
                <Button type="button" variant="outline" size="sm" onClick={handleAddItemRow}>
                  Add Item
                </Button>
              </div>
              <div className="space-y-2 max-h-[200px] overflow-y-auto pr-1">
                {items.map((item, idx) => (
                  <div key={idx} className="flex gap-2 items-center">
                    <Input
                      placeholder="Item Name"
                      className="flex-[2]"
                      value={item.name}
                      onChange={(e) => handleItemChange(idx, "name", e.target.value)}
                      required={idx === 0}
                    />
                    <Input
                      type="number"
                      placeholder="Qty"
                      className="flex-[0.7]"
                      min="1"
                      value={item.qty}
                      onChange={(e) => handleItemChange(idx, "qty", e.target.value)}
                    />
                    <Input
                      type="number"
                      step="0.01"
                      placeholder="Price"
                      className="flex-[1.2]"
                      min="0"
                      value={item.price}
                      onChange={(e) => handleItemChange(idx, "price", e.target.value)}
                      required={idx === 0}
                    />
                    {items.length > 1 && (
                      <Button
                        type="button"
                        variant="destructive"
                        size="icon"
                        className="h-9 w-9 shrink-0"
                        onClick={() => handleRemoveItemRow(idx)}
                      >
                        <Trash2 className="h-4 w-4" />
                      </Button>
                    )}
                  </div>
                ))}
              </div>
            </div>

            {/* Total Amount Panel */}
            <div className="p-3 bg-accent/30 rounded-xl flex justify-between items-center text-sm">
              <span className="font-semibold">Calculated Total:</span>
              <span className="font-extrabold text-primary font-mono-amount">{formatCurrency(calculatedTotalAmount)}</span>
            </div>

            <div className="space-y-1.5">
              <label className="text-sm font-medium">Notes (optional)</label>
              <Input
                value={notes}
                onChange={(e) => setNotes(e.target.value)}
                placeholder="Purchase details or invoice copy number..."
              />
            </div>

            <div className="flex gap-3 pt-2">
              <Button
                type="button"
                variant="outline"
                className="flex-1"
                onClick={() => setShowAdd(false)}
              >
                Cancel
              </Button>
              <Button type="submit" className="flex-1" disabled={adding}>
                {adding ? "Adding..." : "Record Purchase"}
              </Button>
            </div>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  );
}
