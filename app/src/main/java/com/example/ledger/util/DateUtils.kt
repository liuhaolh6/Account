package com.example.ledger.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * 时间范围计算与日期格式化工具。
 * 以"自然月"为统计口径，所有边界都使用左闭右开区间 [start, end)，避免月初/月末数据重复或遗漏。
 */
object DateUtils {

    private val dayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
    private val monthDayFormat = SimpleDateFormat("MM-dd", Locale.CHINA)
    private val monthFormat = SimpleDateFormat("yyyy年MM月", Locale.CHINA)
    private val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA)

    /**
     * 获取指定时间所在月份的第一毫秒。
     *
     * @param millis 任意时间戳
     * @return 当月 1 日 00:00:00.000 的时间戳
     */
    fun startOfMonth(millis: Long = System.currentTimeMillis()): Long =
        calendarOf(millis).apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    /**
     * 获取指定时间所在月份的下个月第一毫秒，作为左闭右开区间的上界。
     *
     * @param millis 任意时间戳
     * @return 下月 1 日 00:00:00.000 的时间戳
     */
    fun endOfMonth(millis: Long = System.currentTimeMillis()): Long =
        calendarOf(startOfMonth(millis)).apply {
            add(Calendar.MONTH, 1)
        }.timeInMillis

    /**
     * 获取指定时间当天的第一毫秒。
     *
     * @param millis 任意时间戳
     * @return 当日 00:00:00.000 的时间戳
     */
    fun startOfDay(millis: Long = System.currentTimeMillis()): Long =
        calendarOf(millis).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    /**
     * 获取指定时间次日的第一毫秒。
     *
     * @param millis 任意时间戳
     * @return 次日 00:00:00.000 的时间戳
     */
    fun endOfDay(millis: Long = System.currentTimeMillis()): Long =
        calendarOf(startOfDay(millis)).apply {
            add(Calendar.DAY_OF_MONTH, 1)
        }.timeInMillis

    /**
     * 月份偏移，用于统计页切换上月/下月。
     *
     * @param millis 基准时间戳
     * @param offset 偏移月数，负数表示往前
     * @return 偏移后所在月份的第一毫秒
     */
    fun shiftMonth(millis: Long, offset: Int): Long =
        calendarOf(startOfMonth(millis)).apply { add(Calendar.MONTH, offset) }.timeInMillis

    /** 格式化为 yyyy-MM-dd */
    fun formatDate(millis: Long): String = dayFormat.format(Date(millis))

    /** 格式化为 MM-dd */
    fun formatMonthDay(millis: Long): String = monthDayFormat.format(Date(millis))

    /** 格式化为 yyyy年MM月 */
    fun formatMonth(millis: Long = System.currentTimeMillis()): String =
        monthFormat.format(Date(millis))

    /** 格式化为 yyyy-MM-dd HH:mm */
    fun formatDateTime(millis: Long): String = dateTimeFormat.format(Date(millis))

    /**
     * 将时间戳规整到当天 00:00，便于以"日"为粒度做分组。
     *
     * @param millis 任意时间戳
     * @return 当日零点的时间戳
     */
    fun dateKey(millis: Long): Long = startOfDay(millis)

    private fun calendarOf(millis: Long): Calendar =
        Calendar.getInstance(Locale.CHINA).apply { timeInMillis = millis }
}
