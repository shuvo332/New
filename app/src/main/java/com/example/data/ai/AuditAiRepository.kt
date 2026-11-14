package com.example.data.ai

import android.graphics.Bitmap
import com.example.data.local.LineItemCategory
import com.example.data.local.MismatchType

data class AuditedLineItemDraft(
    val category: LineItemCategory,
    val mismatchType: MismatchType,
    val sku: String,
    val invoiceItemName: String,
    val systemItemName: String,
    val invoiceQty: Int,
    val systemQty: Int,
    val invoiceUnitPrice: Double,
    val systemUnitPrice: Double,
    val note: String
)

data class AuditAnalysisResult(
    val orderRef: String,
    val vendorName: String,
    val invoiceTotal: Double,
    val systemTotal: Double,
    val summaryNote: String,
    val items: List<AuditedLineItemDraft>,
    val isSampleAiPreview: Boolean = true
)

interface AuditAiRepository {
    suspend fun compareScreenshotAndInvoice(
        screenshotBitmap: Bitmap,
        invoiceBitmap: Bitmap,
        orderRefHint: String,
        vendorHint: String,
        onStageProgress: (String) -> Unit
    ): AuditAnalysisResult
}
