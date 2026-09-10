package com.example.ledger.ui.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ledger.domain.model.TransactionType
import com.example.ledger.ui.components.SummaryCard

/**
 * 统计页：按月查看收支汇总与分类占比。
 *
 * @param viewModel 统计 ViewModel
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(viewModel: StatsViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(topBar = { TopAppBar(title = { Text(text = "统计") }) }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MonthSelector(
                monthLabel = state.monthLabel,
                onPrevious = { viewModel.shiftMonth(-1) },
                onNext = { viewModel.shiftMonth(1) }
            )

            TypeTabs(current = state.type, onSwitch = viewModel::switchType)

            SummaryCard(
                totalIncome = state.summary.totalIncomeInCents,
                totalExpense = state.summary.totalExpenseInCents,
                title = "${state.monthLabel}概览"
            )

            when {
                state.isLoading -> LoadingBlock()
                state.error != null -> ErrorBlock(message = state.error ?: "未知错误")
                state.summary.categoryStats.isEmpty() -> EmptyBlock(type = state.type)
                else -> {
                    CategoryDonutChart(stats = state.summary.categoryStats)
                    Text(
                        text = "${state.type.displayName}分类排行",
                        style = MaterialTheme.typography.titleMedium
                    )
                    CategoryRankList(stats = state.summary.categoryStats)
                }
            }

            androidx.compose.foundation.layout.Spacer(
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }
    }
}

/** 月份切换栏 */
@Composable
private fun MonthSelector(
    monthLabel: String,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevious) {
            Icon(imageVector = Icons.Filled.ChevronLeft, contentDescription = "上个月")
        }
        Text(text = monthLabel, style = MaterialTheme.typography.titleLarge)
        IconButton(onClick = onNext) {
            Icon(imageVector = Icons.Filled.ChevronRight, contentDescription = "下个月")
        }
    }
}

/** 收入/支出维度切换 */
@Composable
private fun TypeTabs(current: TransactionType, onSwitch: (TransactionType) -> Unit) {
    val tabs = listOf(TransactionType.EXPENSE, TransactionType.INCOME)
    TabRow(selectedTabIndex = tabs.indexOf(current)) {
        tabs.forEach { type ->
            Tab(
                selected = type == current,
                onClick = { onSwitch(type) },
                text = { Text(text = "${type.displayName}统计") }
            )
        }
    }
}

/** 统计进行中的占位 */
@Composable
private fun LoadingBlock() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

/**
 * 统计失败提示。
 *
 * @param message 中文错误提示
 */
@Composable
private fun ErrorBlock(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/**
 * 当月无数据提示。
 *
 * @param type 当前统计维度
 */
@Composable
private fun EmptyBlock(type: TransactionType) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "本月还没有${type.displayName}记录",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
