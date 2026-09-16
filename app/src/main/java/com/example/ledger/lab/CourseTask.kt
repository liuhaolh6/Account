package com.example.ledger.lab

/**
 * 课程任务（实验1 领域模型）。
 *
 * 用 data class 而非普通 class：编译器自动生成 equals/hashCode/copy/toString，
 * Compose 判等重组时依赖 equals，数据类能避免"内容未变却整树重组"的性能损耗。
 *
 * @property id 任务主键
 * @property title 任务标题，非空
 * @property owner 负责人；**可空**，表示尚未指派
 * @property completed 是否已完成，给出默认值以减少调用方样板代码
 * @property priority 优先级，用于拓展任务中的样式区分
 */
data class CourseTask(
    val id: Int,
    val title: String,
    val owner: String?,
    val completed: Boolean = false,
    val priority: Priority = Priority.NORMAL
) {
    /** 任务优先级：决定 TaskCard 左侧色条与标签样式 */
    enum class Priority { HIGH, NORMAL, LOW }
}

/**
 * 把可空的 owner 渲染成可直接展示的文本。
 *
 * 三步收敛空值，全程不用 `!!`：
 * 1. `?.` 安全调用：owner 为 null 时整条链直接返回 null，不会 NPE；
 * 2. `trim()` + `takeIf { it.isNotEmpty() }`：把 `""`、`"   "` 这类"伪空值"也视为未分配，
 *    否则界面上会出现一片空白，用户无法判断是没数据还是渲染失败；
 * 3. `?:` Elvis：把最终仍为 null 的情况统一兜底为中文文案。
 *
 * @param task 待展示的任务
 * @return 可直接显示在卡片上的负责人文本，永不为空串
 */
fun displayOwner(task: CourseTask): String =
    task.owner?.trim()?.takeIf { it.isNotEmpty() } ?: "未分配"

/**
 * 三条测试数据：覆盖"已完成 + 有负责人""进行中 + 有负责人""进行中 + 未指派"三种组合。
 * 其中 3 号任务的 owner 显式为 null，专门用于验证空安全分支。
 */
val sampleTasks: List<CourseTask> = listOf(
    CourseTask(
        id = 1,
        title = "完成 Compose 布局练习",
        owner = "刘浩",
        completed = true,
        priority = CourseTask.Priority.NORMAL
    ),
    CourseTask(
        id = 2,
        title = "实现 TaskCard 可复用组件",
        owner = "王芳",
        completed = false,
        priority = CourseTask.Priority.HIGH
    ),
    CourseTask(
        id = 3,
        title = "整理实验报告与截图",
        owner = null,
        completed = false,
        priority = CourseTask.Priority.LOW
    )
)
