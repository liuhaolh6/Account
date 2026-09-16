package com.example.ledger.lab

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ledger.ui.theme.MyLedgerTheme

/**
 * 可复用任务卡片：把一行任务渲染成带状态与优先级的卡片。
 *
 * 组件只接收"数据 + 回调"，不持有任何状态，因此可以在任意页面复用，
 * 也能脱离 Activity 直接在 Preview 中渲染。
 *
 * @param title 任务标题
 * @param owner 负责人展示文本，建议由 [displayOwner] 生成
 * @param completed 是否已完成，决定文字删除线与状态标签
 * @param onClick 点击回调，默认空实现以便只读场景复用
 * @param priority 优先级，决定左侧色条颜色
 * @param modifier 外部修饰符
 */
@Composable
fun TaskCard(
    title: String,
    owner: String,
    completed: Boolean,
    onClick: () -> Unit = {},
    priority: CourseTask.Priority = CourseTask.Priority.NORMAL,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
        ) {
            // 左侧优先级色条：用 4dp 宽度区分优先级，不额外占用布局空间
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(4.dp)
                    .background(priorityColor(priority))
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    // 已完成用删除线表达，符合通用认知，且不依赖额外图标资源
                    textDecoration = if (completed) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "负责人：$owner",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            StatusBadge(
                text = if (completed) "已完成" else "进行中",
                containerColor = if (completed) CompletedBadgeColor else OngoingBadgeColor
            )
        }
    }
}

/**
 * 右侧状态标签。
 *
 * @param text 标签文本
 * @param containerColor 标签背景色
 */
@Composable
private fun StatusBadge(text: String, containerColor: Color) {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .padding(vertical = 20.dp, horizontal = 12.dp)
            .background(containerColor, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = Color.White
        )
    }
}

/**
 * 优先级到颜色的映射。
 *
 * @param priority 任务优先级
 * @return 该优先级对应的强调色
 */
private fun priorityColor(priority: CourseTask.Priority): Color = when (priority) {
    CourseTask.Priority.HIGH -> PriorityHigh
    CourseTask.Priority.NORMAL -> PriorityNormal
    CourseTask.Priority.LOW -> PriorityLow
}

private val PriorityHigh = Color(0xFFD32F2F)
private val PriorityNormal = Color(0xFF1E88E5)
private val PriorityLow = Color(0xFF9E9E9E)
private val OngoingBadgeColor = Color(0xFF1E88E5)
private val CompletedBadgeColor = Color(0xFF2E7D32)

/** Preview 1：进行中任务（含负责人） */
@Preview(name = "TaskCard - 进行中", showBackground = true)
@Composable
private fun TaskCardOngoingPreview() {
    MyLedgerTheme {
        TaskCard(
            title = "实现 TaskCard 可复用组件",
            owner = displayOwner(sampleTasks[1]),
            completed = false,
            priority = CourseTask.Priority.HIGH,
            modifier = Modifier.padding(12.dp)
        )
    }
}

/** Preview 2：已完成任务（带删除线与绿色标签） */
@Preview(name = "TaskCard - 已完成", showBackground = true)
@Composable
private fun TaskCardCompletedPreview() {
    MyLedgerTheme {
        TaskCard(
            title = "完成 Compose 布局练习",
            owner = displayOwner(sampleTasks[0]),
            completed = true,
            priority = CourseTask.Priority.NORMAL,
            modifier = Modifier.padding(12.dp)
        )
    }
}

/** Preview 3：owner 为 null 的任务，验证空安全兜底文案 */
@Preview(name = "TaskCard - 未分配", showBackground = true)
@Composable
private fun TaskCardUnassignedPreview() {
    MyLedgerTheme {
        TaskCard(
            title = "整理实验报告与截图",
            owner = displayOwner(sampleTasks[2]),
            completed = false,
            priority = CourseTask.Priority.LOW,
            modifier = Modifier.padding(12.dp)
        )
    }
}
