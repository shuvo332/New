package com.example.data.repository

import com.example.data.ai.AuditAnalysisResult
import com.example.data.local.AuditDao
import com.example.data.local.AuditLineItemEntity
import com.example.data.local.AuditRecordEntity
import com.example.data.local.AuditWithItems
import com.example.data.local.BagStatus
import com.example.data.local.DiscrepancyWithAudit
import com.example.data.local.LineItemCategory
import com.example.data.local.MismatchType
import com.example.data.local.OrderBagEntity
import kotlinx.coroutines.flow.Flow

class AuditRepository(private val dao: AuditDao) {

    fun observeAllAudits(): Flow<List<AuditWithItems>> = dao.observeAllAuditsWithItems()

    fun observeAudit(auditId: Long): Flow<AuditWithItems?> = dao.observeAuditWithItems(auditId)

    fun observeAllDiscrepancies(): Flow<List<DiscrepancyWithAudit>> = dao.observeAllDiscrepancies()

    fun observeAllBags(): Flow<List<OrderBagEntity>> = dao.observeAllBags()

    suspend fun saveAnalysisResult(
        result: AuditAnalysisResult,
        screenshotLabel: String,
        invoiceLabel: String,
        screenshotUri: String?,
        invoiceUri: String?
    ): Long {
        val missingCount = result.items.count { it.category == LineItemCategory.MISSING }
        val mismatchCount = result.items.count { it.category == LineItemCategory.MISMATCH }
        val matchedCount = result.items.count { it.category == LineItemCategory.MATCHED }

        val record = AuditRecordEntity(
            orderRef = result.orderRef,
            vendorName = result.vendorName,
            createdAtMillis = System.currentTimeMillis(),
            screenshotLabel = screenshotLabel,
            invoiceLabel = invoiceLabel,
            screenshotUri = screenshotUri,
            invoiceUri = invoiceUri,
            invoiceTotal = result.invoiceTotal,
            systemTotal = result.systemTotal,
            missingCount = missingCount,
            mismatchCount = mismatchCount,
            matchedCount = matchedCount,
            summaryNote = result.summaryNote,
            isSampleAiPreview = result.isSampleAiPreview
        )

        val lineItems = result.items.map { draft ->
            AuditLineItemEntity(
                auditId = 0L,
                category = draft.category,
                mismatchType = draft.mismatchType,
                sku = draft.sku,
                invoiceItemName = draft.invoiceItemName,
                systemItemName = draft.systemItemName,
                invoiceQty = draft.invoiceQty,
                systemQty = draft.systemQty,
                invoiceUnitPrice = draft.invoiceUnitPrice,
                systemUnitPrice = draft.systemUnitPrice,
                note = draft.note,
                isResolved = false
            )
        }

        return dao.insertAuditWithItems(record, lineItems)
    }

    suspend fun setLineItemResolved(itemId: Long, resolved: Boolean) {
        dao.setLineItemResolved(itemId, resolved)
    }

    suspend fun deleteAudit(auditId: Long) {
        dao.deleteAuditById(auditId)
    }

    suspend fun addBag(
        bagCode: String,
        vendorName: String,
        itemCount: Int,
        declaredTotal: Double,
        notes: String
    ): Long {
        return dao.insertBag(
            OrderBagEntity(
                bagCode = bagCode.trim(),
                vendorName = vendorName.trim(),
                itemCount = itemCount.coerceAtLeast(1),
                declaredTotal = declaredTotal.coerceAtLeast(0.0),
                status = BagStatus.PENDING_AUDIT,
                notes = notes.trim(),
                updatedAtMillis = System.currentTimeMillis()
            )
        )
    }

    suspend fun updateBagStatus(bagId: Long, status: BagStatus) {
        dao.updateBagStatus(bagId, status, System.currentTimeMillis())
    }

    suspend fun deleteBag(bagId: Long) {
        dao.deleteBagById(bagId)
    }

    suspend fun ensureSeeded() {
        if (dao.getAuditCount() == 0) {
            seedInitialAudits()
        }
        if (dao.getBagCount() == 0) {
            seedInitialBags()
        }
    }

