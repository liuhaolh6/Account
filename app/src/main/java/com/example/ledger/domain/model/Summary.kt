package com.example.ledger.domain.model

/**
 * 统计结果：某个时间范围内的汇总数据。
 *
 * @property totalIncomeInCents 总收入（分）
 * @property totalExpenseInCents 总支出（分）
 * @property categoryStats 按分类聚合的明细，已按金额从大到小排序
 */
data class Summary(
    val totalIncomeInCents: Long = 0L,
    val totalExpenseInCents: Long = 0L,
    val categoryStats: List<CategoryStat> = emptyList()
) {
    /** 结余（分），可能为负 */
    val balanceInCents: Long
        get() = totalIncomeInCents - totalExpenseInCents
}

/**
 * 单个分类的统计项。
 *
 * @property category 分类
 * @property amountInCents 该分类的合计金额（分）
 * @property ratio 占同类总额的比例，取值 0f~1f
 */
data class CategoryStat(
    val category: Category,
    val amountInCents: Long,
    val ratio: Float
)
