package com.example.ledger.lab

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ledger.ui.theme.MyLedgerTheme

/**
 * 实验1 主界面：列出全部课程任务。
 *
 * 状态归属说明：勾选状态属于"界面状态"，且当前实验不涉及持久化，
 * 因此用 remember + mutableStateOf 放在 Composable 内持有；
 * 一旦接入 ViewModel，这里应改为 StateFlow + collectAsStateWithLifecycle。
 *
 * @param tasks 待展示任务，默认取 [sampleTasks]
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Lab1Screen(tasks: List<CourseTask> = sampleTasks) {
    // 用不可变列表重建可变状态：点击卡片时以 copy 替换对应元素，保持单向数据流
    var currentTasks by remember(tasks) { mutableStateOf(tasks) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("实验1 · 课程任务") }) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    text = "共 ${currentTasks.size} 条，已完成 ${currentTasks.count { it.completed }} 条",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            items(items = currentTasks, key = { it.id }) { task ->
                TaskCard(
                    title = task.title,
                    owner = displayOwner(task),
                    completed = task.completed,
                    priority = task.priority,
                    // 事件向上传递：子组件只报告"被点击了"，改哪个数据由父级决定
                    onClick = {
                        currentTasks = currentTasks.map { item ->
                            if (item.id == task.id) item.copy(completed = !item.completed) else item
                        }
                    }
                )
            }
        }
    }
}

/** 整屏 Preview：可脱离模拟器直接查看三条任务的渲染结果 */
@Preview(name = "Lab1Screen - 三条任务", showBackground = true, showSystemUi = true)
@Composable
private fun Lab1ScreenPreview() {
    MyLedgerTheme {
        Lab1Screen()
    }
}
