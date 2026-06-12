import { clsx, type ClassValue } from "clsx";
import { twMerge } from "tailwind-merge";

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

export function formatCurrency(amount: number): string {
  if (amount === 0) return `${"₹"}0`;
  return `${"₹"}${Number(amount).toLocaleString("en-IN")}`;
}

export function formatDate(iso: string | undefined): string {
  if (!iso) return "—";
  const d = new Date(iso);
  return d.toLocaleDateString("en-IN", {
    day: "2-digit",
    month: "short",
    year: "numeric",
  });
}

export function formatDateTime(iso: string | undefined): string {
  if (!iso) return "—";
  const d = new Date(iso);
  return d.toLocaleDateString("en-IN", {
    day: "2-digit",
    month: "short",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
}

export function normalizePhone(phone?: string): string {
  if (!phone) return "";
  let p = String(phone).trim().replace(/[^\d]/g, "");
  if (p.length === 10) p = "91" + p;
  return p;
}

export function displayPhone(phone?: string): string {
  if (!phone) return "";
  const p = String(phone).replace(/[^\d]/g, "");
  if (p.startsWith("91") && p.length === 12) return `+91 ${p.slice(2)}`;
  return phone;
}

export function getCustomerOutstanding(
  customerId: string,
  transactions: { customer_id: string; type: string; amount: number }[],
  bills: { customer_id: string; total: number; status: string }[]
): number {
  let balance = 0;
  transactions
    .filter((t) => t.customer_id === customerId)
    .forEach((t) => {
      if (t.type === "credit") balance += t.amount;
      else if (t.type === "payment") balance -= t.amount;
    });
  bills
    .filter((b) => b.customer_id === customerId && (b.status === "unpaid" || b.status === "overdue" || b.status === "partial"))
    .forEach((b) => {
      balance += b.total;
    });
  return balance;
}

export function formatPhone(phone?: string): string {
  if (!phone) return "";
  const p = String(phone).replace(/[^\d]/g, "");
  if (p.startsWith("91") && p.length === 12) return `+91 ${p.slice(2)}`;
  return phone;
}

export function getInitials(name: string): string {
  const parts = name.trim().split(/\s+/);
  if (parts.length >= 2) return (parts[0][0] + parts[1][0]).toUpperCase();
  return name.slice(0, 2).toUpperCase();
}
