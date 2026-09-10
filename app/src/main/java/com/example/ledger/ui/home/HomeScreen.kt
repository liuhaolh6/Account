package com.example.ledger.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ledger.domain.model.Transaction
import com.example.ledger.ui.components.SummaryCard
import com.example.ledger.ui.components.TransactionCard
import com.example.ledger.ui.theme.AmountTextStyle
import com.example.ledger.ui.theme.ExpenseRed
import com.example.ledger.util.AmountValidator

/**
 * 首页：展示本月概览、今日支出与最近记录。
 *
 * @param viewModel 首页 ViewModel
 * @param onAddClick 点击记账按钮的回调
 * @param onItemClick 点击某条流水的回调
 * @param onViewAllClick 查看全部流水的回调
 */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onAddClick: () -> Unit,
    onItemClick: (Long) -> Unit,
    onViewAllClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    HomeContent(
        state = state,
        onAddClick = onAddClick,
        onItemClick = onItemClick,
        onViewAllClick = onViewAllClick
    )
}

/**
 * 无状态内容区，便于预览与 UI 测试。
 *
 * @param state 界面状态
 * @param onAddClick 记账按钮回调
 * @param onItemClick 条目点击回调
 * @param onViewAllClick 查看全部回调
 */
@Composable
private fun HomeContent(
    state: HomeUiState,
    onAddClick: () -> Unit,
    onItemClick: (Long) -> Unit,
    onViewAllClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            top = 16.dp,
            bottom = 96.dp
        )
    ) {
        item {
            Text(
                text = state.currentMonthLabel,
                style = MaterialTheme.typography.headlineMedium
            )
        }

        item {
            SummaryCard(
                totalIncome = state.monthIncome,
                totalExpense = state.monthExpense,
                title = "本月概览"
            )
        }

        item {
            TodayExpenseRow(amount = state.todayExpense, onAddClick = onAddClick)
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "最近记录",
                    style = MaterialTheme.typography.titleLarge
                )
                TextButton(onClick = onViewAllClick) {
                    Text(text = "查看全部")
                }
            }
        }

        if (state.recent.isEmpty()) {
            item {
                EmptyRecentHint()
            }
        } else {
            items(items = state.recent, key = { it.id }) { transaction: Transaction ->
                TransactionCard(
                    transaction = transaction,
                    onClick = { onItemClick(transaction.id) }
                )
            }
        }
    }
}

/**
 * 今日支出与记账入口。
 *
 * @param amount 今日支出（分）
 * @param onAddClick 记账回调
 */
@Composable
private fun TodayExpenseRow(amount: Long, onAddClick: () -> Unit) {
    androidx.compose.material3.Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onAddClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "今日支出",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "-" + AmountValidator.formatCents(amount),
                    style = AmountTextStyle,
                    color = ExpenseRed
                )
            }
            Text(
                text = "＋ 记一笔",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/** 首页无数据时的引导提示 */
@Composable
private fun EmptyRecentHint() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "本月还没有记账",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "点击上方卡片开始记录第一笔收支",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
