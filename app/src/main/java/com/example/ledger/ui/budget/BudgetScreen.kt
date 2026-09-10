package com.example.ledger.ui.budget

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ledger.ui.theme.BudgetNormal
import com.example.ledger.ui.theme.BudgetOver
import com.example.ledger.ui.theme.BudgetWarning
import com.example.ledger.util.AmountValidator

/**
 * 预算页：设置月度预算并查看使用进度。
 *
 * @param viewModel 预算 ViewModel
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(viewModel: BudgetViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.message) {
        state.message?.let { text ->
            snackbarHostState.showSnackbar(text)
            viewModel.consumeMessage()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(text = "月度预算") }) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = state.monthLabel, style = MaterialTheme.typography.titleLarge)

            BudgetProgressCard(state = state)

            OutlinedTextField(
                value = state.budgetInput,
                onValueChange = viewModel::onBudgetChange,
                label = { Text(text = "月度预算（元）") },
                placeholder = { Text(text = "0.00") },
                singleLine = true,
                isError = state.inputError != null,
                supportingText = state.inputError?.let { error -> { Text(text = error) } },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = viewModel::saveBudget,
                enabled = !state.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(text = if (state.isSaving) "保存中…" else "保存预算")
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * 预算进度卡片：展示已用金额、剩余金额与进度条。
 *
 * @param state 预算界面状态
 */
@Composable
private fun BudgetProgressCard(state: BudgetUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (state.budgetCents <= 0L) {
                Text(
                    text = "还没有设置预算，填写下方金额即可开启超支提醒",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                return@Column
            }

            Text(
                text = "本月已支出 ${AmountValidator.formatCents(state.spentCents)} / " +
                    "预算 ${AmountValidator.formatCents(state.budgetCents)}",
                style = MaterialTheme.typography.bodyMedium
            )

            BudgetBar(ratio = state.usedRatio, isOver = state.isOverBudget)

            Text(
                text = if (state.isOverBudget) {
                    "已超出预算 ${AmountValidator.formatCents(-state.remainingCents)}"
                } else {
                    "剩余可用 ${AmountValidator.formatCents(state.remainingCents)}"
                },
                style = MaterialTheme.typography.bodySmall,
                color = if (state.isOverBudget) BudgetOver else MaterialTheme.colorScheme.onSurfaceVariant
            )

            // 明确口径：预算只针对本月，避免用户拿它和流水页的"全部记录合计"对比
            Text(
                text = "统计范围：仅本月（不含历史月份）",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 预算进度条。
 *
 * @param ratio 已使用比例，0f~1f
 * @param isOver 是否超支
 */
@Composable
private fun BudgetBar(ratio: Float, isOver: Boolean) {
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val barColor = when {
        isOver -> BudgetOver
        ratio >= WARNING_THRESHOLD -> BudgetWarning
        else -> BudgetNormal
    }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(14.dp)
    ) {
        val radius = CornerRadius(size.height / 2f)
        drawRoundRect(color = trackColor, cornerRadius = radius)
        drawRoundRect(
            color = barColor,
            size = Size(width = size.width * ratio, height = size.height),
            topLeft = Offset.Zero,
            cornerRadius = radius
        )
    }
}

/** 使用比例达到该值时进度条转为警示色 */
private const val WARNING_THRESHOLD = 0.8f
