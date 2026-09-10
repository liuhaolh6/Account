package com.example.ledger.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 流水表实体。
 * 时间字段建立索引，因为列表与统计都按时间范围查询，这是最高频的过滤条件。
 */
@Entity(
    tableName = "transactions",
    indices = [Index(value = ["occurred_at"]), Index(value = ["category_id"])]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0L,

    /** 金额（分），恒为正数 */
    @ColumnInfo(name = "amount_in_cents")
    val amountInCents: Long,

    /** 收支类型，存为字符串以便数据库可读 */
    @ColumnInfo(name = "type")
    val type: String,

    @ColumnInfo(name = "category_id")
    val categoryId: String,

    @ColumnInfo(name = "note")
    val note: String,

    @ColumnInfo(name = "occurred_at")
    val occurredAt: Long,

    /** 记录创建时间，用于排查数据异常 */
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
