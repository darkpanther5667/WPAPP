package com.aistudio.sharmakhata.pqmzvk.util

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Locale
import java.util.TimeZone

class FormatUtilsTest {

    private val savedLocale = Locale.getDefault()
    private val savedTimeZone = TimeZone.getDefault()

    @Before
    fun setUp() {
        // The app targets Indian users, so set locale/timezone to IST for
        // deterministic date formatting in tests.
        Locale.setDefault(Locale.ENGLISH)
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"))
    }

    @After
    fun tearDown() {
        Locale.setDefault(savedLocale)
        TimeZone.setDefault(savedTimeZone)
    }

    // ── formatCurrency ───────────────────────────────────────────────────

    @Test
    fun `formatCurrency formats zero`() {
        val result = FormatUtils.formatCurrency(0.0)
        assertTrue("Should contain 0.00", result.contains("0.00"))
    }

    @Test
    fun `formatCurrency formats small positive value`() {
        val result = FormatUtils.formatCurrency(1000.0)
        assertTrue("Should contain 1,000", result.contains("1,000"))
    }

    @Test
    fun `formatCurrency formats value with decimals`() {
        val result = FormatUtils.formatCurrency(12345.67)
        assertTrue("Should contain 12,345", result.contains("12,345"))
    }

    @Test
    fun `formatCurrency formats negative value`() {
        val result = FormatUtils.formatCurrency(-500.0)
        assertTrue("Should contain minus sign for negative amount", result.contains("-"))
    }

    @Test
    fun `formatCurrency formats large value`() {
        val result = FormatUtils.formatCurrency(100000.0)
        assertTrue("Should contain 1,00,000", result.contains("1,00,000"))
    }

    // ── formatShort ──────────────────────────────────────────────────────

    @Test
    fun `formatShort shows plain value below 1000`() {
        assertEquals("₹500", FormatUtils.formatShort(500.0))
    }

    @Test
    fun `formatShort shows K for thousands`() {
        assertEquals("₹5.0K", FormatUtils.formatShort(5000.0))
    }

    @Test
    fun `formatShort shows L for lakhs`() {
        assertEquals("₹1.0L", FormatUtils.formatShort(100000.0))
    }

    @Test
    fun `formatShort handles negative value`() {
        assertEquals("-₹5.0K", FormatUtils.formatShort(-5000.0))
    }

    @Test
    fun `formatShort handles zero`() {
        assertEquals("₹0", FormatUtils.formatShort(0.0))
    }

    @Test
    fun `formatShort handles boundary at 999`() {
        assertEquals("₹999", FormatUtils.formatShort(999.0))
    }

    @Test
    fun `formatShort handles boundary at 1000`() {
        assertEquals("₹1.0K", FormatUtils.formatShort(1000.0))
    }

    @Test
    fun `formatShort handles boundary at 99999`() {
        assertEquals("₹100.0K", FormatUtils.formatShort(99999.0))
    }

    @Test
    fun `formatShort handles boundary at 100000`() {
        assertEquals("₹1.0L", FormatUtils.formatShort(100000.0))
    }

    @Test
    fun `formatShort handles negative value under 1000`() {
        assertEquals("-₹999", FormatUtils.formatShort(-999.0))
    }

    // ── formatDate ───────────────────────────────────────────────────────

    @Test
    fun `formatDate parses ISO date with timezone offset`() {
        assertEquals("15 Jan 2024", FormatUtils.formatDate("2024-01-15T10:30:00+05:30"))
    }

    @Test
    fun `formatDate parses ISO date without timezone`() {
        assertEquals("15 Jan 2024", FormatUtils.formatDate("2024-01-15T10:30:00"))
    }

    @Test
    fun `formatDate handles December date`() {
        assertEquals("25 Dec 2024", FormatUtils.formatDate("2024-12-25T08:00:00+05:30"))
    }

    @Test
    fun `formatDate falls back to raw string on invalid input`() {
        val result = FormatUtils.formatDate("not-a-date")
        assertEquals("not-a-date", result)
    }

    @Test
    fun `formatDate truncates unparseable ISO-like string`() {
        // A plain date without time component won't match either parser,
        // so it falls through to take(10).
        val result = FormatUtils.formatDate("2024-01-15")
        assertEquals("2024-01-15", result)
    }

    @Test
    fun `formatDate handles empty string`() {
        assertEquals("", FormatUtils.formatDate(""))
    }

    // ── formatDateTime ───────────────────────────────────────────────────

    @Test
    fun `formatDateTime formats date and time with AM`() {
        // 10:30 IST = 10:30 AM
        assertEquals(
            "15 Jan 2024, 10:30 AM",
            FormatUtils.formatDateTime("2024-01-15T10:30:00+05:30")
        )
    }

    @Test
    fun `formatDateTime formats date and time with PM`() {
        // 14:30 IST = 02:30 PM
        assertEquals(
            "15 Jun 2024, 02:30 PM",
            FormatUtils.formatDateTime("2024-06-15T14:30:00+05:30")
        )
    }

    @Test
    fun `formatDateTime does not crash on invalid input`() {
        assertEquals("garbage", FormatUtils.formatDateTime("garbage"))
    }

    @Test
    fun `formatDateTime handles empty string`() {
        assertEquals("", FormatUtils.formatDateTime(""))
    }

    // ── formatShortDate ──────────────────────────────────────────────────

    @Test
    fun `formatShortDate returns day and month`() {
        assertEquals("15 Jan", FormatUtils.formatShortDate("2024-01-15T10:30:00+05:30"))
    }

    @Test
    fun `formatShortDate handles December date`() {
        assertEquals("25 Dec", FormatUtils.formatShortDate("2024-12-25T08:00:00+05:30"))
    }

    @Test
    fun `formatShortDate falls back on invalid input`() {
        val result = FormatUtils.formatShortDate("bad-input")
        assertEquals("bad-input", result)
    }

    @Test
    fun `formatShortDate handles empty string`() {
        assertEquals("", FormatUtils.formatShortDate(""))
    }
}
