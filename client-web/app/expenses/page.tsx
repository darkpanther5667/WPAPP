"use client";

import { useEffect, useState } from "react";
import { apiClient } from "@/lib/api-client";
import { useAuthStore } from "@/stores/auth-store";
import { formatCurrency, formatDate, cn } from "@/lib/utils";
import { toast } from "@/lib/use-toast";
import { Expense } from "@/types";
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
  Plus,
  Trash2,
  Wallet,
  TrendingDown,
  RefreshCw,
  Coins,
  ShoppingCart,
  Home,
  Car,
  Utensils,
  FileText,
  MoreHorizontal,
} from "lucide-react";

const CATEGORIES = [
  "Other",
  "Rent",
  "Salary",
  "Electricity",
  "Transport",
  "Food",
  "Supplies",
  "Maintenance",
];

const categoryIcons: Record<string, React.ReactNode> = {
  Rent: <Home className="h-4 w-4" />,
  Salary: <Wallet className="h-4 w-4" />,
  Electricity: <TrendingDown className="h-4 w-4" />,
  Transport: <Car className="h-4 w-4" />,
  Food: <Utensils className="h-4 w-4" />,
  Supplies: <ShoppingCart className="h-4 w-4" />,
  Maintenance: <FileText className="h-4 w-4" />,
  Other: <Coins className="h-4 w-4" />,
};

