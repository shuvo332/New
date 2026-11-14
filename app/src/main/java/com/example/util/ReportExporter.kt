package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.local.AuditWithItems
import com.example.data.local.LineItemCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReportExporter {

    private val currencyFormat: NumberFormat
        get() = NumberFormat.getCurrencyInstance(Locale.US)

    private val dateFormat: SimpleDateFormat
        get() = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.US)

    fun formatMoney(amount: Double): String = currencyFormat.format(amount)

    fun formatDate(millis: Long): String = dateFormat.format(Date(millis))

    fun buildPlainTextReport(auditWithItems: AuditWithItems): String {
        val audit = auditWithItems.audit
        val missing = auditWithItems.items.filter { it.category == LineItemCategory.MISSING }
        val mismatches = auditWithItems.items.filter { it.category == LineItemCategory.MISMATCH }
        val matched = auditWithItems.items.filter { it.category == LineItemCategory.MATCHED }

        return buildString {
            appendLine("=== INVOICE & ORDER AUDIT REPORT ===")
            appendLine("Order Ref: ${audit.orderRef}")
            appendLine("Vendor: ${audit.vendorName}")
            appendLine("Date: ${formatDate(audit.createdAtMillis)}")
            if (audit.isSampleAiPreview) {
                appendLine("Mode: Sample AI Preview (Firebase AI Logic not enabled)")
            }
            appendLine("Invoice Total: ${formatMoney(audit.invoiceTotal)}")
            appendLine("System Total:  ${formatMoney(audit.systemTotal)}")
            appendLine("Net Variance:  ${formatMoney(audit.netVariance)}")
            appendLine("Match Rate:    ${audit.matchRatePercent}%")
            appendLine()

            appendLine("[MISSING ITEMS (${missing.size})]")
            if (missing.isEmpty()) {
                appendLine("  None")
            } else {
                missing.forEach { item ->
                    val status = if (item.isResolved) " [RESOLVED]" else ""
                    appendLine("  - ${item.sku} | ${item.invoiceItemName} | Qty: ${item.invoiceQty} @ ${formatMoney(item.invoiceUnitPrice)}$status")
                    appendLine("    Note: ${item.note}")
                }
            }
            appendLine()

            appendLine("[WRONG ENTRIES / MISMATCHES (${mismatches.size})]")
            if (mismatches.isEmpty()) {
                appendLine("  None")
            } else {
                mismatches.forEach { item ->
                    val status = if (item.isResolved) " [RESOLVED]" else ""
                    appendLine("  - ${item.sku} | Inv: ${item.invoiceItemName} (${item.invoiceQty} x ${formatMoney(item.invoiceUnitPrice)}) vs Sys: ${item.systemItemName} (${item.systemQty} x ${formatMoney(item.systemUnitPrice)})$status")
                    appendLine("    Note: ${item.note}")
                }
            }
            appendLine()

            appendLine("[MATCHED ITEMS (${matched.size})]")
            matched.forEach { item ->
                appendLine("  - ${item.sku} | ${item.invoiceItemName} | Qty: ${item.invoiceQty} @ ${formatMoney(item.invoiceUnitPrice)}")
            }
        }
    }

    fun copyReportToClipboard(context: Context, auditWithItems: AuditWithItems) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(
            "Audit Report ${auditWithItems.audit.orderRef}",
            buildPlainTextReport(auditWithItems)
        )
        clipboard.setPrimaryClip(clip)
    }

    fun sharePlainTextReport(context: Context, auditWithItems: AuditWithItems) {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Audit Discrepancy Report - ${auditWithItems.audit.orderRef}")
            putExtra(Intent.EXTRA_TEXT, buildPlainTextReport(auditWithItems))
        }
        context.startActivity(Intent.createChooser(sendIntent, "Share Audit Report"))
    }

    suspend fun exportAndSharePdf(context: Context, auditWithItems: AuditWithItems): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val reportsDir = File(context.cacheDir, "reports").apply { mkdirs() }
                val safeRef = auditWithItems.audit.orderRef.replace(Regex("[^A-Za-z0-9_-]"), "_")
                val pdfFile = File(reportsDir, "Audit_${safeRef}_${System.currentTimeMillis()}.pdf")

                val pdf = PdfDocument()
                val pageWidth = 595
                val pageHeight = 842
                val margin = 40f

                var pageNumber = 1
                var page = pdf.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
                var canvas = page.canvas
                var y = 48f

                val headerPaint = Paint().apply {
                    color = Color.parseColor("#006D77")
                    style = Paint.Style.FILL
                }
                val titlePaint = Paint().apply {
                    color = Color.WHITE
                    textSize = 18f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                }
                val subHeaderPaint = Paint().apply {
                    color = Color.parseColor("#D1FAE5")
                    textSize = 11f
                }
                val sectionPaint = Paint().apply {
                    color = Color.parseColor("#005B6E")
                    textSize = 13f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                }
                val bodyPaint = Paint().apply {
                    color = Color.parseColor("#0F172A")
                    textSize = 10.5f
                }
                val mutedPaint = Paint().apply {
                    color = Color.parseColor("#475569")
                    textSize = 9.5f
                }

                // Header Banner
                canvas.drawRoundRect(margin, y, pageWidth - margin, y + 68f, 12f, 12f, headerPaint)
                canvas.drawText("INVOICE & ORDER AUDIT REPORT", margin + 16f, y + 28f, titlePaint)
                val audit = auditWithItems.audit
                canvas.drawText(
                    "${audit.orderRef}  |  ${audit.vendorName}  |  ${formatDate(audit.createdAtMillis)}",
                    margin + 16f,
                    y + 49f,
                    subHeaderPaint
                )
                y += 88f

                fun ensureSpace(needed: Float) {
                    if (y + needed > pageHeight - 40f) {
                        pdf.finishPage(page)
                        pageNumber++
                        page = pdf.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
                        canvas = page.canvas
                        y = 48f
                    }
                }

                // Summary
                canvas.drawText(
                    "Invoice Total: ${formatMoney(audit.invoiceTotal)}   |   System Total: ${formatMoney(audit.systemTotal)}   |   Variance: ${formatMoney(audit.netVariance)}",
                    margin,
                    y,
                    bodyPaint
                )
                y += 18f
                canvas.drawText(
                    "Match Rate: ${audit.matchRatePercent}%   |   Missing: ${audit.missingCount}   |   Mismatches: ${audit.mismatchCount}   |   Matched: ${audit.matchedCount}",
                    margin,
                    y,
                    mutedPaint
                )
                y += 24f

                if (audit.isSampleAiPreview) {
                    canvas.drawText(
                        "Notice: Sample AI Preview (Live Gemini vision analysis requires Firebase AI Logic).",
                        margin,
                        y,
                        mutedPaint
                    )
                    y += 22f
                }

                fun drawSection(heading: String, category: LineItemCategory) {
                    val items = auditWithItems.items.filter { it.category == category }
                    ensureSpace(40f)
                    canvas.drawText("$heading (${items.size})", margin, y, sectionPaint)
                    y += 16f
                    if (items.isEmpty()) {
                        canvas.drawText("No items in this category.", margin + 8f, y, mutedPaint)
                        y += 18f
                    } else {
                        items.forEach { item ->
                            ensureSpace(36f)
                            val resolvedTag = if (item.isResolved) " [RESOLVED]" else ""
                            val line1 = "${item.sku} - ${item.invoiceItemName}$resolvedTag"
                            val line2 = "Inv: ${item.invoiceQty} x ${formatMoney(item.invoiceUnitPrice)}  |  Sys: ${item.systemQty} x ${formatMoney(item.systemUnitPrice)}"
                            canvas.drawText(line1.take(78), margin + 8f, y, bodyPaint)
                            y += 13f
                            canvas.drawText(line2, margin + 12f, y, mutedPaint)
                            y += 13f
                            if (item.note.isNotBlank()) {
                                canvas.drawText(item.note.take(86), margin + 12f, y, mutedPaint)
                                y += 14f
                            }
                            y += 4f
                        }
                    }
                    y += 8f
                }

                drawSection("MISSING ITEMS", LineItemCategory.MISSING)
                drawSection("WRONG ENTRIES / MISMATCHES", LineItemCategory.MISMATCH)
                drawSection("MATCHED ITEMS", LineItemCategory.MATCHED)

                pdf.finishPage(page)
                FileOutputStream(pdfFile).use { out -> pdf.writeTo(out) }
                pdf.close()

                withContext(Dispatchers.Main) {
                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        pdfFile
                    )
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/pdf"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        putExtra(Intent.EXTRA_SUBJECT, "Audit Report - ${audit.orderRef}")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Export PDF Report"))
                }
            }
        }
}
