package com.example.data.local

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

data class AuditWithItems(
    @Embedded val audit: AuditRecordEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "auditId"
    )
    val items: List<AuditLineItemEntity>
)

data class DiscrepancyWithAudit(
    @Embedded val item: AuditLineItemEntity,
    @Relation(
        parentColumn = "auditId",
        entityColumn = "id"
    )
    val audit: AuditRecordEntity?
)

@Dao
interface AuditDao {

    @Transaction
    @Query("SELECT * FROM audit_records ORDER BY createdAtMillis DESC")
    fun observeAllAuditsWithItems(): Flow<List<AuditWithItems>>

    @Transaction
    @Query("SELECT * FROM audit_records WHERE id = :auditId")
    fun observeAuditWithItems(auditId: Long): Flow<AuditWithItems?>

    @Transaction
    @Query("SELECT * FROM audit_line_items WHERE category != 'MATCHED' ORDER BY isResolved ASC, id DESC")
    fun observeAllDiscrepancies(): Flow<List<DiscrepancyWithAudit>>

    @Query("SELECT COUNT(*) FROM audit_records")
    suspend fun getAuditCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditRecord(record: AuditRecordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLineItems(items: List<AuditLineItemEntity>)

    @Transaction
    suspend fun insertAuditWithItems(
        record: AuditRecordEntity,
        items: List<AuditLineItemEntity>
    ): Long {
        val auditId = insertAuditRecord(record)
        insertLineItems(items.map { it.copy(auditId = auditId) })
        return auditId
    }

    @Query("UPDATE audit_line_items SET isResolved = :resolved WHERE id = :itemId")
    suspend fun setLineItemResolved(itemId: Long, resolved: Boolean)

    @Query("DELETE FROM audit_records WHERE id = :auditId")
    suspend fun deleteAuditById(auditId: Long)

    // Bag / Order Management
    @Query("SELECT * FROM order_bags ORDER BY updatedAtMillis DESC")
    fun observeAllBags(): Flow<List<OrderBagEntity>>

    @Query("SELECT COUNT(*) FROM order_bags")
    suspend fun getBagCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBag(bag: OrderBagEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBags(bags: List<OrderBagEntity>)

    @Update
    suspend fun updateBag(bag: OrderBagEntity)

    @Query("UPDATE order_bags SET status = :status, updatedAtMillis = :updatedAt WHERE id = :bagId")
    suspend fun updateBagStatus(bagId: Long, status: BagStatus, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM order_bags WHERE id = :bagId")
    suspend fun deleteBagById(bagId: Long)
}
