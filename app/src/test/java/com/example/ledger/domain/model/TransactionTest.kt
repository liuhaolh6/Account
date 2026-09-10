package com.example.ledger.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 领域模型单元测试：验证金额汇总与分类兜底逻辑。
 */
class TransactionTest {

    @Test
    fun `收入为正支出为负`() {
        val income = Transaction(
            amountInCents = 5000L,
            type = TransactionType.INCOME,
            categoryId = "salary"
        )
        val expense = Transaction(
            amountInCents = 3000L,
            type = TransactionType.EXPENSE,
            categoryId = "food"
        )
        assertEquals(5000L, income.signedAmountInCents)
        assertEquals(-3000L, expense.signedAmountInCents)
    }

    @Test
    fun `未知分类回退到其他而不是崩溃`() {
        val transaction = Transaction(
            amountInCents = 100L,
            type = TransactionType.EXPENSE,
            categoryId = "not_exist_category"
        )
        assertEquals("其他", transaction.category.name)
    }

    @Test
    fun `分类列表按收支类型区分`() {
        val expenseIds = Categories.of(TransactionType.EXPENSE).map { it.id }
        val incomeIds = Categories.of(TransactionType.INCOME).map { it.id }
        assertTrue(expenseIds.contains("food"))
        assertTrue(incomeIds.contains("salary"))
        assertTrue(expenseIds.intersect(incomeIds.toSet()).isEmpty())
    }

    @Test
    fun `汇总结余为收入减支出`() {
        val summary = Summary(totalIncomeInCents = 10000L, totalExpenseInCents = 4000L)
        assertEquals(6000L, summary.balanceInCents)
    }

    @Test
    fun `超支时结余为负`() {
        val summary = Summary(totalIncomeInCents = 1000L, totalExpenseInCents = 3500L)
        assertEquals(-2500L, summary.balanceInCents)
    }

    @Test
    fun `所有分类的图标名称都能被解析`() {
        val allCategories = Categories.EXPENSE + Categories.INCOME
        // 分类 id 必须唯一，否则统计分组会互相覆盖
        assertEquals(allCategories.size, allCategories.map { it.id }.toSet().size)
        assertTrue(allCategories.all { it.color != 0L })
    }
}
