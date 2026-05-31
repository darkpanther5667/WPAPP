package com.aistudio.sharmakhata.pqmzvk.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.Align
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

data class InvoiceItem(
    val name: String,
    val qty: Int,
    val price: Double,
    val total: Double = price * qty
)

data class InvoiceData(
    val shopName: String,
    val shopAddress: String,
    val shopPhone: String,
    val gstin: String,
    val invoiceNumber: String,
    val customerName: String,
    val customerPhone: String,
    val date: String,
    val items: List<InvoiceItem>,
    val subtotal: Double,
    val tax: Double,
    val total: Double,
    val status: String // "paid" or "unpaid"
)

object PdfExporter {

    // Page constants (A4 at 72 dpi = 595 x 842 pts)
    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842

    // Margins
    private const val MARGIN_LEFT = 40f
    private const val MARGIN_RIGHT = 40f
    private const val MARGIN_TOP = 40f

    // Content width
    private const val CONTENT_WIDTH = PAGE_WIDTH - MARGIN_LEFT - MARGIN_RIGHT

    // Table column positions (relative to page left edge)
    private val COL_SNO_L = MARGIN_LEFT
    private val COL_SNO_R = MARGIN_LEFT + 35f
    private val COL_ITEM_L = COL_SNO_R + 4f
    private val COL_ITEM_R = COL_ITEM_L + 210f
    private val COL_QTY_L = COL_ITEM_R + 4f
    private val COL_QTY_R = COL_QTY_L + 55f
    private val COL_RATE_L = COL_QTY_R + 4f
    private val COL_RATE_R = COL_RATE_L + 100f
    private val COL_TOTAL_L = COL_RATE_R + 4f
    private val COL_TOTAL_R = MARGIN_LEFT + CONTENT_WIDTH

    // Paints — created once and reused
    private val headerPaint = Paint().apply {
        color = Color.parseColor("#1A237E")
        textSize = 26f
        typeface = Typeface.DEFAULT_BOLD
        isAntiAlias = true
    }

    private val shopInfoPaint = Paint().apply {
        color = Color.parseColor("#424242")
        textSize = 11f
        isAntiAlias = true
    }

    private val shopInfoBoldPaint = Paint().apply {
        color = Color.parseColor("#424242")
        textSize = 11f
        typeface = Typeface.DEFAULT_BOLD
        isAntiAlias = true
    }

    private val dividerPaint = Paint().apply {
        color = Color.parseColor("#BDBDBD")
        strokeWidth = 1.5f
    }

    private val sectionLabelPaint = Paint().apply {
        color = Color.parseColor("#757575")
        textSize = 10f
        typeface = Typeface.DEFAULT_BOLD
        isAntiAlias = true
    }

    private val sectionValuePaint = Paint().apply {
        color = Color.parseColor("#212121")
        textSize = 11f
        isAntiAlias = true
    }

    private val sectionValueBoldPaint = Paint().apply {
        color = Color.parseColor("#212121")
        textSize = 11f
        typeface = Typeface.DEFAULT_BOLD
        isAntiAlias = true
    }

    private val tableHeaderPaint = Paint().apply {
        color = Color.WHITE
        textSize = 10f
        typeface = Typeface.DEFAULT_BOLD
        isAntiAlias = true
    }

    private val tableHeaderBgPaint = Paint().apply {
        color = Color.parseColor("#1A237E")
        style = Paint.Style.FILL
    }

    private val tableRowPaint1 = Paint().apply {
        color = Color.parseColor("#FAFAFA")
        style = Paint.Style.FILL
    }

