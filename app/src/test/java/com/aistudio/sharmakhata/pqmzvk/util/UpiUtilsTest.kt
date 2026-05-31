package com.aistudio.sharmakhata.pqmzvk.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class UpiUtilsTest {

    // ── buildUpiUri ──────────────────────────────────────────────────────

    @Test
    fun `buildUpiUri includes all fields when provided`() {
        val request = UpiUtils.UpiRequest(
            upiId = "merchant@upi",
            merchantName = "ShopName",
            amount = 100.0,
            note = "Payment for bill"
        )
        val uri = UpiUtils.buildUpiUri(request)

        assertEquals("upi", uri.scheme)
        assertEquals("pay", uri.authority)
        assertEquals("merchant%40upi", uri.getQueryParameter("pa"))
        assertEquals("ShopName", uri.getQueryParameter("pn"))
        assertEquals("INR", uri.getQueryParameter("cu"))
        assertEquals("100.00", uri.getQueryParameter("am"))
        assertEquals("Payment for bill", uri.getQueryParameter("tn"))
    }

    @Test
    fun `buildUpiUri omits amount when null`() {
        val request = UpiUtils.UpiRequest(
            upiId = "test@upi",
            merchantName = "Store",
            amount = null,
            note = "Thanks"
        )
        val uri = UpiUtils.buildUpiUri(request)
        assertNull("amount query param should be absent", uri.getQueryParameter("am"))
        assertEquals("test%40upi", uri.getQueryParameter("pa"))
        assertEquals("Store", uri.getQueryParameter("pn"))
        assertEquals("Thanks", uri.getQueryParameter("tn"))
    }

    @Test
    fun `buildUpiUri omits note when blank`() {
        val request = UpiUtils.UpiRequest(
            upiId = "test@upi",
            merchantName = "Store",
            amount = 50.0,
            note = ""
        )
        val uri = UpiUtils.buildUpiUri(request)
        assertNull("note query param should be absent when blank", uri.getQueryParameter("tn"))
        assertEquals("50.00", uri.getQueryParameter("am"))
    }

    @Test
    fun `buildUpiUri omits note when whitespace only`() {
        val request = UpiUtils.UpiRequest(
            upiId = "x@upi",
            merchantName = "X",
            amount = 25.0,
            note = "   "
        )
        val uri = UpiUtils.buildUpiUri(request)
        assertNull("note query param should be absent when whitespace only", uri.getQueryParameter("tn"))
    }

    @Test
    fun `buildUpiUri returns correct scheme`() {
        val request = UpiUtils.UpiRequest(
            upiId = "payee@bank",
            merchantName = "My Shop"
        )
        val uri = UpiUtils.buildUpiUri(request)
        assertEquals("upi", uri.scheme)
        assertEquals("pay", uri.authority)
    }

    @Test
    fun `buildUpiUri encodes special characters in note`() {
        val request = UpiUtils.UpiRequest(
            upiId = "a@b",
            merchantName = "S",
            amount = 10.0,
            note = "Thanks & regards!"
        )
        val uri = UpiUtils.buildUpiUri(request)
        val tn = uri.getQueryParameter("tn")
        // Uri.encode will encode the ampersand
        assertEquals("Thanks & regards!", tn)
    }

    @Test
    fun `buildUpiUri formats amount with two decimal places`() {
        val request = UpiUtils.UpiRequest(
            upiId = "m@u",
            merchantName = "Shop",
            amount = 123.456
        )
        val uri = UpiUtils.buildUpiUri(request)
        assertEquals("123.46", uri.getQueryParameter("am"))
    }

    @Test
    fun `buildUpiUri formats whole amount with two decimal places`() {
        val request = UpiUtils.UpiRequest(
            upiId = "m@u",
            merchantName = "S",
            amount = 50.0
        )
        val uri = UpiUtils.buildUpiUri(request)
        assertEquals("50.00", uri.getQueryParameter("am"))
    }

    // ── UpiRequest data class ────────────────────────────────────────────

    @Test
    fun `UpiRequest uses default currency INR`() {
        val request = UpiUtils.UpiRequest(upiId = "x@y", merchantName = "Test")
        assertEquals("INR", request.currency)
    }

    @Test
    fun `UpiRequest uses default empty note`() {
        val request = UpiUtils.UpiRequest(upiId = "x@y", merchantName = "Test")
        assertEquals("", request.note)
    }

    @Test
    fun `UpiRequest uses default null amount`() {
        val request = UpiUtils.UpiRequest(upiId = "x@y", merchantName = "Test")
        assertNull(request.amount)
    }
}
