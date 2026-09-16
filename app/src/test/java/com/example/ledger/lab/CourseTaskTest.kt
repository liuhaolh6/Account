package com.example.ledger.lab

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * 实验1 纯逻辑单测：验证 data class 与空安全表达式。
 *
 * 这些断言不依赖 Android 框架，因此放在 test/ 目录下用 JVM 直接跑。
 */
class CourseTaskTest {

    @Test
    fun `owner 为 null 时显示未分配`() {
        val task = CourseTask(id = 1, title = "整理实验报告", owner = null)
        assertEquals("未分配", displayOwner(task))
    }

    @Test
    fun `owner 为空白串时同样显示未分配`() {
        // 空串与非空串都是"没有负责人"，必须收敛成同一文案，否则界面会出现空白区域
        assertEquals("未分配", displayOwner(CourseTask(2, "任务", "   ")))
        assertEquals("未分配", displayOwner(CourseTask(3, "任务", "")))
    }

    @Test
    fun `owner 有值时去除首尾空格并原样返回`() {
        assertEquals("刘浩", displayOwner(CourseTask(4, "任务", "  刘浩 ")))
    }

    @Test
    fun `copy 返回新对象且原对象不被修改`() {
        val origin = CourseTask(1, "完成任务卡", "王芳")
        val done = origin.copy(completed = true)

        assertFalse(origin.completed)
        assertEquals(true, done.completed)
        assertEquals(origin.id, done.id)
    }

    @Test
    fun `data class 按内容判等`() {
        // Compose 依赖 equals 判断是否需要重组，此处即是该行为的依据
        assertEquals(
            CourseTask(1, "完成任务卡", "王芳"),
            CourseTask(1, "完成任务卡", "王芳")
        )
        assertNull(CourseTask(1, "完成任务卡", null).owner)
    }

    @Test
    fun `sampleTasks 含三条数据且恰有一条未分配负责人`() {
        assertEquals(3, sampleTasks.size)
        assertEquals(1, sampleTasks.count { it.owner == null })
        assertEquals("未分配", displayOwner(sampleTasks.first { it.owner == null }))
    }
}
