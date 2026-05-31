package com.aistudio.sharmakhata.pqmzvk.util

import org.junit.Assert.assertEquals
import org.junit.Test

class GstCalculatorTest {

    // ── gstRates ─────────────────────────────────────────────────────────

    @Test
    fun `gstRates contains expected values`() {
        assertEquals(listOf(0, 5, 12, 18, 28), GstCalculator.gstRates)
    }

    // ── CGST + SGST (intra-state, 18% = 9% + 9%) ────────────────────────

    @Test
    fun `calculate CGST_SGST for 1000 at 18 percent`() {
        val result = GstCalculator.calculate(1000.0, 18, GstType.CGST_SGST)
        assertEquals(1000.0, result.taxableAmount, 0.001)
        assertEquals(90.0, result.cgst, 0.001)
        assertEquals(90.0, result.sgst, 0.001)
        assertEquals(0.0, result.igst, 0.001)
        assertEquals(180.0, result.totalGst, 0.001)
        assertEquals(1180.0, result.grandTotal, 0.001)
    }

    // ── IGST (inter-state, 18%) ──────────────────────────────────────────

    @Test
    fun `calculate IGST for 1000 at 18 percent`() {
        val result = GstCalculator.calculate(1000.0, 18, GstType.IGST)
        assertEquals(1000.0, result.taxableAmount, 0.001)
        assertEquals(0.0, result.cgst, 0.001)
        assertEquals(0.0, result.sgst, 0.001)
        assertEquals(180.0, result.igst, 0.001)
        assertEquals(180.0, result.totalGst, 0.001)
        assertEquals(1180.0, result.grandTotal, 0.001)
    }

    // ── Zero GST rate ────────────────────────────────────────────────────

    @Test
    fun `calculate with zero GST rate returns original amount`() {
        val result = GstCalculator.calculate(500.0, 0, GstType.CGST_SGST)
        assertEquals(500.0, result.taxableAmount, 0.001)
        assertEquals(0.0, result.cgst, 0.001)
        assertEquals(0.0, result.sgst, 0.001)
        assertEquals(0.0, result.totalGst, 0.001)
        assertEquals(500.0, result.grandTotal, 0.001)
    }

    // ── Zero taxable amount ──────────────────────────────────────────────

    @Test
    fun `calculate with zero taxable amount returns zero`() {
        val result = GstCalculator.calculate(0.0, 18, GstType.CGST_SGST)
        assertEquals(0.0, result.taxableAmount, 0.001)
        assertEquals(0.0, result.cgst, 0.001)
        assertEquals(0.0, result.sgst, 0.001)
        assertEquals(0.0, result.totalGst, 0.001)
        assertEquals(0.0, result.grandTotal, 0.001)
    }

    // ── Negative taxable amount ──────────────────────────────────────────

    @Test
    fun `calculate with negative taxable amount returns that amount unchanged`() {
        val result = GstCalculator.calculate(-100.0, 18, GstType.CGST_SGST)
        assertEquals(-100.0, result.taxableAmount, 0.001)
        assertEquals(0.0, result.cgst, 0.001)
        assertEquals(0.0, result.sgst, 0.001)
        assertEquals(0.0, result.totalGst, 0.001)
        assertEquals(-100.0, result.grandTotal, 0.001)
    }

    // ── Different rates ──────────────────────────────────────────────────

    @Test
    fun `calculate CGST_SGST for 100 at 5 percent`() {
        val result = GstCalculator.calculate(100.0, 5, GstType.CGST_SGST)
        assertEquals(100.0, result.taxableAmount, 0.001)
        assertEquals(2.5, result.cgst, 0.001)
        assertEquals(2.5, result.sgst, 0.001)
        assertEquals(5.0, result.totalGst, 0.001)
        assertEquals(105.0, result.grandTotal, 0.001)
    }

    @Test
    fun `calculate CGST_SGST for 200 at 12 percent`() {
        val result = GstCalculator.calculate(200.0, 12, GstType.CGST_SGST)
        assertEquals(200.0, result.taxableAmount, 0.001)
        assertEquals(12.0, result.cgst, 0.001)
        assertEquals(12.0, result.sgst, 0.001)
        assertEquals(24.0, result.totalGst, 0.001)
        assertEquals(224.0, result.grandTotal, 0.001)
    }

    @Test
    fun `calculate IGST for 200 at 28 percent`() {
        val result = GstCalculator.calculate(200.0, 28, GstType.IGST)
        assertEquals(200.0, result.taxableAmount, 0.001)
        assertEquals(56.0, result.igst, 0.001)
        assertEquals(56.0, result.totalGst, 0.001)
        assertEquals(256.0, result.grandTotal, 0.001)
    }

    // ── Edge cases ───────────────────────────────────────────────────────

    @Test
    fun `calculate with default parameters (18 percent CGST_SGST)`() {
        val result = GstCalculator.calculate(1000.0)
        assertEquals(1000.0, result.taxableAmount, 0.001)
        assertEquals(90.0, result.cgst, 0.001)
        assertEquals(90.0, result.sgst, 0.001)
        assertEquals(180.0, result.totalGst, 0.001)
        assertEquals(1180.0, result.grandTotal, 0.001)
    }

    @Test
    fun `calculate with floating point precision`() {
        val result = GstCalculator.calculate(99.99, 18, GstType.CGST_SGST)
        assertEquals(99.99, result.taxableAmount, 0.001)
        assertEquals(8.9991, result.cgst, 0.001)
        assertEquals(8.9991, result.sgst, 0.001)
        assertEquals(17.9982, result.totalGst, 0.001)
        assertEquals(117.9882, result.grandTotal, 0.001)
    }

    // ── formatGstAmount ──────────────────────────────────────────────────

    @Test
    fun `formatGstAmount formats zero`() {
        assertEquals("0.00", GstCalculator.formatGstAmount(0.0))
    }

    @Test
    fun `formatGstAmount formats whole number`() {
        assertEquals("90.00", GstCalculator.formatGstAmount(90.0))
    }

    @Test
    fun `formatGstAmount formats decimal`() {
        assertEquals("8.99", GstCalculator.formatGstAmount(8.99))
    }

    @Test
    fun `formatGstAmount formats two decimal places`() {
        assertEquals("17.99", GstCalculator.formatGstAmount(17.991))
    }
}
