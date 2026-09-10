package com.example.ledger.ui.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.ledger.domain.model.CategoryStat
import com.example.ledger.util.AmountValidator

/**
 * 分类占比环形图。
 *
 * 直接用 Canvas 手绘而非引入第三方图表库：本项目的可视化需求简单，
 * 自绘可避免额外依赖，也便于说明绘制原理。
 *
 * @param stats 分类统计数据，占比之和约为 1
 * @param modifier 外部修饰符
 */
@Composable
fun CategoryDonutChart(stats: List<CategoryStat>, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.4f),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(200.dp)) {
            val strokeWidth = 36.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val topLeft = Offset(
                x = (size.width - diameter) / 2f,
                y = (size.height - diameter) / 2f
            )
            var startAngle = -90f

            stats.forEach { stat ->
                // 用 sweepAngle 表达占比：360 度乘以该分类的比例
                val sweep = stat.ratio * 360f
                drawArc(
                    color = Color(stat.category.color),
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(diameter, diameter),
                    style = Stroke(width = strokeWidth)
                )
                startAngle += sweep
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "共 ${stats.size} 个分类",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 分类排行榜：横向条形图 + 金额 + 占比。
 *
 * @param stats 分类统计数据，已按金额倒序
 * @param modifier 外部修饰符
 */
@Composable
fun CategoryRankList(stats: List<CategoryStat>, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        stats.forEach { stat -> CategoryRankRow(stat = stat) }
    }
}

/** 单个分类的横向条形行 */
@Composable
private fun CategoryRankRow(stat: CategoryStat) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stat.category.name,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "${AmountValidator.formatCents(stat.amountInCents)}  " +
                    "(${(stat.ratio * 100).toInt()}%)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        CategoryBar(stat = stat)
    }
}

/** 占比条：用 Canvas 绘制，避免依赖进度条组件的固定高度限制 */
@Composable
private fun CategoryBar(stat: CategoryStat) {
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val barColor = Color(stat.category.color)

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
    ) {
        val radius = androidx.compose.ui.geometry.CornerRadius(size.height / 2f)
        drawRoundRect(color = trackColor, cornerRadius = radius)
        drawRoundRect(
            color = barColor,
            size = Size(width = size.width * stat.ratio, height = size.height),
            cornerRadius = radius
        )
    }
}
