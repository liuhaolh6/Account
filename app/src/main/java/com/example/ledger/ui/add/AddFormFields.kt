package com.example.ledger.ui.add

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp
import com.example.ledger.domain.model.TransactionType
import com.example.ledger.ui.theme.ExpenseRed

/**
 * 收支类型切换标签。
 *
 * @param current 当前选中的类型
 * @param onTypeChange 切换回调
 */
@Composable
internal fun TypeTabs(current: TransactionType, onTypeChange: (TransactionType) -> Unit) {
    val tabs = listOf(TransactionType.EXPENSE, TransactionType.INCOME)
    TabRow(selectedTabIndex = tabs.indexOf(current)) {
        tabs.forEach { type ->
            Tab(
                selected = type == current,
                onClick = { onTypeChange(type) },
                text = { Text(text = type.displayName) }
            )
        }
    }
}

/**
 * 金额输入框，收入用主色、支出用红色，强化金额方向语义。
 *
 * @param value 输入文本
 * @param error 校验错误提示，非空时输入框标红
 * @param isIncome 是否为收入，决定前缀符号与颜色
 * @param onValueChange 输入变化回调
 */
@Composable
internal fun AmountField(
    value: String,
    error: String?,
    isIncome: Boolean,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(text = "金额（元）") },
        placeholder = { Text(text = "0.00") },
        prefix = {
            Text(
                text = if (isIncome) "+" else "-",
                color = if (isIncome) MaterialTheme.colorScheme.primary else ExpenseRed
            )
        },
        singleLine = true,
        isError = error != null,
        supportingText = error?.let { message -> { Text(text = message) } },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        textStyle = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold),
        modifier = Modifier.fillMaxWidth()
    )
}
