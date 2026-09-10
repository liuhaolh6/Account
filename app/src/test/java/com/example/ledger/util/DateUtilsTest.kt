package com.example.ledger.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 时间工具单元测试。
 * 重点验证左闭右开区间的边界，这是月度统计最容易出错的地方。
 */
class DateUtilsTest {

    @Test
    fun `月初为当月一日零点`() {
        val someDay = 1_725_235_200_000L
        val start = DateUtils.startOfMonth(someDay)
        assertTrue(start <= someDay)
        assertEquals(start, DateUtils.startOfMonth(start))
    }

    @Test
    fun `月末等于下月初`() {
        val someDay = 1_725_235_200_000L
        val end = DateUtils.endOfMonth(someDay)
        assertEquals(end, DateUtils.shiftMonth(DateUtils.startOfMonth(someDay), 1))
    }

    @Test
    fun `月份偏移正负方向正确`() {
        val start = DateUtils.startOfMonth()
        val previous = DateUtils.shiftMonth(start, -1)
        val next = DateUtils.shiftMonth(start, 1)
        assertTrue(previous < start)
        assertTrue(next > start)
    }

    @Test
    fun `当日区间覆盖当天且不跨越次日`() {
        val dayStart = DateUtils.startOfDay()
        val dayEnd = DateUtils.endOfDay()
        assertTrue(dayEnd > dayStart)
        assertEquals(dayStart, DateUtils.dateKey(dayStart + 3_600_000L))
    }

    @Test
    fun `日期格式化输出符合预期`() {
        val millis = DateUtils.startOfDay()
        assertEquals(10, DateUtils.formatDate(millis).length)
        assertEquals(5, DateUtils.formatMonthDay(millis).length)
    }
}
