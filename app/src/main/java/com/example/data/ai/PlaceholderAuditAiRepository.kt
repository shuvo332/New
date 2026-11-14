package com.example.data.ai

import android.graphics.Bitmap
import com.example.data.local.LineItemCategory
import com.example.data.local.MismatchType
import kotlinx.coroutines.delay
import kotlin.math.abs

/**
 * Sample implementation used when Firebase AI Logic is not provisioned.
 * Always marks returned results with `isSampleAiPreview = true` so the UI
 * displays the required notice that live Gemini vision analysis requires Firebase.
 */
class PlaceholderAuditAiRepository : AuditAiRepository {

    override suspend fun compareScreenshotAndInvoice(
        screenshotBitmap: Bitmap,
        invoiceBitmap: Bitmap,
        orderRefHint: String,
        vendorHint: String,
        onStageProgress: (String) -> Unit
    ): AuditAnalysisResult {
        onStageProgress("Scanning system screenshot (${screenshotBitmap.width}×${screenshotBitmap.height})…")
        delay(550)
        onStageProgress("Extracting invoice line items (${invoiceBitmap.width}×${invoiceBitmap.height})…")
        delay(650)
        onStageProgress("Cross-checking SKUs, quantities & unit prices…")
        delay(550)

        val resolvedOrderRef = orderRefHint.trim().ifBlank { "ORD-2026-8841" }
        val resolvedVendor = vendorHint.trim().ifBlank { "Apex Supply Co." }

        // Deterministic variation based on image dimensions so different uploads feel responsive
        val seedVariant = abs(screenshotBitmap.width + invoiceBitmap.height) % 2

        val sampleItems = if (seedVariant == 0) {
            listOf(
                AuditedLineItemDraft(
                    category = LineItemCategory.MISSING,
                    mismatchType = MismatchType.NONE,
                    sku = "SKU-4412",
                    invoiceItemName = "Thermal Shipping Labels (Roll 500)",
                    systemItemName = "— Not in System SS —",
                    invoiceQty = 4,
                    systemQty = 0,
                    invoiceUnitPrice = 18.50,
                    systemUnitPrice = 0.0,
                    note = "Billed on invoice ($74.00) but omitted from system order entry."
                ),
                AuditedLineItemDraft(
                    category = LineItemCategory.MISSING,
                    mismatchType = MismatchType.NONE,
                    sku = "SKU-9028",
                    invoiceItemName = "Heavy-Duty Packing Tape (6-Pack)",
                    systemItemName = "— Not in System SS —",
                    invoiceQty = 2,
                    systemQty = 0,
                    invoiceUnitPrice = 14.00,
                    systemUnitPrice = 0.0,
                    note = "Item appears at bottom of printed invoice; missing from system screenshot."
                ),
                AuditedLineItemDraft(
                    category = LineItemCategory.MISMATCH,
                    mismatchType = MismatchType.QUANTITY,
                    sku = "SKU-1180",
                    invoiceItemName = "Kraft Corrugated Mailer 12x9",
                    systemItemName = "Kraft Corrugated Mailer 12x9",
                    invoiceQty = 50,
                    systemQty = 35,
                    invoiceUnitPrice = 1.80,
                    systemUnitPrice = 1.80,
                    note = "Quantity mismatch: Invoice bills 50 units vs 35 logged in system (-15 units / $27.00)."
                ),
                AuditedLineItemDraft(
                    category = LineItemCategory.MISMATCH,
                    mismatchType = MismatchType.PRICE,
                    sku = "SKU-2045",
                    invoiceItemName = "Wireless Barcode Scanner Pro",
                    systemItemName = "Wireless Barcode Scanner Pro",
                    invoiceQty = 2,
                    systemQty = 2,
                    invoiceUnitPrice = 89.00,
                    systemUnitPrice = 79.00,
                    note = "Unit price discrepancy: Invoice charges $89.00 vs $79.00 in system (+$20.00)."
                ),
                AuditedLineItemDraft(
                    category = LineItemCategory.MISMATCH,
                    mismatchType = MismatchType.PRODUCT_NAME,
                    sku = "SKU-3310",
                    invoiceItemName = "Bubble Cushion Wrap 24\" Perf",
                    systemItemName = "Foam Sheet Roll 12\" Std",
                    invoiceQty = 3,
                    systemQty = 3,
                    invoiceUnitPrice = 24.00,
                    systemUnitPrice = 22.00,
                    note = "Wrong product entry in system screenshot; item description & unit price differ."
                ),
                AuditedLineItemDraft(
                    category = LineItemCategory.MATCHED,
                    mismatchType = MismatchType.NONE,
                    sku = "SKU-1004",
                    invoiceItemName = "A4 Laser Invoice Paper (Ream)",
                    systemItemName = "A4 Laser Invoice Paper (Ream)",
                    invoiceQty = 10,
                    systemQty = 10,
                    invoiceUnitPrice = 8.50,
                    systemUnitPrice = 8.50,
                    note = "Exact quantity and unit price match."
                ),
                AuditedLineItemDraft(
                    category = LineItemCategory.MATCHED,
                    mismatchType = MismatchType.NONE,
                    sku = "SKU-5501",
                    invoiceItemName = "Tamper-Evident Poly Bags (100ct)",
                    systemItemName = "Tamper-Evident Poly Bags (100ct)",
                    invoiceQty = 6,
                    systemQty = 6,
                    invoiceUnitPrice = 19.50,
                    systemUnitPrice = 19.50,
                    note = "Exact quantity and unit price match."
                ),
                AuditedLineItemDraft(
                    category = LineItemCategory.MATCHED,
                    mismatchType = MismatchType.NONE,
                    sku = "SKU-7102",
                    invoiceItemName = "RFID Inventory Tags (Pack 250)",
                    systemItemName = "RFID Inventory Tags (Pack 250)",
                    invoiceQty = 4,
                    systemQty = 4,
                    invoiceUnitPrice = 32.00,
                    systemUnitPrice = 32.00,
                    note = "Exact quantity and unit price match."
                )
            )
        } else {
            listOf(
                AuditedLineItemDraft(
                    category = LineItemCategory.MISSING,
                    mismatchType = MismatchType.NONE,
                    sku = "SKU-6190",
                    invoiceItemName = "Cold-Chain Gel Packs (Box 24)",
                    systemItemName = "— Not in System SS —",
                    invoiceQty = 3,
                    systemQty = 0,
                    invoiceUnitPrice = 26.00,
                    systemUnitPrice = 0.0,
                    note = "Present on physical invoice ($78.00) but absent in system screenshot."
                ),
                AuditedLineItemDraft(
                    category = LineItemCategory.MISMATCH,
                    mismatchType = MismatchType.QTY_AND_PRICE,
                    sku = "SKU-2291",
                    invoiceItemName = "Insulated Pallet Liner 48x40",
                    systemItemName = "Insulated Pallet Liner 48x40",
                    invoiceQty = 12,
                    systemQty = 10,
                    invoiceUnitPrice = 15.50,
                    systemUnitPrice = 14.00,
                    note = "Both quantity (+2) and unit price (+$1.50) exceed system order values."
                ),
                AuditedLineItemDraft(
                    category = LineItemCategory.MATCHED,
                    mismatchType = MismatchType.NONE,
                    sku = "SKU-8040",
                    invoiceItemName = "Stretch Pallet Film 80ga (4-Roll)",
                    systemItemName = "Stretch Pallet Film 80ga (4-Roll)",
                    invoiceQty = 5,
                    systemQty = 5,
                    invoiceUnitPrice = 54.00,
                    systemUnitPrice = 54.00,
                    note = "Verified match across both documents."
                ),
                AuditedLineItemDraft(
                    category = LineItemCategory.MATCHED,
                    mismatchType = MismatchType.NONE,
                    sku = "SKU-8052",
                    invoiceItemName = "Corner Board Protectors (50ct)",
                    systemItemName = "Corner Board Protectors (50ct)",
                    invoiceQty = 4,
                    systemQty = 4,
                    invoiceUnitPrice = 28.00,
                    systemUnitPrice = 28.00,
                    note = "Verified match across both documents."
                )
            )
        }

        val invoiceTotal = sampleItems.sumOf { it.invoiceQty * it.invoiceUnitPrice }
        val systemTotal = sampleItems.sumOf { it.systemQty * it.systemUnitPrice }
        val missingCount = sampleItems.count { it.category == LineItemCategory.MISSING }
        val mismatchCount = sampleItems.count { it.category == LineItemCategory.MISMATCH }

        return AuditAnalysisResult(
            orderRef = resolvedOrderRef,
            vendorName = resolvedVendor,
            invoiceTotal = invoiceTotal,
            systemTotal = systemTotal,
            summaryNote = "Sample AI Preview: Detected $missingCount missing item(s) and $mismatchCount entry mismatch(es).",
            items = sampleItems,
            isSampleAiPreview = true
        )
    }
}
