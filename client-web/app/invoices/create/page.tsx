"use client";

import { useEffect, useState, useMemo } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { apiClient } from "@/lib/api-client";
import { useAuthStore } from "@/stores/auth-store";
import { formatCurrency } from "@/lib/utils";
import { Customer, BillItem } from "@/types";
import { Button } from "@/components/ui/button";
import { Card, CardContent, Input, Skeleton } from "@/components/ui";
import { ChevronLeft, Trash2, Plus, ShoppingCart, Hash } from "lucide-react";

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

  // GST & Discount State
  const [gstType, setGstType] = useState<"none" | "cgst_sgst" | "igst">("none");
  const [gstRate, setGstRate] = useState<number>(18);
  const [invoiceNumber, setInvoiceNumber] = useState("");
  const [discount, setDiscount] = useState<number>(0);

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

  // Dynamically calculate taxable, GST, and totals for each item
  const calculatedItems = useMemo(() => {
    return items.map((item) => {
      const taxable = item.qty * item.price;
      let cgst = 0;
      let sgst = 0;
      let igst = 0;
      if (gstType === "cgst_sgst") {
        cgst = (taxable * (gstRate / 2)) / 100;
        sgst = cgst;
      } else if (gstType === "igst") {
        igst = (taxable * gstRate) / 100;
      }
      const total_with_tax = taxable + cgst + sgst + igst;
      return {
        ...item,
        gst_rate: gstRate,
        taxable,
        cgst,
        sgst,
        igst,
        total_with_tax,
      };
    });
  }, [items, gstType, gstRate]);

  const subtotal = useMemo(
    () => calculatedItems.reduce((s, it) => s + it.taxable, 0),
    [calculatedItems]
  );

  const totalCgst = useMemo(
    () => calculatedItems.reduce((s, it) => s + it.cgst, 0),
    [calculatedItems]
  );

  const totalSgst = useMemo(
    () => calculatedItems.reduce((s, it) => s + it.sgst, 0),
    [calculatedItems]
  );

  const totalIgst = useMemo(
    () => calculatedItems.reduce((s, it) => s + it.igst, 0),
    [calculatedItems]
  );

  const totalGst = totalCgst + totalSgst + totalIgst;

  const grandTotal = useMemo(
    () => Math.max(0, subtotal + totalGst - discount),
    [subtotal, totalGst, discount]
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
      const payload = {
        customerId: selectedCustomerId,
        amount: grandTotal,
        items: calculatedItems.filter((it) => it.name.trim()),
        discount,
        invoice_number: invoiceNumber || undefined,
        gst_enabled: gstType !== "none",
        gst_type: gstType !== "none" ? gstType : undefined,
        gst_rate: gstRate,
        taxable_amount: subtotal,
        total_cgst: totalCgst,
        total_sgst: totalSgst,
        total_igst: totalIgst,
        grand_total: grandTotal,
        notes: notes || undefined,
      };

      const { data } = await apiClient.post("/api/bill/create", payload);
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
            className="w-full h-11 rounded-xl border border-input bg-white dark:bg-dark-800 px-4 text-sm focus:outline-none focus:ring-2 focus:ring-primary/50 text-foreground"
            required
          >
            <option value="">Choose a customer...</option>
            <option value="walk-in">Walk-in Customer (One-Off)</option>
            {customers.map((c) => (
              <option key={c.id} value={c.id}>
                {c.name} — {c.phone}
              </option>
            ))}
          </select>
        </CardContent>
      </Card>

      {/* Invoice Details: Custom Invoice Number & Discount & GST */}
      <Card>
        <CardContent className="p-4 space-y-4">
          <div className="flex items-center gap-2">
            <Hash className="h-4 w-4 text-primary" />
            <h2 className="text-sm font-semibold text-foreground">Invoice Settings</h2>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="text-xs font-semibold text-muted-foreground block mb-1">
                Custom Invoice Number (Optional)
              </label>
              <Input
                placeholder="e.g. INV-1002"
                value={invoiceNumber}
                onChange={(e) => setInvoiceNumber(e.target.value)}
                className="h-10 text-sm"
              />
            </div>

            <div>
              <label className="text-xs font-semibold text-muted-foreground block mb-1">
                Discount Amount (₹)
              </label>
              <Input
                type="number"
                min="0"
                placeholder="e.g. 100"
                value={discount || ""}
                onChange={(e) => setDiscount(parseFloat(e.target.value) || 0)}
                className="h-10 text-sm"
              />
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 pt-2">
            <div>
              <label className="text-xs font-semibold text-muted-foreground block mb-1">
                GST Type
              </label>
              <select
                value={gstType}
                onChange={(e) => setGstType(e.target.value as any)}
                className="w-full h-10 rounded-lg border border-input bg-white dark:bg-dark-800 px-3 text-sm focus:outline-none focus:ring-2 focus:ring-primary/50 text-foreground"
              >
                <option value="none">No GST</option>
                <option value="cgst_sgst">CGST + SGST (Intra-State)</option>
                <option value="igst">IGST (Inter-State)</option>
              </select>
            </div>

            {gstType !== "none" && (
              <div>
                <label className="text-xs font-semibold text-muted-foreground block mb-1">
                  GST Rate (%)
                </label>
                <select
                  value={gstRate}
                  onChange={(e) => setGstRate(parseInt(e.target.value) || 0)}
                  className="w-full h-10 rounded-lg border border-input bg-white dark:bg-dark-800 px-3 text-sm focus:outline-none focus:ring-2 focus:ring-primary/50 text-foreground"
                >
                  <option value={0}>0%</option>
                  <option value={5}>5%</option>
                  <option value={12}>12%</option>
                  <option value={18}>18%</option>
                  <option value={28}>28%</option>
                </select>
              </div>
            )}
          </div>
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
            <div key={idx} className="grid grid-cols-[1fr_60px_80px_36px] sm:grid-cols-[1fr_60px_80px_36px] gap-2 items-center">
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

          {/* Calculations Summary */}
          <div className="pt-4 border-t border-border space-y-2 text-sm text-muted-foreground">
            <div className="flex items-center justify-between">
              <span>Taxable Amount</span>
              <span className="font-semibold text-foreground">{formatCurrency(subtotal)}</span>
            </div>

            {gstType !== "none" && (
              <>
                {gstType === "cgst_sgst" ? (
                  <>
                    <div className="flex items-center justify-between text-xs">
                      <span>CGST ({gstRate / 2}%)</span>
                      <span>{formatCurrency(totalCgst)}</span>
                    </div>
                    <div className="flex items-center justify-between text-xs">
                      <span>SGST ({gstRate / 2}%)</span>
                      <span>{formatCurrency(totalSgst)}</span>
                    </div>
                  </>
                ) : (
                  <div className="flex items-center justify-between text-xs">
                    <span>IGST ({gstRate}%)</span>
                    <span>{formatCurrency(totalIgst)}</span>
                  </div>
                )}
                <div className="flex items-center justify-between text-xs">
                  <span>Total Tax</span>
                  <span>{formatCurrency(totalGst)}</span>
                </div>
              </>
            )}

            {discount > 0 && (
              <div className="flex items-center justify-between text-red-500 text-xs">
                <span>Discount</span>
                <span>-{formatCurrency(discount)}</span>
              </div>
            )}

            <div className="flex items-center justify-between pt-2 border-t border-border">
              <span className="font-medium text-foreground">Grand Total</span>
              <span className="text-lg font-extrabold text-foreground">
                {formatCurrency(grandTotal)}
              </span>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Notes / Remarks */}
      <Card>
        <CardContent className="p-4">
          <label className="text-sm font-semibold text-foreground block mb-2">
            Notes / Remarks (Optional)
          </label>
          <textarea
            placeholder="Add invoice notes or terms..."
            value={notes}
            onChange={(e) => setNotes(e.target.value)}
            className="w-full min-h-[80px] rounded-xl border border-input bg-white dark:bg-dark-800 p-3 text-sm focus:outline-none focus:ring-2 focus:ring-primary/50 text-foreground"
          />
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
