package com.example.ledger.domain.model

/**
 * 一条收支流水（领域模型）。
 *
 * 该模型与数据库实体、网络 DTO 分离：金额统一用"分"为单位的 Long 存储，
 * 从根本上规避浮点运算误差，仅在展示时转换为元。
 *
 * @property id 主键，0 表示尚未持久化的新记录
 * @property amountInCents 金额（单位：分），恒为正数，方向由 [type] 决定
 * @property type 收支类型
 * @property categoryId 分类 id，对应 [Categories]
 * @property note 备注，可为空串
 * @property occurredAt 发生时间的时间戳（毫秒）
 */
data class Transaction(
    val id: Long = 0L,
    val amountInCents: Long,
    val type: TransactionType,
    val categoryId: String,
    val note: String = "",
    val occurredAt: Long = System.currentTimeMillis()
) {
    /** 带符号的金额（分）：收入为正、支出为负，用于汇总计算 */
    val signedAmountInCents: Long
        get() = if (type == TransactionType.INCOME) amountInCents else -amountInCents

    /** 分类详情，未知分类自动回退，保证 UI 层无需再做空判断 */
    val category: Category
        get() = Categories.findById(categoryId)
}
