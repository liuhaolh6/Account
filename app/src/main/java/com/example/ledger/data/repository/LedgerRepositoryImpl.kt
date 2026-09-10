package com.example.ledger.data.repository

import com.example.ledger.data.local.LedgerDatabase
import com.example.ledger.data.local.TransactionDao
import com.example.ledger.data.local.toDomain
import com.example.ledger.data.local.toEntity
import com.example.ledger.domain.model.CategoryStat
import com.example.ledger.domain.model.Categories
import com.example.ledger.domain.model.Summary
import com.example.ledger.domain.model.Transaction
import com.example.ledger.domain.model.TransactionType
import com.example.ledger.domain.repository.LedgerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * 基于 Room 的记账仓库实现。
 *
 * 所有挂起操作都用 runCatching 包裹并转为 [Result]，避免异常直接抛到 UI 层导致崩溃；
 * 页面只需根据 Result 展示对应的中文提示。
 *
 * @param dao 流水表访问对象
 */
class LedgerRepositoryImpl(private val dao: TransactionDao) : LedgerRepository {

    override fun observeAll(): Flow<List<Transaction>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun search(keyword: String): Flow<List<Transaction>> {
        val trimmed = keyword.trim()
        // 关键字为空时直接返回全量，避免构造无意义的 LIKE '%%' 查询
        return if (trimmed.isEmpty()) {
            observeAll()
        } else {
            dao.search(trimmed).map { list -> list.map { it.toDomain() } }
        }
    }

    override fun observeBetween(startMillis: Long, endMillis: Long): Flow<List<Transaction>> =
        dao.observeBetween(startMillis, endMillis).map { list -> list.map { it.toDomain() } }

    override suspend fun save(transaction: Transaction): Result<Long> = runCatching {
        val entity = transaction.toEntity()
        if (entity.id == 0L) {
            dao.insert(entity)
        } else {
            dao.update(entity)
            entity.id
        }
    }

    override suspend fun delete(id: Long): Result<Unit> = runCatching {
        dao.deleteById(id)
    }

    override suspend fun summarize(
        startMillis: Long,
        endMillis: Long,
        type: TransactionType
    ): Result<Summary> = runCatching {
        val typeKey = type.name
        val totalIncome = if (type == TransactionType.INCOME) {
            dao.sumAmountByType(typeKey, startMillis, endMillis) ?: 0L
        } else {
            0L
        }
        val totalExpense = if (type == TransactionType.EXPENSE) {
            dao.sumAmountByType(typeKey, startMillis, endMillis) ?: 0L
        } else {
            0L
        }
        val sums = dao.sumByCategory(typeKey, startMillis, endMillis)
        val total = sums.sumOf { it.amount }
        val stats = sums.map { item ->
            CategoryStat(
                category = Categories.findById(item.categoryId),
                amountInCents = item.amount,
                // 用 Double 先做除法再转 Float，避免整数除法把所有比例压成 0
                ratio = if (total > 0L) (item.amount.toDouble() / total).toFloat() else 0f
            )
        }
        Summary(
            totalIncomeInCents = totalIncome,
            totalExpenseInCents = totalExpense,
            categoryStats = stats
        )
    }

    companion object {
        /**
         * 通过数据库实例创建仓库，供应用启动时组装依赖。
         *
         * @param database 应用数据库
         * @return 仓库实例
         */
        fun create(database: LedgerDatabase): LedgerRepository =
            LedgerRepositoryImpl(database.transactionDao())
    }
}
