package com.example.ledger.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ledger.ui.theme.AmountTextStyle
import com.example.ledger.ui.theme.ExpenseRed
import com.example.ledger.ui.theme.IncomeGreen
import com.example.ledger.util.AmountValidator

/**
 * 收支概览卡片：展示收入、支出、结余三项核心数字。
 *
 * @param totalIncome 收入合计（分）
 * @param totalExpense 支出合计（分）
 * @param title 卡片标题，如"本月概览"
 * @param modifier 外部修饰符
 */
@Composable
fun SummaryCard(
    totalIncome: Long,
    totalExpense: Long,
    title: String,
    modifier: Modifier = Modifier
) {
    val balance = totalIncome - totalExpense

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AmountColumn(
                    label = "收入",
                    text = "+" + AmountValidator.formatCents(totalIncome),
                    color = IncomeGreen
                )
                AmountColumn(
                    label = "支出",
                    text = "-" + AmountValidator.formatCents(totalExpense),
                    color = ExpenseRed
                )
                AmountColumn(
                    label = "结余",
                    text = AmountValidator.formatCents(balance, withSign = balance != 0L),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

/**
 * 概览卡中的单列数字。
 *
 * @param label 指标名称
 * @param text 已格式化的金额文本
 * @param color 数字颜色
 */
@Composable
private fun AmountColumn(label: String, text: String, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = text,
            style = AmountTextStyle,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}