export default function ExpensesPage() {
  const { store } = useAuthStore();
  const [expenses, setExpenses] = useState<Expense[]>([]);
  const [loading, setLoading] = useState(true);
  const [showAdd, setShowAdd] = useState(false);
  const [title, setTitle] = useState("");
  const [amount, setAmount] = useState("");
  const [category, setCategory] = useState("Other");
  const [note, setNote] = useState("");
  const [adding, setAdding] = useState(false);
  const [deletingId, setDeletingId] = useState<string | null>(null);

  const fetchExpenses = async () => {
    if (!store?.id) return;
    try {
      const { data } = await apiClient.get(`/api/db?storeId=${store.id}`);
      setExpenses(data.expenses || []);
    } catch {
      // handled via empty state
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchExpenses();
  }, [store?.id]);

  const totalExpenses = expenses.reduce((s, e) => s + e.amount, 0);

  const handleAdd = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!title.trim() || !amount) return;
    setAdding(true);
    try {
      await apiClient.post("/api/expense/add", {
        title: title.trim(),
        amount: parseFloat(amount),
        category: category || "Other",
        note: note.trim() || null,
      });
      setTitle("");
      setAmount("");
      setCategory("Other");
      setNote("");
      setShowAdd(false);
      toast({ title: "Expense added", variant: "success" });
      await fetchExpenses();
    } catch (err: any) {
      toast({
        title: "Error",
        description: err?.response?.data?.message || "Failed to add expense",
        variant: "error",
      });
    } finally {
      setAdding(false);
    }
  };

  const handleDelete = async (id: string) => {
    setDeletingId(id);
    try {
      await apiClient.delete(`/api/expense/${id}`);
      setExpenses((prev) => prev.filter((e) => e.id !== id));
      toast({ title: "Expense deleted", variant: "success" });
    } catch (err: any) {
      toast({
        title: "Error",
        description: err?.response?.data?.message || "Failed to delete expense",
        variant: "error",
      });
    } finally {
      setDeletingId(null);
    }
  };

  const sorted = [...expenses].sort(
    (a, b) => new Date(b.created_at).getTime() - new Date(a.created_at).getTime()
  );

  if (loading) {
    return (
      <div className="px-4 py-5 space-y-4 page-enter">
        <Skeleton className="h-7 w-24 rounded-xl" />
        <Skeleton className="h-24 rounded-2xl" />
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
        <h1 className="text-2xl font-extrabold text-foreground">Expenses</h1>
        <Button
          size="icon"
          className="h-10 w-10 rounded-full"
          onClick={() => setShowAdd(true)}
        >
          <Plus className="h-5 w-5" />
        </Button>
      </div>

      {/* Summary Card */}
      <Card className="bg-gradient-to-br from-red-50 to-transparent dark:from-red-900/10 border-red-100 dark:border-red-900/20">
        <CardContent className="p-5">
          <div className="flex items-center gap-3">
            <div className="h-10 w-10 rounded-xl bg-red-100 dark:bg-red-900/30 flex items-center justify-center">
              <TrendingDown className="h-5 w-5 text-red-500" />
            </div>
            <div>
              <p className="text-xs text-muted-foreground font-medium">
                Total Expenses
              </p>
              <p className="text-2xl font-extrabold text-red-500 font-mono-amount">
                {formatCurrency(totalExpenses)}
              </p>
            </div>
          </div>
          <p className="text-xs text-muted-foreground mt-2">
            {expenses.length} expense{expenses.length !== 1 ? "s" : ""} recorded
          </p>
        </CardContent>
      </Card>

      {/* Expense List */}
      {sorted.length === 0 ? (
        <Card>
          <CardContent className="py-12 text-center">
            <div className="p-4 rounded-full bg-primary/10 mb-3 inline-block">
              <Coins className="h-8 w-8 text-primary/50" />
            </div>
            <p className="text-sm font-medium text-foreground">
              No expenses yet
            </p>
            <p className="text-xs text-muted-foreground mt-1">
              Track your business expenses to see profit reports
            </p>
            <Button
              size="sm"
              className="mt-4"
              onClick={() => setShowAdd(true)}
            >
              Add Expense
            </Button>
          </CardContent>
        </Card>
      ) : (
        <div className="space-y-2">
          {sorted.map((exp) => (
            <Card key={exp.id} className="group">
              <CardContent className="p-4">
                <div className="flex items-center gap-3">
                  <div className="h-9 w-9 shrink-0 rounded-xl bg-accent/50 flex items-center justify-center text-muted-foreground">
                    {categoryIcons[exp.category] || categoryIcons["Other"]}
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-semibold text-foreground truncate">
                      {exp.title}
                    </p>
                    <p className="text-xs text-muted-foreground flex items-center gap-2">
                      <span className="px-1.5 py-0.5 rounded-full bg-accent/50 text-[10px] font-medium">
                        {exp.category}
                      </span>
                      {exp.note && (
                        <>
                          <span>·</span>
                          <span className="truncate">{exp.note}</span>
                        </>
                      )}
                    </p>
                  </div>
                  <div className="text-right">
                    <p className="text-sm font-bold text-red-500 font-mono-amount">
                      -{formatCurrency(exp.amount)}
                    </p>
                    <p className="text-[10px] text-muted-foreground">
                      {formatDate(exp.created_at)}
                    </p>
                  </div>
                  <button
                    onClick={() => handleDelete(exp.id)}
                    disabled={deletingId === exp.id}
                    className="shrink-0 h-8 w-8 rounded-full flex items-center justify-center opacity-0 group-hover:opacity-100 hover:bg-red-100 dark:hover:bg-red-900/30 text-muted-foreground hover:text-red-500 transition-all"
                  >
                    {deletingId === exp.id ? (
                      <span className="h-3.5 w-3.5 rounded-full border-2 border-red-300 border-t-red-500 animate-spin" />
                    ) : (
                      <Trash2 className="h-3.5 w-3.5" />
                    )}
                  </button>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}

      {/* Add Expense Dialog */}
      <Dialog open={showAdd} onOpenChange={setShowAdd}>
        <DialogContent className="max-w-sm">
          <DialogHeader>
            <DialogTitle>Add Expense</DialogTitle>
          </DialogHeader>
          <form onSubmit={handleAdd} className="space-y-4 mt-2">
            <div className="space-y-1.5">
              <label className="text-sm font-medium">Title</label>
              <Input
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                placeholder="e.g. Shop Rent"
                required
              />
            </div>
            <div className="space-y-1.5">
              <label className="text-sm font-medium">Amount (₹)</label>
              <Input
                type="number"
                step="0.01"
                min="0"
                value={amount}
                onChange={(e) => setAmount(e.target.value)}
                placeholder="5000"
                required
              />
            </div>
            <div className="space-y-1.5">
              <label className="text-sm font-medium">Category</label>
              <div className="flex flex-wrap gap-2">
                {CATEGORIES.map((cat) => (
                  <button
                    key={cat}
                    type="button"
                    onClick={() => setCategory(cat)}
                    className={cn(
                      "px-3 py-1.5 rounded-full text-xs font-medium transition-all",
                      category === cat
                        ? "bg-primary text-white shadow-sm"
                        : "bg-accent/50 text-muted-foreground hover:bg-accent"
                    )}
                  >
                    {cat}
                  </button>
                ))}
              </div>
            </div>
            <div className="space-y-1.5">
              <label className="text-sm font-medium">Note (optional)</label>
              <Input
                value={note}
                onChange={(e) => setNote(e.target.value)}
                placeholder="Any details..."
              />
            </div>
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
                {adding ? "Adding..." : "Add Expense"}
              </Button>
            </div>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  );
}
