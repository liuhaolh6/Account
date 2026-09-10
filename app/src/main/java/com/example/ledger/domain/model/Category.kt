package com.example.ledger.domain.model

/**
 * 账目分类定义。
 * 分类以常量表形式内置，是为了让统计口径稳定：分类一旦变化，历史数据的统计结果会失真。
 * [icon] 使用 Material Icons 的名称，由 UI 层负责映射为具体的 ImageVector。
 */
data class Category(
    val id: String,
    val name: String,
    val type: TransactionType,
    val icon: String,
    /** 用于饼图等可视化的固定颜色（ARGB），保证同一分类在各页面颜色一致 */
    val color: Long
)

object Categories {

    val EXPENSE: List<Category> = listOf(
        Category("food", "餐饮", TransactionType.EXPENSE, "Restaurant", 0xFFEF5350),
        Category("traffic", "交通", TransactionType.EXPENSE, "DirectionsBus", 0xFF42A5F5),
        Category("shopping", "购物", TransactionType.EXPENSE, "ShoppingBag", 0xFFAB47BC),
        Category("housing", "居住", TransactionType.EXPENSE, "Home", 0xFF26A69A),
        Category("entertainment", "娱乐", TransactionType.EXPENSE, "SportsEsports", 0xFFFFA726),
        Category("medical", "医疗", TransactionType.EXPENSE, "LocalHospital", 0xFFEC407A),
        Category("study", "学习", TransactionType.EXPENSE, "MenuBook", 0xFF7E57C2),
        Category("other_expense", "其他", TransactionType.EXPENSE, "MoreHoriz", 0xFF78909C)
    )

    val INCOME: List<Category> = listOf(
        Category("salary", "工资", TransactionType.INCOME, "AccountBalanceWallet", 0xFF66BB6A),
        Category("bonus", "奖金", TransactionType.INCOME, "CardGiftcard", 0xFF9CCC65),
        Category("parttime", "兼职", TransactionType.INCOME, "WorkOutline", 0xFF26C6DA),
        Category("investment", "理财", TransactionType.INCOME, "TrendingUp", 0xFF8D6E63),
        Category("other_income", "其他", TransactionType.INCOME, "MoreHoriz", 0xFF78909C)
    )

    /**
     * 按收支类型返回可选分类列表。
     *
     * @param type 收支类型
     * @return 该类型下的全部分类
     */
    fun of(type: TransactionType): List<Category> =
        when (type) {
            TransactionType.EXPENSE -> EXPENSE
            TransactionType.INCOME -> INCOME
        }

    /**
     * 按分类 id 查找分类，找不到时回退到该类型的"其他"分类，
     * 避免历史数据中的未知分类导致界面崩溃。
     *
     * @param id 分类 id
     * @return 匹配的分类，兜底为支出类"其他"
     */
    fun findById(id: String): Category =
        (EXPENSE + INCOME).firstOrNull { it.id == id } ?: EXPENSE.last()
}