    private suspend fun seedInitialAudits() {
        val now = System.currentTimeMillis()
        val audit1Id = dao.insertAuditWithItems(
            record = AuditRecordEntity(
                orderRef = "ORD-2026-8814",
                vendorName = "Metro Fresh Wholesale",
                createdAtMillis = now - 3_600_000L * 3,
                screenshotLabel = "ERP_Order_8814.png",
                invoiceLabel = "INV_Metro_8814.jpg",
                invoiceTotal = 642.50,
                systemTotal = 558.50,
                missingCount = 1,
                mismatchCount = 2,
                matchedCount = 3,
                summaryNote = "Sample Record: 1 missing SKU and 2 price/qty discrepancies flagged.",
                isSampleAiPreview = true
            ),
            items = listOf(
                AuditLineItemEntity(
                    auditId = 0L,
                    category = LineItemCategory.MISSING,
                    mismatchType = MismatchType.NONE,
                    sku = "MF-309",
                    invoiceItemName = "Organic Espresso Beans 5lb",
                    systemItemName = "— Not in System SS —",
                    invoiceQty = 2,
                    systemQty = 0,
                    invoiceUnitPrice = 34.00,
                    systemUnitPrice = 0.0,
                    note = "Missing from POS screenshot; billed $68.00 on vendor invoice."
                ),
                AuditLineItemEntity(
                    auditId = 0L,
                    category = LineItemCategory.MISMATCH,
                    mismatchType = MismatchType.QUANTITY,
                    sku = "MF-112",
                    invoiceItemName = "Oat Barista Milk (Case 12)",
                    systemItemName = "Oat Barista Milk (Case 12)",
                    invoiceQty = 8,
                    systemQty = 6,
                    invoiceUnitPrice = 24.00,
                    systemUnitPrice = 24.00,
                    note = "Invoice lists 8 cases; system shows 6 cases (+2)."
                ),
                AuditLineItemEntity(
                    auditId = 0L,
                    category = LineItemCategory.MISMATCH,
                    mismatchType = MismatchType.PRICE,
                    sku = "MF-208",
                    invoiceItemName = "Compostable 12oz Cups (500ct)",
                    systemItemName = "Compostable 12oz Cups (500ct)",
                    invoiceQty = 2,
                    systemQty = 2,
                    invoiceUnitPrice = 42.25,
                    systemUnitPrice = 48.25,
                    note = "System unit price is $6.00 higher than invoiced contract price.",
                    isResolved = true
                ),
                AuditLineItemEntity(
                    auditId = 0L,
                    category = LineItemCategory.MATCHED,
                    sku = "MF-019",
                    invoiceItemName = "Unbleached Paper Sleeves (1000ct)",
                    systemItemName = "Unbleached Paper Sleeves (1000ct)",
                    invoiceQty = 4,
                    systemQty = 4,
                    invoiceUnitPrice = 22.00,
                    systemUnitPrice = 22.00,
                    note = "Exact match."
                ),
                AuditLineItemEntity(
                    auditId = 0L,
                    category = LineItemCategory.MATCHED,
                    sku = "MF-044",
                    invoiceItemName = "Raw Cane Sugar Sticks (Box)",
                    systemItemName = "Raw Cane Sugar Sticks (Box)",
                    invoiceQty = 5,
                    systemQty = 5,
                    invoiceUnitPrice = 18.00,
                    systemUnitPrice = 18.00,
                    note = "Exact match."
                ),
                AuditLineItemEntity(
                    auditId = 0L,
                    category = LineItemCategory.MATCHED,
                    sku = "MF-071",
                    invoiceItemName = "Matcha Ceremonial Tin 500g",
                    systemItemName = "Matcha Ceremonial Tin 500g",
                    invoiceQty = 3,
                    systemQty = 3,
                    invoiceUnitPrice = 46.00,
                    systemUnitPrice = 46.00,
                    note = "Exact match."
                )
            )
        )

        dao.insertAuditWithItems(
            record = AuditRecordEntity(
                orderRef = "ORD-2026-8790",
                vendorName = "Nordic Logistics Hardware",
                createdAtMillis = now - 3_600_000L * 26,
                screenshotLabel = "WMS_Dash_8790.png",
                invoiceLabel = "Nordic_Inv_8790.jpg",
                invoiceTotal = 410.00,
                systemTotal = 410.00,
                missingCount = 0,
                mismatchCount = 0,
                matchedCount = 3,
                summaryNote = "Sample Record: 100% clean match across all 3 SKU lines.",
                isSampleAiPreview = true
            ),
            items = listOf(
                AuditLineItemEntity(
                    auditId = 0L,
                    category = LineItemCategory.MATCHED,
                    sku = "NL-701",
                    invoiceItemName = "Heavy-Duty Pallet Strapping",
                    systemItemName = "Heavy-Duty Pallet Strapping",
                    invoiceQty = 4,
                    systemQty = 4,
                    invoiceUnitPrice = 45.00,
                    systemUnitPrice = 45.00,
                    note = "Verified."
                ),
                AuditLineItemEntity(
                    auditId = 0L,
                    category = LineItemCategory.MATCHED,
                    sku = "NL-715",
                    invoiceItemName = "Steel Crimping Seals (1000ct)",
                    systemItemName = "Steel Crimping Seals (1000ct)",
                    invoiceQty = 2,
                    systemQty = 2,
                    invoiceUnitPrice = 55.00,
                    systemUnitPrice = 55.00,
                    note = "Verified."
                ),
                AuditLineItemEntity(
                    auditId = 0L,
                    category = LineItemCategory.MATCHED,
                    sku = "NL-740",
                    invoiceItemName = "Safety Cutter Blades (Pack 50)",
                    systemItemName = "Safety Cutter Blades (Pack 50)",
                    invoiceQty = 4,
                    systemQty = 4,
                    invoiceUnitPrice = 30.00,
                    systemUnitPrice = 30.00,
                    note = "Verified."
                )
            )
        )

        // Update seeded bag with audit1Id reference
        dao.insertBags(
            listOf(
                OrderBagEntity(
                    bagCode = "BAG-8814-A",
                    vendorName = "Metro Fresh Wholesale",
                    itemCount = 6,
                    declaredTotal = 642.50,
                    status = BagStatus.DISCREPANCY_FOUND,
                    linkedAuditId = audit1Id,
                    notes = "Hold dispatch until missing espresso beans & milk qty are reconciled.",
                    updatedAtMillis = now - 3_600_000L * 2
                ),
                OrderBagEntity(
                    bagCode = "BAG-8790-C",
                    vendorName = "Nordic Logistics Hardware",
                    itemCount = 3,
                    declaredTotal = 410.00,
                    status = BagStatus.VERIFIED,
                    notes = "Passed 100% match audit. Ready for bay 4 pickup.",
                    updatedAtMillis = now - 3_600_000L * 18
                ),
                OrderBagEntity(
                    bagCode = "BAG-8841-B",
                    vendorName = "Apex Supply Co.",
                    itemCount = 8,
                    declaredTotal = 780.00,
                    status = BagStatus.PENDING_AUDIT,
                    notes = "Awaiting system screenshot vs printed invoice check.",
                    updatedAtMillis = now - 1_800_000L
                ),
                OrderBagEntity(
                    bagCode = "BAG-8752-D",
                    vendorName = "Pacific Office Depot",
                    itemCount = 11,
                    declaredTotal = 915.40,
                    status = BagStatus.DISPATCHED,
                    notes = "Verified and signed out by courier #19.",
                    updatedAtMillis = now - 3_600_000L * 42
                )
            )
        )
    }

    private suspend fun seedInitialBags() {
        // Bags are seeded alongside audits above; fallback if audits already existed.
        val now = System.currentTimeMillis()
        dao.insertBags(
            listOf(
                OrderBagEntity(
                    bagCode = "BAG-8841-B",
                    vendorName = "Apex Supply Co.",
                    itemCount = 8,
                    declaredTotal = 780.00,
                    status = BagStatus.PENDING_AUDIT,
                    notes = "Awaiting system screenshot vs printed invoice check.",
                    updatedAtMillis = now
                )
            )
        )
    }
}
