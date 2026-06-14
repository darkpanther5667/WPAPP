"use client";

import { useEffect, useState } from "react";
import { apiClient } from "@/lib/api-client";
import { useAuthStore } from "@/stores/auth-store";
import { toast } from "@/lib/use-toast";
import { Staff } from "@/types";
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
  Users,
  Shield,
  User,
  Search,
} from "lucide-react";

export default function StaffPage() {
  const { store } = useAuthStore();
  const [staffList, setStaffList] = useState<Staff[]>([]);
  const [loading, setLoading] = useState(true);
  const [showAdd, setShowAdd] = useState(false);
  const [search, setSearch] = useState("");

  // Form states
  const [name, setName] = useState("");
  const [phone, setPhone] = useState("");
  const [role, setRole] = useState("staff");
  const [adding, setAdding] = useState(false);
  const [deletingId, setDeletingId] = useState<string | null>(null);

  const fetchStaff = async () => {
    if (!store?.id) return;
    try {
      const { data } = await apiClient.get(`/api/db?storeId=${store.id}`);
      setStaffList(data.staff || []);
    } catch {
      // handled via empty state
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchStaff();
  }, [store?.id]);

  const handleAdd = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim() || !phone.trim()) {
      toast({
        title: "Validation Error",
        description: "Name and Phone are required.",
        variant: "error",
      });
      return;
    }
    setAdding(true);
    try {
      await apiClient.post("/api/staff/add", {
        name: name.trim(),
        phone: phone.trim(),
        role,
      });

      setName("");
      setPhone("");
      setRole("staff");
      setShowAdd(false);
      toast({ title: "Staff member added successfully", variant: "success" });
      await fetchStaff();
    } catch (err: any) {
      toast({
        title: "Error",
        description: err?.response?.data?.message || "Failed to add staff member",
        variant: "error",
      });
    } finally {
      setAdding(false);
    }
  };

  const handleDelete = async (id: string) => {
    if (!confirm("Are you sure you want to remove this staff member?")) return;
    setDeletingId(id);
    try {
      await apiClient.delete(`/api/staff/${id}`);
      setStaffList((prev) => prev.filter((s) => s.id !== id));
      toast({ title: "Staff member removed successfully", variant: "success" });
    } catch (err: any) {
      toast({
        title: "Error",
        description: err?.response?.data?.message || "Failed to remove staff member",
        variant: "error",
      });
    } finally {
      setDeletingId(null);
    }
  };

  const filtered = staffList.filter((s) =>
    s.name.toLowerCase().includes(search.toLowerCase()) ||
    s.phone.includes(search)
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
        <h1 className="text-2xl font-extrabold text-foreground">Staff Management</h1>
        <Button
          size="icon"
          className="h-10 w-10 rounded-full"
          onClick={() => setShowAdd(true)}
        >
          <Plus className="h-5 w-5" />
        </Button>
      </div>

      {/* Summary Panel */}
      <Card className="bg-gradient-to-br from-indigo-50 to-transparent dark:from-indigo-900/10 border-indigo-100 dark:border-indigo-900/20">
        <CardContent className="p-5">
          <div className="flex items-center gap-3">
            <div className="h-10 w-10 rounded-xl bg-indigo-100 dark:bg-indigo-900/30 flex items-center justify-center">
              <Users className="h-5 w-5 text-indigo-500" />
            </div>
            <div>
              <p className="text-xs text-muted-foreground font-medium">Active Team Members</p>
              <p className="text-2xl font-extrabold text-indigo-500 font-mono-amount">
                {staffList.length}
              </p>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Filter / Search */}
      <div className="relative">
        <Search className="absolute left-3 top-2.5 h-4 w-4 text-muted-foreground" />
        <Input
          placeholder="Search by name or phone..."
          className="pl-9"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />
      </div>

      {/* Staff List */}
      {filtered.length === 0 ? (
        <Card>
          <CardContent className="py-12 text-center">
            <div className="p-4 rounded-full bg-primary/10 mb-3 inline-block">
              <Users className="h-8 w-8 text-primary/50" />
            </div>
            <p className="text-sm font-medium text-foreground">No staff members found</p>
            <p className="text-xs text-muted-foreground mt-1">
              Add staff members to let them record payments and issue invoices
            </p>
            <Button size="sm" className="mt-4" onClick={() => setShowAdd(true)}>
              Add Staff Member
            </Button>
          </CardContent>
        </Card>
      ) : (
        <div className="space-y-2">
          {filtered.map((staff) => (
            <Card key={staff.id} className="group">
              <CardContent className="p-4">
                <div className="flex items-center gap-3">
                  <div className="h-9 w-9 shrink-0 rounded-xl bg-accent/50 flex items-center justify-center text-muted-foreground">
                    {staff.id.startsWith("owner_") ? (
                      <Shield className="h-4 w-4 text-indigo-500" />
                    ) : (
                      <User className="h-4 w-4" />
                    )}
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-semibold text-foreground truncate">
                      {staff.name}
                    </p>
                    <p className="text-xs text-muted-foreground flex items-center gap-2">
                      <span>{staff.phone}</span>
                      <span>·</span>
                      <span className="px-1.5 py-0.5 rounded-full bg-accent/50 text-[10px] font-medium capitalize">
                        {staff.id.startsWith("owner_") ? "Owner" : "Staff"}
                      </span>
                    </p>
                  </div>
                  <div className="text-right flex items-center gap-3">
                    {!staff.id.startsWith("owner_") && (
                      <button
                        onClick={() => handleDelete(staff.id)}
                        disabled={deletingId === staff.id}
                        className="shrink-0 h-8 w-8 rounded-full flex items-center justify-center opacity-0 group-hover:opacity-100 hover:bg-red-100 dark:hover:bg-red-900/30 text-muted-foreground hover:text-red-500 transition-all"
                      >
                        {deletingId === staff.id ? (
                          <span className="h-3.5 w-3.5 rounded-full border-2 border-red-300 border-t-red-500 animate-spin" />
                        ) : (
                          <Trash2 className="h-3.5 w-3.5" />
                        )}
                      </button>
                    )}
                  </div>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}

      {/* Add Staff Dialog */}
      <Dialog open={showAdd} onOpenChange={setShowAdd}>
        <DialogContent className="max-w-sm">
          <DialogHeader>
            <DialogTitle>Add Staff Member</DialogTitle>
          </DialogHeader>
          <form onSubmit={handleAdd} className="space-y-4 mt-2">
            <div className="space-y-1.5">
              <label className="text-sm font-medium">Name</label>
              <Input
                value={name}
                onChange={(e) => setName(e.target.value)}
                placeholder="Full Name"
                required
              />
            </div>
            <div className="space-y-1.5">
              <label className="text-sm font-medium">Phone</label>
              <Input
                value={phone}
                onChange={(e) => setPhone(e.target.value)}
                placeholder="e.g. 9876543210"
                required
              />
            </div>
            <div className="space-y-1.5">
              <label className="text-sm font-medium">Role</label>
              <select
                value={role}
                onChange={(e) => setRole(e.target.value)}
                className="w-full h-10 px-3 rounded-lg border border-input bg-background text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
              >
                <option value="staff">Staff</option>
                <option value="admin">Admin</option>
              </select>
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
                {adding ? "Adding..." : "Add Staff"}
              </Button>
            </div>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  );
}
