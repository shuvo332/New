package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters

class AuditTypeConverters {
    @TypeConverter
    fun fromLineCategory(value: LineItemCategory): String = value.name

    @TypeConverter
    fun toLineCategory(value: String): LineItemCategory = LineItemCategory.valueOf(value)

    @TypeConverter
    fun fromMismatchType(value: MismatchType): String = value.name

    @TypeConverter
    fun toMismatchType(value: String): MismatchType = MismatchType.valueOf(value)

    @TypeConverter
    fun fromBagStatus(value: BagStatus): String = value.name

    @TypeConverter
    fun toBagStatus(value: String): BagStatus = BagStatus.valueOf(value)
}

@Database(
    entities = [
        AuditRecordEntity::class,
        AuditLineItemEntity::class,
        OrderBagEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(AuditTypeConverters::class)
abstract class AuditorDatabase : RoomDatabase() {
    abstract fun auditDao(): AuditDao

    companion object {
        @Volatile
        private var INSTANCE: AuditorDatabase? = null

        fun getInstance(context: Context): AuditorDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AuditorDatabase::class.java,
                    "invoice_auditor.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