    private val tableRowPaint2 = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }

    private val tableRowBorderPaint = Paint().apply {
        color = Color.parseColor("#E0E0E0")
        strokeWidth = 0.5f
    }

    private val itemTextPaint = Paint().apply {
        color = Color.parseColor("#212121")
        textSize = 10f
        isAntiAlias = true
    }

    private val itemTextRightPaint = Paint().apply {
        color = Color.parseColor("#212121")
        textSize = 10f
        textAlign = Align.RIGHT
        isAntiAlias = true
    }

    private val totalLabelPaint = Paint().apply {
        color = Color.parseColor("#424242")
        textSize = 12f
        typeface = Typeface.DEFAULT_BOLD
        isAntiAlias = true
    }

    private val totalValuePaint = Paint().apply {
        color = Color.parseColor("#212121")
        textSize = 12f
        typeface = Typeface.DEFAULT_BOLD
        textAlign = Align.RIGHT
        isAntiAlias = true
    }

    private val grandTotalLabelPaint = Paint().apply {
        color = Color.parseColor("#1A237E")
        textSize = 14f
        typeface = Typeface.DEFAULT_BOLD
        isAntiAlias = true
    }

    private val grandTotalValuePaint = Paint().apply {
        color = Color.parseColor("#1A237E")
        textSize = 14f
        typeface = Typeface.DEFAULT_BOLD
        textAlign = Align.RIGHT
        isAntiAlias = true
    }

    private val statusBadgePaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val statusTextPaint = Paint().apply {
        color = Color.WHITE
        textSize = 10f
        typeface = Typeface.DEFAULT_BOLD
        textAlign = Align.CENTER
        isAntiAlias = true
    }

    private val footerPaint = Paint().apply {
        color = Color.parseColor("#9E9E9E")
        textSize = 9f
        textAlign = Align.CENTER
        isAntiAlias = true
    }

    // ── Public API ────────────────────────────────────────────────────────

    fun generateInvoice(context: Context, invoiceData: InvoiceData): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        drawInvoice(canvas, invoiceData)

        document.finishPage(page)

        val safeName = invoiceData.invoiceNumber
            .replace("/", "_")
            .replace("\\", "_")
            .replace(" ", "_")
        val file = File(context.cacheDir, "invoice_${safeName}.pdf")
        FileOutputStream(file).use { out ->
            document.writeTo(out)
        }
        document.close()

        return file
    }

    fun sharePdf(context: Context, file: File) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Invoice"))
    }

    // ── Drawing Engine ───────────────────────────────────────────────────

    private fun drawInvoice(canvas: Canvas, data: InvoiceData) {
        var y = MARGIN_TOP

        y = drawHeader(canvas, data, y)
        y += 8f
        y = drawDivider(canvas, y)
        y += 12f
        y = drawInvoiceMeta(canvas, data, y)
        y += 8f
        y = drawDivider(canvas, y)
        y += 12f
        y = drawItemsTable(canvas, data, y)
        y += 8f
        y = drawTotals(canvas, data, y)
        y += 16f
        y = drawStatusBadge(canvas, data, y)
        y += 10f
        drawFooter(canvas, y)
    }

    // ── Header ────────────────────────────────────────────────────────────

    private fun drawHeader(canvas: Canvas, data: InvoiceData, startY: Float): Float {
        var y = startY

        // Shop name
        canvas.drawText(data.shopName.uppercase(), MARGIN_LEFT, y + 26f, headerPaint)
        y += 32f

        // Address
        if (data.shopAddress.isNotBlank()) {
            canvas.drawText(data.shopAddress, MARGIN_LEFT, y, shopInfoPaint)
            y += 14f
        }

        // Phone
        if (data.shopPhone.isNotBlank()) {
            canvas.drawText("Phone: ${data.shopPhone}", MARGIN_LEFT, y, shopInfoPaint)
            y += 14f
        }

        // GSTIN
        if (data.gstin.isNotBlank()) {
            canvas.drawText("GSTIN: ${data.gstin}", MARGIN_LEFT, y, shopInfoPaint)
            y += 14f
        }

        return y
    }

    // ── Divider ───────────────────────────────────────────────────────────

    private fun drawDivider(canvas: Canvas, startY: Float): Float {
        canvas.drawLine(MARGIN_LEFT, startY, MARGIN_LEFT + CONTENT_WIDTH, startY, dividerPaint)
        return startY
    }

    // ── Invoice Metadata ──────────────────────────────────────────────────

    private fun drawInvoiceMeta(canvas: Canvas, data: InvoiceData, startY: Float): Float {
        val colMid = MARGIN_LEFT + CONTENT_WIDTH / 2f
        var y = startY

        // "INVOICE" title centered
        val invoiceLabelPaint = Paint().apply {
            color = Color.parseColor("#1A237E")
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("INVOICE", PAGE_WIDTH / 2f, y + 18f, invoiceLabelPaint)
        y += 30f

        // Left column: invoice number, date
        canvas.drawText("Invoice #", MARGIN_LEFT, y, sectionLabelPaint)
        canvas.drawText(data.invoiceNumber, MARGIN_LEFT, y + 14f, sectionValueBoldPaint)

        val dateLabelY = y + 32f
        canvas.drawText("Date", MARGIN_LEFT, dateLabelY, sectionLabelPaint)
        canvas.drawText(data.date, MARGIN_LEFT, dateLabelY + 14f, sectionValuePaint)

        // Right column: bill to
        val billToX = colMid + 10f
        canvas.drawText("Bill To", billToX, y, sectionLabelPaint)
        canvas.drawText(data.customerName, billToX, y + 14f, sectionValueBoldPaint)
        if (data.customerPhone.isNotBlank()) {
            canvas.drawText(data.customerPhone, billToX, y + 28f, sectionValuePaint)
            return y + 50f
        }

        return y + 40f
    }

    // ── Items Table ──────────────────────────────────────────────────────

    private fun drawItemsTable(canvas: Canvas, data: InvoiceData, startY: Float): Float {
        var y = startY
        val rowHeight = 22f

        // ── Table header ──
        canvas.drawRect(MARGIN_LEFT, y, COL_TOTAL_R, y + rowHeight, tableHeaderBgPaint)

        canvas.drawText("#", COL_SNO_L + 4f, y + 15f, tableHeaderPaint)
        canvas.drawText("Item", COL_ITEM_L, y + 15f, tableHeaderPaint)
        canvas.drawText("Qty", COL_QTY_R - 4f, y + 15f, tableHeaderPaint)
        canvas.drawText("Price", COL_RATE_R - 4f, y + 15f, tableHeaderPaint)
        canvas.drawText("Total", COL_TOTAL_R - 4f, y + 15f, tableHeaderPaint)

        y += rowHeight

        // ── Table rows ──
        data.items.forEachIndexed { index, item ->
            val bgPaint = if (index % 2 == 0) tableRowPaint1 else tableRowPaint2
            canvas.drawRect(MARGIN_LEFT, y, COL_TOTAL_R, y + rowHeight, bgPaint)

            // Row border bottom
            canvas.drawLine(MARGIN_LEFT, y + rowHeight, COL_TOTAL_R, y + rowHeight, tableRowBorderPaint)

            val textY = y + 15f

            // S.No.
            canvas.drawText((index + 1).toString(), COL_SNO_L + 4f, textY, itemTextPaint)

            // Item name (truncate if too long)
            val itemName = if (item.name.length > 28) item.name.take(25) + "..." else item.name
            canvas.drawText(itemName, COL_ITEM_L, textY, itemTextPaint)

            // Qty, Price, Total (right-aligned in their columns)
            canvas.drawText(item.qty.toString(), COL_QTY_R - 4f, textY, itemTextRightPaint)
            canvas.drawText(
                FormatUtils.formatCurrency(item.price),
                COL_RATE_R - 4f,
                textY,
                itemTextRightPaint
            )
            canvas.drawText(
                FormatUtils.formatCurrency(item.total),
                COL_TOTAL_R - 4f,
                textY,
                itemTextRightPaint
            )

            y += rowHeight
        }

        // Bottom border of last row
        canvas.drawLine(MARGIN_LEFT, y, COL_TOTAL_R, y, tableRowBorderPaint)

        return y
    }

    // ── Totals Section ────────────────────────────────────────────────────

    private fun drawTotals(canvas: Canvas, data: InvoiceData, startY: Float): Float {
        var y = startY
        val totalsRightX = COL_TOTAL_R

        // Subtotals table on the right side (from mid-page to right margin)
        val totalsLeftX = MARGIN_LEFT + CONTENT_WIDTH * 0.45f

        // Subtotal
        canvas.drawText("Subtotal", totalsLeftX, y, totalLabelPaint)
        canvas.drawText(FormatUtils.formatCurrency(data.subtotal), totalsRightX, y, totalValuePaint)
        y += 18f

        // Tax
        canvas.drawText("Tax", totalsLeftX, y, totalLabelPaint)
        canvas.drawText(FormatUtils.formatCurrency(data.tax), totalsRightX, y, totalValuePaint)
        y += 18f

        // Divider before grand total
        canvas.drawLine(totalsLeftX, y, totalsRightX, y, dividerPaint)
        y += 6f

        // Grand Total
        canvas.drawText("Grand Total", totalsLeftX, y, grandTotalLabelPaint)
        canvas.drawText(FormatUtils.formatCurrency(data.total), totalsRightX, y, grandTotalValuePaint)
        y += 22f

        // Bold double line under grand total
        val heavyLinePaint = Paint().apply {
            color = Color.parseColor("#1A237E")
            strokeWidth = 2.5f
        }
        canvas.drawLine(totalsLeftX, y, totalsRightX, y, heavyLinePaint)

        return y + 4f
    }

    // ── Status Badge ──────────────────────────────────────────────────────

    private fun drawStatusBadge(canvas: Canvas, data: InvoiceData, startY: Float): Float {
        val statusText = if (data.status.lowercase() == "paid") "PAID" else "UNPAID"
        val isPaid = data.status.lowercase() == "paid"
        val badgeColor = if (isPaid) Color.parseColor("#2E7D32") else Color.parseColor("#C62828")

        statusBadgePaint.color = badgeColor

        val badgeWidth = 90f
        val badgeHeight = 24f
        val cornerRadius = 4f
        val badgeX = MARGIN_LEFT + CONTENT_WIDTH - badgeWidth
        val badgeY = startY

        canvas.drawRoundRect(badgeX, badgeY, badgeX + badgeWidth, badgeY + badgeHeight, cornerRadius, cornerRadius, statusBadgePaint)
        canvas.drawText(statusText, badgeX + badgeWidth / 2f, badgeY + 16f, statusTextPaint)

        return startY + badgeHeight
    }

    // ── Footer ────────────────────────────────────────────────────────────

    private fun drawFooter(canvas: Canvas, startY: Float) {
        val y = PAGE_HEIGHT - 30f
        canvas.drawText("Thank you for your business!", PAGE_WIDTH / 2f, y, footerPaint)
        canvas.drawText(
            "This is a computer-generated invoice.",
            PAGE_WIDTH / 2f,
            y + 14f,
            footerPaint
        )
    }
}
