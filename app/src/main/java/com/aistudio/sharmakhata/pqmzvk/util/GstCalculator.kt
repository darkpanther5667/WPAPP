package com.aistudio.sharmakhata.pqmzvk.util

enum class GstType {
    CGST_SGST,  // Intra-state: 9% CGST + 9% SGST = 18% total
    IGST        // Inter-state: 18% IGST
}

data class GstBreakdown(
    val taxableAmount: Double,
    val cgst: Double = 0.0,
    val sgst: Double = 0.0,
    val igst: Double = 0.0,
    val totalGst: Double = 0.0,
    val grandTotal: Double = 0.0
)

object GstCalculator {
    val gstRates = listOf(0, 5, 12, 18, 28)

    fun calculate(taxableAmount: Double, gstRate: Int = 18, type: GstType = GstType.CGST_SGST): GstBreakdown {
        if (gstRate == 0 || taxableAmount <= 0) {
            return GstBreakdown(taxableAmount = taxableAmount, grandTotal = taxableAmount)
        }
        val totalGst = taxableAmount * gstRate / 100.0
        return when (type) {
            GstType.CGST_SGST -> {
                val halfGst = totalGst / 2.0
                GstBreakdown(taxableAmount = taxableAmount, cgst = halfGst, sgst = halfGst, totalGst = totalGst, grandTotal = taxableAmount + totalGst)
            }
            GstType.IGST -> {
                GstBreakdown(taxableAmount = taxableAmount, igst = totalGst, totalGst = totalGst, grandTotal = taxableAmount + totalGst)
            }
        }
    }

    fun formatGstAmount(amount: Double): String = String.format("%.2f", amount)
}
