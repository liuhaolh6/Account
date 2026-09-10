package com.example.ledger.ui.add

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ledger.domain.model.TransactionType
import com.example.ledger.util.DateUtils

/**
 * 记账表单页：支持收支切换、金额输入、分类选择、备注与日期设置。
 *
 * @param viewModel 表单 ViewModel
 * @param onBack 返回上一页
 * @param onSaved 保存成功后的回调
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    viewModel: AddTransactionViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showDatePicker by remember { mutableStateOf(false) }

    // 保存成功后回到上一页；用 LaunchedEffect 保证只在状态变化时触发一次
    LaunchedEffect(state.saveSuccess) {
        if (state.saveSuccess) {
            viewModel.consumeSuccess()
            onSaved()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "记一笔") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TypeTabs(current = state.type, onTypeChange = viewModel::onTypeChange)

            AmountField(
                value = state.amountInput,
                error = state.amountError,
                isIncome = state.type == TransactionType.INCOME,
                onValueChange = viewModel::onAmountChange
            )

            CategoryGrid(
                categories = state.categories,
                selectedId = state.selectedCategory.id,
                onSelect = viewModel::onCategorySelect
            )

            OutlinedTextField(
                value = state.note,
                onValueChange = viewModel::onNoteChange,
                label = { Text(text = "备注（选填，最多 50 字）") },
                singleLine = true,
                isError = state.noteError != null,
                supportingText = state.noteError?.let { error -> { Text(text = error) } },
                modifier = Modifier.fillMaxWidth()
            )

            DateRow(
                dateText = DateUtils.formatDate(state.occurredAt),
                onClick = { showDatePicker = true }
            )

            state.saveError?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Button(
                onClick = { viewModel.submit(onSuccess = {}) },
                enabled = state.canSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(text = "保存")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showDatePicker) {
        TransactionDatePicker(
            initialMillis = state.occurredAt,
            onDismiss = { showDatePicker = false },
            onConfirm = { millis ->
                viewModel.onDateChange(millis)
                showDatePicker = false
            }
        )
    }
}
