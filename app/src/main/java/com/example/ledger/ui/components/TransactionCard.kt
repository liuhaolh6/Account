package com.example.ledger.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ledger.domain.model.Transaction
import com.example.ledger.domain.model.TransactionType
import com.example.ledger.ui.theme.AmountTextStyle
import com.example.ledger.ui.theme.ExpenseRed
import com.example.ledger.ui.theme.IncomeGreen
import com.example.ledger.util.AmountValidator
import com.example.ledger.util.DateUtils

/**
 * 单条流水卡片，首页与列表页共用。
 *
 * @param transaction 流水数据
 * @param onClick 点击回调，默认空实现以便在不支持点击的场景复用
 * @param modifier 外部修饰符
 */
@Composable
fun TransactionCard(
    transaction: Transaction,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isIncome = transaction.type == TransactionType.INCOME
    val amountColor = if (isIncome) IncomeGreen else ExpenseRed
    val amountPrefix = if (isIncome) "+" else "-"

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
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CategoryIcon(
                iconName = transaction.category.icon,
                colorArgb = transaction.category.color
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = transaction.category.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = buildSubtitle(transaction),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = amountPrefix + AmountValidator.formatCents(transaction.amountInCents),
                style = AmountTextStyle,
                color = amountColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * 组装副标题：有备注时以备注为主，否则展示时间。
 *
 * @param transaction 流水数据
 * @return 适合单行展示的副标题文本
 */
private fun buildSubtitle(transaction: Transaction): String =
    if (transaction.note.isBlank()) {
        DateUtils.formatDateTime(transaction.occurredAt)
    } else {
        "${transaction.note} · ${DateUtils.formatMonthDay(transaction.occurredAt)}"
    }
