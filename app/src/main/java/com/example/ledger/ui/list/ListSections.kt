package com.example.ledger.ui.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.ledger.util.AmountValidator

/**
 * 搜索输入框。
 *
 * @param keyword 当前关键字
 * @param onKeywordChange 关键字变化回调
 * @param modifier 外部修饰符
 */
@Composable
internal fun SearchBar(
    keyword: String,
    onKeywordChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = keyword,
        onValueChange = onKeywordChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(text = "搜索备注或分类") },
        leadingIcon = {
            Icon(imageVector = Icons.Filled.Search, contentDescription = "搜索")
        },
        trailingIcon = {
            if (keyword.isNotEmpty()) {
                IconButton(onClick = { onKeywordChange("") }) {
                    Icon(imageVector = Icons.Filled.Clear, contentDescription = "清除")
                }
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
    )
}

/**
 * 结果统计条，让用户知道筛选后的条数与合计金额。
 *
 * 这里的合计是"当前列表全部记录"的合计（关键字为空时即历史全部月份），
 * 与预算页的"本月支出"口径不同，故文案中显式标注范围，避免用户误解。
 */
@Composable
internal fun ResultSummary(count: Int, totalIncome: Long, totalExpense: Long) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = "共 $count 条记录",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "收入 ${AmountValidator.formatCents(totalIncome)} / " +
                "支出 ${AmountValidator.formatCents(totalExpense)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "统计范围：以上结果集合计（非单月，如需本月支出请看预算页）",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** 无匹配结果时的引导文案，区分「尚无数据」与「搜索无结果」 */
@Composable
internal fun EmptyResult(keyword: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Text(
            text = if (keyword.isBlank()) {
                "还没有流水记录，回到首页点击记一笔开始记账"
            } else {
                "没有找到包含「$keyword」的记录"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
