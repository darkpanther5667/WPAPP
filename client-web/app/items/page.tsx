"use client";

import { useEffect, useState, useMemo } from "react";
import { apiClient } from "@/lib/api-client";
import { useAuthStore } from "@/stores/auth-store";
import { formatCurrency, cn } from "@/lib/utils";
import { toast } from "@/lib/use-toast";
import { StoredItem } from "@/types";
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
import { Input } from "@/components/ui/input";
import {
  Package,
  Plus,
  Search,
  Trash2,
  Edit3,
  RefreshCw,
  AlertTriangle,
} from "lucide-react";

export default function ItemsPage() {
  const { store } = useAuthStore();
  const [items, setItems] = useState<StoredItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState("");
  const [showAdd, setShowAdd] = useState(false);
  const [editingItem, setEditingItem] = useState<StoredItem | null>(null);

  // Form state
  const [name, setName] = useState("");
  const [price, setPrice] = useState("");
  const [stock, setStock] = useState("");
  const [hsn, setHsn] = useState("");
  const [gstRate, setGstRate] = useState("");
  const [unit, setUnit] = useState("pcs");
  const [saving, setSaving] = useState(false);
  const [deletingId, setDeletingId] = useState<string | null>(null);

  const fetchItems = async () => {
    if (!store?.id) return;
    try {
      const { data } = await apiClient.get(`/api/db?storeId=${store.id}`);
      setItems(data.items || []);
    } catch {
      // handled via empty state
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchItems();
  }, [store?.id]);

  const filtered = useMemo(() => {
    if (!search.trim()) return items;
    const q = search.toLowerCase();
    return items.filter(
      (i) =>
        i.name.toLowerCase().includes(q) ||
        (i.sku && i.sku.toLowerCase().includes(q)) ||
        (i.hsn && i.hsn.includes(q))
    );
  }, [items, search]);

  const lowStockItems = items.filter(
    (i) => i.stock !== undefined && i.stock <= 5
  );

  const resetForm = () => {
    setName("");
    setPrice("");
    setStock("");
    setHsn("");
    setGstRate("");
    setUnit("pcs");
    setEditingItem(null);
  };

  const openEdit = (item: StoredItem) => {
    setEditingItem(item);
    setName(item.name);
    setPrice(String(item.price));
    setStock(String(item.stock ?? 0));
    setHsn(item.hsn || "");
    setGstRate(item.gst_rate ? String(item.gst_rate) : "");
    setUnit(item.unit || "pcs");
    setShowAdd(true);
  };

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim() || !price) return;
    setSaving(true);
    try {
      const payload = {
        name: name.trim(),
        price: parseFloat(price),
        stock: parseInt(stock || "0"),
        hsn: hsn.trim(),
        gst_rate: parseInt(gstRate || "0"),
        unit: unit,
      };

      if (editingItem) {
        await apiClient.post("/api/items/update", { id: editingItem.id, ...payload });
        toast({ title: "Item updated", variant: "success" });
      } else {
        await apiClient.post("/api/items/add", payload);
        toast({ title: "Item added", variant: "success" });
      }

      resetForm();
      setShowAdd(false);
      await fetchItems();
    } catch (err: any) {
      toast({
        title: "Error",
        description: err?.response?.data?.message || "Failed to save item",
        variant: "error",
      });
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (id: string) => {
    if (!confirm("Delete this item? This cannot be undone.")) return;
    setDeletingId(id);
    try {
      await apiClient.delete(`/api/items/delete/${id}`);
      setItems((prev) => prev.filter((i) => i.id !== id));
      toast({ title: "Item deleted", variant: "success" });
    } catch (err: any) {
      toast({
        title: "Error",
        description: err?.response?.data?.message || "Failed to delete item",
        variant: "error",
      });
    } finally {
      setDeletingId(null);
    }
  };

  if (loading) {
    return (
      <div className="px-4 py-5 space-y-4 page-enter">
        <Skeleton className="h-7 w-24 rounded-xl" />
        <Skeleton className="h-11 rounded-xl" />
        {[1, 2, 3, 4].map((i) => (
          <Skeleton key={i} className="h-20 rounded-2xl" />
        ))}
      </div>
    );
  }

  return (
    <div className="px-4 py-5 space-y-4 page-enter">
      {/* Header */}
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-extrabold text-foreground">Inventory</h1>
        <Button
          size="icon"
          className="h-10 w-10 rounded-full"
          onClick={() => {
            resetForm();
            setShowAdd(true);
          }}
        >
          <Plus className="h-5 w-5" />
        </Button>
      </div>

      {/* Low Stock Alert */}
      {lowStockItems.length > 0 && (
        <Card className="border-amber-200 dark:border-amber-900/50 bg-amber-50 dark:bg-amber-900/10">
          <CardContent className="p-4">
            <div className="flex items-center gap-2 mb-2">
              <AlertTriangle className="h-4 w-4 text-amber-500" />
              <p className="text-sm font-semibold text-amber-700 dark:text-amber-400">
                Low Stock Alert
              </p>
            </div>
            <div className="flex flex-wrap gap-2">
              {lowStockItems.map((item) => (
                <span
                  key={item.id}
                  className="px-2.5 py-1 rounded-full bg-amber-100 dark:bg-amber-900/30 text-xs font-medium text-amber-700 dark:text-amber-400"
                >
                  {item.name} ({item.stock} left)
                </span>
              ))}
            </div>
          </CardContent>
        </Card>
      )}

      {/* Search */}
      <div className="relative">
        <Search className="absolute left-3.5 top-3.5 h-4 w-4 text-muted-foreground" />
        <Input
          placeholder="Search by name, SKU, or HSN..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          className="pl-10 h-11"
        />
      </div>

      {/* Stats Summary */}
      <div className="grid grid-cols-3 gap-2.5">
        <Card>
          <CardContent className="p-3 text-center">
            <p className="text-lg font-extrabold text-foreground">{items.length}</p>
            <p className="text-[10px] text-muted-foreground">Total Items</p>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="p-3 text-center">
            <p className="text-lg font-extrabold text-foreground">
              {items.reduce((s, i) => s + (i.stock ?? 0), 0)}
            </p>
            <p className="text-[10px] text-muted-foreground">Total Stock</p>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="p-3 text-center">
            <p className="text-lg font-extrabold text-foreground">{lowStockItems.length}</p>
            <p className="text-[10px] text-muted-foreground">Low Stock</p>
          </CardContent>
        </Card>
      </div>

      {/* Item List */}
      {filtered.length === 0 ? (
        <Card>
          <CardContent className="py-12 text-center">
            <div className="p-4 rounded-full bg-primary/10 mb-3 inline-block">
              <Package className="h-8 w-8 text-primary/50" />
            </div>
            <p className="text-sm font-medium text-foreground">
              {search ? "No items found" : "No items in inventory"}
            </p>
            <p className="text-xs text-muted-foreground mt-1">
              {search
                ? "Try a different search term"
                : "Add your first item to start tracking inventory"}
            </p>
            {!search && (
              <Button
                size="sm"
                className="mt-4"
                onClick={() => {
                  resetForm();
                  setShowAdd(true);
                }}
              >
                Add Item
              </Button>
            )}
          </CardContent>
        </Card>
      ) : (
        <div className="space-y-2">
          {filtered.map((item) => (
            <Card key={item.id} className="group">
              <CardContent className="p-4">
                <div className="flex items-center gap-3">
                  <div className="h-10 w-10 shrink-0 rounded-2xl bg-primary/10 flex items-center justify-center">
                    <Package className="h-5 w-5 text-primary" />
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-semibold text-foreground truncate">
                      {item.name}
                    </p>
                    <p className="text-xs text-muted-foreground flex items-center gap-2">
                      {item.sku && <span>SKU: {item.sku}</span>}
                      {item.hsn && <span>HSN: {item.hsn}</span>}
                      {item.unit && <span>/ {item.unit}</span>}
                    </p>
                  </div>
                  <div className="text-right">
                    <p className="text-sm font-bold font-mono-amount">
                      {formatCurrency(item.price)}
                    </p>
                    <p
                      className={cn(
                        "text-xs font-medium",
                        (item.stock ?? 0) <= 5
                          ? "text-red-500"
                          : "text-green-600"
                      )}
                    >
                      {item.stock ?? 0} in stock
                    </p>
                  </div>
                  <div className="flex gap-1">
                    <button
                      onClick={() => openEdit(item)}
                      className="h-8 w-8 rounded-full flex items-center justify-center opacity-0 group-hover:opacity-100 hover:bg-accent text-muted-foreground hover:text-foreground transition-all"
                    >
                      <Edit3 className="h-3.5 w-3.5" />
                    </button>
                    <button
                      onClick={() => handleDelete(item.id)}
                      disabled={deletingId === item.id}
                      className="h-8 w-8 rounded-full flex items-center justify-center opacity-0 group-hover:opacity-100 hover:bg-red-100 dark:hover:bg-red-900/30 text-muted-foreground hover:text-red-500 transition-all"
                    >
                      {deletingId === item.id ? (
                        <span className="h-3.5 w-3.5 rounded-full border-2 border-red-300 border-t-red-500 animate-spin" />
                      ) : (
                        <Trash2 className="h-3.5 w-3.5" />
                      )}
                    </button>
                  </div>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}

      {/* Add/Edit Item Dialog */}
      <Dialog
        open={showAdd}
        onOpenChange={(open) => {
          if (!open) resetForm();
          setShowAdd(open);
        }}
      >
        <DialogContent className="max-w-sm">
          <DialogHeader>
            <DialogTitle>
              {editingItem ? "Edit Item" : "Add Item"}
            </DialogTitle>
          </DialogHeader>
          <form onSubmit={handleSave} className="space-y-4 mt-2">
            <div className="space-y-1.5">
              <label className="text-sm font-medium">Item Name *</label>
              <Input
                value={name}
                onChange={(e) => setName(e.target.value)}
                placeholder="e.g. Wheat Flour (Atta)"
                required
              />
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div className="space-y-1.5">
                <label className="text-sm font-medium">Price (₹) *</label>
                <Input
                  type="number"
                  step="0.01"
                  min="0"
                  value={price}
                  onChange={(e) => setPrice(e.target.value)}
                  placeholder="40"
                  required
                />
              </div>
              <div className="space-y-1.5">
                <label className="text-sm font-medium">Stock</label>
                <Input
                  type="number"
                  min="0"
                  value={stock}
                  onChange={(e) => setStock(e.target.value)}
                  placeholder="100"
                />
              </div>
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div className="space-y-1.5">
                <label className="text-sm font-medium">HSN Code</label>
                <Input
                  value={hsn}
                  onChange={(e) => setHsn(e.target.value)}
                  placeholder="1101"
                />
              </div>
              <div className="space-y-1.5">
                <label className="text-sm font-medium">GST Rate (%)</label>
                <Input
                  type="number"
                  min="0"
                  max="28"
                  value={gstRate}
                  onChange={(e) => setGstRate(e.target.value)}
                  placeholder="5"
                />
              </div>
            </div>
            <div className="space-y-1.5">
              <label className="text-sm font-medium">Unit</label>
              <div className="flex gap-2">
                {["pcs", "kg", "g", "l", "ml", "m", "box", "pack"].map(
                  (u) => (
                    <button
                      key={u}
                      type="button"
                      onClick={() => setUnit(u)}
                      className={cn(
                        "px-3 py-1.5 rounded-full text-xs font-medium transition-all",
                        unit === u
                          ? "bg-primary text-white shadow-sm"
                          : "bg-accent/50 text-muted-foreground hover:bg-accent"
                      )}
                    >
                      {u}
                    </button>
                  )
                )}
              </div>
            </div>
            <div className="flex gap-3 pt-1">
              <Button
                type="button"
                variant="outline"
                className="flex-1"
                onClick={() => {
                  resetForm();
                  setShowAdd(false);
                }}
              >
                Cancel
              </Button>
              <Button type="submit" className="flex-1" disabled={saving}>
                {saving
                  ? "Saving..."
                  : editingItem
                  ? "Update Item"
                  : "Add Item"}
              </Button>
            </div>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  );
}
