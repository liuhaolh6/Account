package com.example.ledger.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * 流水表的数据库访问对象。
 * 查询统一返回 Flow，让界面能够随数据变化自动刷新，避免手工触发重查。
 */
@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions ORDER BY occurred_at DESC, id DESC")
    fun observeAll(): Flow<List<TransactionEntity>>

    /**
     * 关键字模糊查询：同时匹配备注与分类 id，供搜索页使用。
     *
     * @param keyword 已转义的查询关键字（调用方负责去除空白）
     */
    @Query(
        """
        SELECT * FROM transactions
        WHERE note LIKE '%' || :keyword || '%' OR category_id LIKE '%' || :keyword || '%'
        ORDER BY occurred_at DESC, id DESC
        """
    )
    fun search(keyword: String): Flow<List<TransactionEntity>>

    @Query(
        """
        SELECT * FROM transactions
        WHERE occurred_at >= :startMillis AND occurred_at < :endMillis
        ORDER BY occurred_at DESC, id DESC
        """
    )
    fun observeBetween(startMillis: Long, endMillis: Long): Flow<List<TransactionEntity>>

    @Query(
        """
        SELECT * FROM transactions
        WHERE occurred_at >= :startMillis AND occurred_at < :endMillis
        """
    )
    suspend fun getBetween(startMillis: Long, endMillis: Long): List<TransactionEntity>

    /** 汇总指定范围内的收入总额，无数据时由 SQL 的 SUM 返回 null，交由 Kotlin 侧兜底 */
    @Query(
        """
        SELECT SUM(amount_in_cents) FROM transactions
        WHERE type = :type AND occurred_at >= :startMillis AND occurred_at < :endMillis
        """
    )
    suspend fun sumAmountByType(
        type: String,
        startMillis: Long,
        endMillis: Long
    ): Long?

    @Query(
        """
        SELECT category_id AS categoryId, SUM(amount_in_cents) AS amount
        FROM transactions
        WHERE type = :type AND occurred_at >= :startMillis AND occurred_at < :endMillis
        GROUP BY category_id
        ORDER BY amount DESC
        """
    )
    suspend fun sumByCategory(
        type: String,
        startMillis: Long,
        endMillis: Long
    ): List<CategorySum>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: TransactionEntity): Long

    @Update
    suspend fun update(entity: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long)
}

/**
 * 分类聚合查询的投影结果。
 *
 * @property categoryId 分类 id
 * @property amount 该分类合计金额（分）
 */
data class CategorySum(
    val categoryId: String,
    val amount: Long
)
