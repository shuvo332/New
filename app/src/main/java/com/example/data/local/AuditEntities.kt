package com.example.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class LineItemCategory {
    MISSING,
    MISMATCH,
    MATCHED
}

enum class MismatchType {
    NONE,
    QUANTITY,
    PRICE,
    PRODUCT_NAME,
    QTY_AND_PRICE
}

enum class BagStatus {
    PENDING_AUDIT,
    DISCREPANCY_FOUND,
    VERIFIED,
    DISPATCHED
}

@Entity(tableName = "audit_records")
data class AuditRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val orderRef: String,
    val vendorName: String,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val screenshotLabel: String,
    val invoiceLabel: String,
    val screenshotUri: String? = null,
    val invoiceUri: String? = null,
    val invoiceTotal: Double,
    val systemTotal: Double,
    val missingCount: Int,
    val mismatchCount: Int,
    val matchedCount: Int,
    val summaryNote: String,
    val isSampleAiPreview: Boolean = true
) {
    val netVariance: Double
        get() = invoiceTotal - systemTotal

    val matchRatePercent: Int
        get() {
            val total = missingCount + mismatchCount + matchedCount
            return if (total <= 0) 100 else ((matchedCount * 100.0) / total).toInt()
        }

    val hasIssues: Boolean
        get() = missingCount > 0 || mismatchCount > 0
}

@Entity(
    tableName = "audit_line_items",
    foreignKeys = [
        ForeignKey(
            entity = AuditRecordEntity::class,
            parentColumns = ["id"],
            childColumns = ["auditId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("auditId")]
)
data class AuditLineItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val auditId: Long,
    val category: LineItemCategory,
    val mismatchType: MismatchType = MismatchType.NONE,
    val sku: String,
    val invoiceItemName: String,
    val systemItemName: String,
    val invoiceQty: Int,
    val systemQty: Int,
    val invoiceUnitPrice: Double,
    val systemUnitPrice: Double,
    val note: String,
    val isResolved: Boolean = false
) {
    val invoiceLineTotal: Double get() = invoiceQty * invoiceUnitPrice
    val systemLineTotal: Double get() = systemQty * systemUnitPrice
    val lineVariance: Double get() = invoiceLineTotal - systemLineTotal
}

@Entity(tableName = "order_bags")
data class OrderBagEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val bagCode: String,
    val vendorName: String,
    val itemCount: Int,
    val declaredTotal: Double,
    val status: BagStatus = BagStatus.PENDING_AUDIT,
    val linkedAuditId: Long? = null,
    val notes: String = "",
    val updatedAtMillis: Long = System.currentTimeMillis()
)
