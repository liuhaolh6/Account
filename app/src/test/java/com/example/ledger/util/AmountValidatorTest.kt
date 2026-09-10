package com.example.ledger.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * 金额校验工具单元测试。
 * 金额是账目应用的核心数据，这些边界用例必须覆盖，避免出现"1 元"变成"0 元"这类问题。
 */
class AmountValidatorTest {

    @Test
    fun `整数金额解析为分`() {
        assertEquals(1200L, AmountValidator.parseToCents("12"))
    }

    @Test
    fun `一位小数解析为分`() {
        assertEquals(50L, AmountValidator.parseToCents("0.5"))
    }

    @Test
    fun `两位小数解析为分`() {
        assertEquals(1250L, AmountValidator.parseToCents("12.50"))
    }

    @Test
    fun `允许首尾空格`() {
        assertEquals(999L, AmountValidator.parseToCents("  9.99  "))
    }

    @Test
    fun `空字符串返回null`() {
        assertNull(AmountValidator.parseToCents(""))
    }

    @Test
    fun `零金额返回null`() {
        assertNull(AmountValidator.parseToCents("0"))
        assertNull(AmountValidator.parseToCents("0.00"))
    }

    @Test
    fun `负数返回null`() {
        assertNull(AmountValidator.parseToCents("-5"))
    }

    @Test
    fun `超过两位小数返回null`() {
        assertNull(AmountValidator.parseToCents("1.234"))
    }

    @Test
    fun `非数字返回null`() {
        assertNull(AmountValidator.parseToCents("abc"))
        assertNull(AmountValidator.parseToCents("12元"))
    }

    @Test
    fun `超出上限返回null`() {
        assertNull(AmountValidator.parseToCents("99999999999"))
    }

    @Test
    fun `格式化分到元保留两位小数`() {
        assertEquals("12.50", AmountValidator.formatCents(1250L))
        assertEquals("0.01", AmountValidator.formatCents(1L))
    }

    @Test
    fun `格式化负数在带符号时输出负号`() {
        assertEquals("-12.50", AmountValidator.formatCents(-1250L, withSign = true))
        assertEquals("12.50", AmountValidator.formatCents(-1250L, withSign = false))
    }

    @Test
    fun `规范性输入去除多余空格`() {
        assertEquals("12.50", AmountValidator.normalize(" 12.5 "))
    }

    @Test
    fun `备注超长给出中文提示`() {
        val longNote = "a".repeat(51)
        assertNotNull(AmountValidator.validateNote(longNote))
    }

    @Test
    fun `正常备注校验通过`() {
        assertNull(AmountValidator.validateNote("午饭"))
    }
}
