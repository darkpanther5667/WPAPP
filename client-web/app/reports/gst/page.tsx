"use client";

import { useEffect, useState, useMemo } from "react";
import Link from "next/link";
import { apiClient } from "@/lib/api-client";
import { useAuthStore } from "@/stores/auth-store";
import { formatCurrency, formatDate, cn } from "@/lib/utils";
import { Bill } from "@/types";
import { Card, CardContent, CardHeader, CardTitle, Skeleton } from "@/components/ui";
import { Button } from "@/components/ui/button";
import {
  ChevronLeft,
  FileSpreadsheet,
  Percent,
  Calculator,
  Calendar,
  Layers,
} from "lucide-react";

interface GstRateSummary {
  rate: number;
  taxable: number;
  cgst: number;
  sgst: number;
  igst: number;
  totalTax: number;
  totalAmount: number;
  count: number;
}

export default function GstReportPage() {
  const { store } = useAuthStore();
  const [bills, setBills] = useState<Bill[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedPeriod, setSelectedPeriod] = useState<"current_month" | "last_month" | "all_time">("current_month");

  const fetchData = async () => {
    if (!store?.id) return;
    try {
      const { data } = await apiClient.get(`/api/db?storeId=${store.id}`);
      setBills(data.bills || []);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, [store?.id]);

  // Filter bills by selected period
  const filteredBills = useMemo(() => {
    const now = new Date();
    const currentYear = now.getFullYear();
    const currentMonth = now.getMonth(); // 0-indexed

    return bills.filter((b) => {
      if (!b.created_at) return false;
      const bDate = new Date(b.created_at);
      
      if (selectedPeriod === "current_month") {
        return bDate.getFullYear() === currentYear && bDate.getMonth() === currentMonth;
      } else if (selectedPeriod === "last_month") {
        // Calculate last month
        const lmYear = currentMonth === 0 ? currentYear - 1 : currentYear;
        const lmMonth = currentMonth === 0 ? 11 : currentMonth - 1;
        return bDate.getFullYear() === lmYear && bDate.getMonth() === lmMonth;
      }
      return true; // all_time
    });
  }, [bills, selectedPeriod]);

  // GST aggregation calculations
  const gstSummary = useMemo(() => {
    let taxableAmount = 0;
    let cgst = 0;
    let sgst = 0;
    let igst = 0;
    let totalTax = 0;
    let grandTotal = 0;
    let gstInvoicesCount = 0;

    // Map rate breakdown
    const rateMap: Record<number, GstRateSummary> = {};

    filteredBills.forEach((b) => {
      // Check if bill has GST or total gst fields are greater than 0
      const billCgst = b.total_cgst || 0;
      const billSgst = b.total_sgst || 0;
      const billIgst = b.total_igst || 0;
      const billTax = billCgst + billSgst + billIgst;

      if ((b.gst_rate || 0) > 0 || billTax > 0) {
        gstInvoicesCount++;
        taxableAmount += b.taxable_amount || b.total - billTax;
        cgst += billCgst;
        sgst += billSgst;
        igst += billIgst;
        totalTax += billTax;
        grandTotal += b.grand_total || b.total;

        // Group by rate (b.gst_rate)
        const rate = b.gst_rate || 0;
        if (!rateMap[rate]) {
          rateMap[rate] = {
            rate,
            taxable: 0,
            cgst: 0,
            sgst: 0,
            igst: 0,
            totalTax: 0,
            totalAmount: 0,
            count: 0,
          };
        }
        const bTaxable = b.taxable_amount || b.total - billTax;
        rateMap[rate].taxable += bTaxable;
        rateMap[rate].cgst += billCgst;
        rateMap[rate].sgst += billSgst;
        rateMap[rate].igst += billIgst;
        rateMap[rate].totalTax += billTax;
        rateMap[rate].totalAmount += b.grand_total || b.total;
        rateMap[rate].count++;
      }
    });

    const ratesList = Object.values(rateMap).sort((a, b) => a.rate - b.rate);

    return {
      taxableAmount,
      cgst,
      sgst,
      igst,
      totalTax,
      grandTotal,
      gstInvoicesCount,
      ratesList,
    };
  }, [filteredBills]);

  if (loading) {
    return (
      <div className="px-4 py-5 space-y-4 page-enter">
        <Skeleton className="h-8 w-20 rounded-xl" />
        <Skeleton className="h-28 rounded-2xl" />
        <Skeleton className="h-56 rounded-2xl" />
      </div>
    );
  }

  return (
    <div className="px-4 py-5 space-y-5 page-enter">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div className="flex items-center gap-2">
          <Link href="/reports">
            <Button variant="ghost" size="icon" className="h-9 w-9 rounded-full">
              <ChevronLeft className="h-5 w-5" />
            </Button>
          </Link>
          <div>
            <h1 className="text-xl font-extrabold text-foreground">GST Returns Report</h1>
            <p className="text-xs text-muted-foreground">GST summary metrics for GSTR filing</p>
          </div>
        </div>

        {/* Period Selector Toggle */}
        <div className="flex bg-accent/40 rounded-xl p-1 shrink-0">
          {(["current_month", "last_month", "all_time"] as const).map((period) => (
            <button
              key={period}
              onClick={() => setSelectedPeriod(period)}
              className={cn(
                "px-3 py-1.5 rounded-lg text-xs font-semibold transition-all",
                selectedPeriod === period
                  ? "bg-primary text-white shadow-sm"
                  : "text-muted-foreground hover:text-foreground"
              )}
            >
              {period === "current_month"
                ? "This Month"
                : period === "last_month"
                ? "Last Month"
                : "All Time"}
            </button>
          ))}
        </div>
      </div>

      {/* Overview stats */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-3">
        <Card className="bg-gradient-to-br from-indigo-50 to-transparent dark:from-indigo-900/10 border-indigo-100 dark:border-indigo-900/20">
          <CardContent className="p-4">
            <Layers className="h-7 w-7 text-indigo-500 mb-1" />
            <p className="text-[10px] text-muted-foreground font-semibold uppercase">GST Invoices</p>
            <p className="text-lg font-bold text-foreground mt-0.5">{gstSummary.gstInvoicesCount}</p>
          </CardContent>
        </Card>

        <Card className="bg-gradient-to-br from-blue-50 to-transparent dark:from-blue-900/10 border-blue-100 dark:border-blue-900/20">
          <CardContent className="p-4">
            <Calculator className="h-7 w-7 text-blue-500 mb-1" />
            <p className="text-[10px] text-muted-foreground font-semibold uppercase">Taxable Value</p>
            <p className="text-lg font-extrabold text-blue-600 mt-0.5 font-mono-amount">
              {formatCurrency(gstSummary.taxableAmount)}
            </p>
          </CardContent>
        </Card>

        <Card className="bg-gradient-to-br from-green-50 to-transparent dark:from-green-900/10 border-green-100 dark:border-green-900/20">
          <CardContent className="p-4">
            <Percent className="h-7 w-7 text-green-500 mb-1" />
            <p className="text-[10px] text-muted-foreground font-semibold uppercase">Total GST Collected</p>
            <p className="text-lg font-extrabold text-green-600 mt-0.5 font-mono-amount">
              {formatCurrency(gstSummary.totalTax)}
            </p>
          </CardContent>
        </Card>

        <Card className="bg-gradient-to-br from-purple-50 to-transparent dark:from-purple-900/10 border-purple-100 dark:border-purple-900/20">
          <CardContent className="p-4">
            <FileSpreadsheet className="h-7 w-7 text-purple-500 mb-1" />
            <p className="text-[10px] text-muted-foreground font-semibold uppercase">Total Invoice Value</p>
            <p className="text-lg font-extrabold text-purple-600 mt-0.5 font-mono-amount">
              {formatCurrency(gstSummary.grandTotal)}
            </p>
          </CardContent>
        </Card>
      </div>

      {/* Tax Component split */}
      <Card>
        <CardHeader>
          <CardTitle className="text-sm font-semibold flex items-center gap-2">
            <Percent className="h-4 w-4" /> GST Component Breakdown
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-3 gap-4 text-center">
            <div className="p-3 bg-accent/20 rounded-xl">
              <p className="text-xs text-muted-foreground font-medium">CGST (Central)</p>
              <p className="text-base font-bold text-foreground font-mono-amount mt-1">
                {formatCurrency(gstSummary.cgst)}
              </p>
            </div>
            <div className="p-3 bg-accent/20 rounded-xl">
              <p className="text-xs text-muted-foreground font-medium">SGST (State)</p>
              <p className="text-base font-bold text-foreground font-mono-amount mt-1">
                {formatCurrency(gstSummary.sgst)}
              </p>
            </div>
            <div className="p-3 bg-accent/20 rounded-xl">
              <p className="text-xs text-muted-foreground font-medium">IGST (Integrated)</p>
              <p className="text-base font-bold text-foreground font-mono-amount mt-1">
                {formatCurrency(gstSummary.igst)}
              </p>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* GSTR-1 Rate-Wise Table */}
      <Card>
        <CardHeader className="pb-3 flex flex-row items-center justify-between">
          <CardTitle className="text-base flex items-center gap-2">
            <FileSpreadsheet className="h-4.5 w-4.5 text-primary" />
            GSTR-1 Outward Supplies Summary
          </CardTitle>
        </CardHeader>
        <CardContent className="overflow-x-auto">
          <table className="w-full text-sm text-left text-foreground">
            <thead className="text-xs text-muted-foreground uppercase bg-accent/25 border-b border-border">
              <tr>
                <th className="px-4 py-3">GST Rate</th>
                <th className="px-4 py-3 text-right">Invoices</th>
                <th className="px-4 py-3 text-right">Taxable Amt</th>
                <th className="px-4 py-3 text-right">CGST</th>
                <th className="px-4 py-3 text-right">SGST</th>
                <th className="px-4 py-3 text-right">IGST</th>
                <th className="px-4 py-3 text-right">Total GST</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border">
              {gstSummary.ratesList.length === 0 ? (
                <tr>
                  <td colSpan={7} className="px-4 py-6 text-center text-muted-foreground">
                    No GST invoices in this period
                  </td>
                </tr>
              ) : (
                gstSummary.ratesList.map((row) => (
                  <tr key={row.rate} className="hover:bg-accent/10">
                    <td className="px-4 py-3 font-semibold">{row.rate}%</td>
                    <td className="px-4 py-3 text-right">{row.count}</td>
                    <td className="px-4 py-3 text-right font-mono-amount">{formatCurrency(row.taxable)}</td>
                    <td className="px-4 py-3 text-right font-mono-amount">{row.cgst > 0 ? formatCurrency(row.cgst) : "—"}</td>
                    <td className="px-4 py-3 text-right font-mono-amount">{row.sgst > 0 ? formatCurrency(row.sgst) : "—"}</td>
                    <td className="px-4 py-3 text-right font-mono-amount">{row.igst > 0 ? formatCurrency(row.igst) : "—"}</td>
                    <td className="px-4 py-3 text-right font-bold text-primary font-mono-amount">{formatCurrency(row.totalTax)}</td>
                  </tr>
                ))
              )}
            </tbody>
            {gstSummary.ratesList.length > 0 && (
              <tfoot className="bg-accent/15 font-bold border-t-2 border-border">
                <tr>
                  <td className="px-4 py-3">Total</td>
                  <td className="px-4 py-3 text-right">{gstSummary.gstInvoicesCount}</td>
                  <td className="px-4 py-3 text-right font-mono-amount">{formatCurrency(gstSummary.taxableAmount)}</td>
                  <td className="px-4 py-3 text-right font-mono-amount">{formatCurrency(gstSummary.cgst)}</td>
                  <td className="px-4 py-3 text-right font-mono-amount">{formatCurrency(gstSummary.sgst)}</td>
                  <td className="px-4 py-3 text-right font-mono-amount">{formatCurrency(gstSummary.igst)}</td>
                  <td className="px-4 py-3 text-right text-primary font-mono-amount">{formatCurrency(gstSummary.totalTax)}</td>
                </tr>
              </tfoot>
            )}
          </table>
        </CardContent>
      </Card>

      {/* GSTR-3B Format */}
      <Card>
        <CardHeader>
          <CardTitle className="text-base flex items-center gap-2">
            <Calendar className="h-4.5 w-4.5 text-green-500" />
            GSTR-3B Outward Taxable Supplies Summary
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-3">
          <div className="flex justify-between items-center p-3 rounded-xl bg-accent/20">
            <div>
              <p className="text-xs font-semibold text-foreground">3.1 (a) Outward Taxable Supplies</p>
              <p className="text-[10px] text-muted-foreground">Other than zero rated, nil rated and exempted</p>
            </div>
            <div className="text-right">
              <p className="text-sm font-bold text-foreground font-mono-amount">Val: {formatCurrency(gstSummary.taxableAmount)}</p>
              <p className="text-xs font-semibold text-primary font-mono-amount">GST: {formatCurrency(gstSummary.totalTax)}</p>
            </div>
          </div>
          <div className="flex justify-between items-center p-3 rounded-xl bg-accent/20">
            <div>
              <p className="text-xs font-semibold text-foreground">3.1 (b) Outward Taxable Supplies (Zero Rated)</p>
              <p className="text-[10px] text-muted-foreground">Exports or SEZ sales</p>
            </div>
            <p className="text-sm font-bold text-muted-foreground font-mono-amount">—</p>
          </div>
          <div className="flex justify-between items-center p-3 rounded-xl bg-accent/20">
            <div>
              <p className="text-xs font-semibold text-foreground">3.1 (c) Other Outward Supplies (Nil Rated, Exempted)</p>
              <p className="text-[10px] text-muted-foreground">Non-GST sales</p>
            </div>
            <p className="text-sm font-bold text-muted-foreground font-mono-amount">—</p>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
