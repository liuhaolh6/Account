package com.example.ledger.domain.model

/**
 * 收支类型。
 * 用枚举而非布尔值，便于后续扩展"转账"等类型时不影响已有逻辑。
 */
enum class TransactionType {
    /** 支出 */
    EXPENSE,

    /** 收入 */
    INCOME;

    /** 供界面展示的中文名称 */
    val displayName: String
        get() = when (this) {
            EXPENSE -> "支出"
            INCOME -> "收入"
        }
}
