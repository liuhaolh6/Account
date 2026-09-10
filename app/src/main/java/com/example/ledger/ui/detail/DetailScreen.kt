package com.example.ledger.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ledger.domain.model.TransactionType

/**
 * 流水详情页：展示完整信息并支持删除。
 *
 * @param transactionId 目标流水 id
 * @param viewModel 详情 ViewModel
 * @param onBack 返回上一页
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    transactionId: Long,
    viewModel: DetailViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(transactionId) { viewModel.load(transactionId) }

    LaunchedEffect(state.deleted) { if (state.deleted) onBack() }

    LaunchedEffect(state.message) {
        state.message?.let { text ->
            snackbarHostState.showSnackbar(text)
            viewModel.consumeMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "记录详情") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(imageVector = Icons.Filled.Delete, contentDescription = "删除")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            state.detail?.let { transaction ->
                DetailContent(transaction = transaction)
            } ?: Text(
                text = "记录不存在或已被删除",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    if (showDeleteDialog) {
        DeleteConfirmDialog(
            onConfirm = {
                showDeleteDialog = false
                viewModel.delete()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
}

/**
 * 详情内容区。
 *
 * @param transaction 流水数据
 */
@Composable
private fun DetailContent(transaction: com.example.ledger.domain.model.Transaction) {
    val isIncome = transaction.type == TransactionType.INCOME
    val amountColor = if (isIncome) {
        com.example.ledger.ui.theme.IncomeGreen
    } else {
        com.example.ledger.ui.theme.ExpenseRed
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            com.example.ledger.ui.components.CategoryIcon(
                iconName = transaction.category.icon,
                colorArgb = transaction.category.color
            )
            Text(
                text = transaction.category.name,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = (if (isIncome) "+" else "-") +
                    com.example.ledger.util.AmountValidator.formatCents(transaction.amountInCents),
                style = com.example.ledger.ui.theme.AmountTextStyle,
                color = amountColor
            )
            DetailRow(label = "类型", value = transaction.type.displayName)
            DetailRow(
                label = "日期",
                value = com.example.ledger.util.DateUtils.formatDateTime(transaction.occurredAt)
            )
            DetailRow(label = "备注", value = transaction.note.ifBlank { "无" })
        }
    }
}
