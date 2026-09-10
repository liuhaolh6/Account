package com.example.ledger.data.local

import com.example.ledger.domain.model.Transaction
import com.example.ledger.domain.model.TransactionType

/**
 * 数据库实体与领域模型之间的转换。
 * 集中放置转换逻辑，避免在 Repository 中散落大量字段映射代码。
 */

/** 将数据库实体转换为领域模型 */
fun TransactionEntity.toDomain(): Transaction = Transaction(
    id = id,
    amountInCents = amountInCents,
    type = parseType(type),
    categoryId = categoryId,
    note = note,
    occurredAt = occurredAt
)

/** 将领域模型转换为数据库实体 */
fun Transaction.toEntity(): TransactionEntity = TransactionEntity(
    id = id,
    amountInCents = amountInCents,
    type = type.name,
    categoryId = categoryId,
    note = note,
    occurredAt = occurredAt
)

/**
 * 解析数据库中的类型字符串。
 * 遇到无法识别的历史脏数据时回退为支出，保证界面不崩溃。
 */
private fun parseType(raw: String): TransactionType =
    runCatching { TransactionType.valueOf(raw) }.getOrDefault(TransactionType.EXPENSE)
