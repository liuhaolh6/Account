package com.example.ledger.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ledger.domain.model.Transaction
import com.example.ledger.domain.repository.LedgerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.IOException

/**
 * 流水列表界面状态。
 *
 * @property keyword 当前搜索关键字
 * @property transactions 匹配到的流水，按时间倒序
 * @property totalExpense 当前结果集的支出合计（分）
 * @property totalIncome 当前结果集的收入合计（分）
 * @property message 面向用户的中文提示（含错误与操作反馈）
 */
data class TransactionListUiState(
    val keyword: String = "",
    val transactions: List<Transaction> = emptyList(),
    val totalExpense: Long = 0L,
    val totalIncome: Long = 0L,
    val message: String? = null
)

/**
 * 流水列表 ViewModel：负责搜索与删除。
 *
 * @param repository 记账仓库
 */
class TransactionListViewModel(private val repository: LedgerRepository) : ViewModel() {

    private val keywordFlow = MutableStateFlow("")
    private val messageFlow = MutableStateFlow<String?>(null)

    val uiState: StateFlow<TransactionListUiState> = combine(
        keywordFlow.flatMapLatest { keyword -> repository.search(keyword) },
        keywordFlow,
        messageFlow
    ) { list, keyword, message ->
        TransactionListUiState(
            keyword = keyword,
            transactions = list,
            totalExpense = list.filter { it.signedAmountInCents < 0 }.sumOf { it.amountInCents },
            totalIncome = list.filter { it.signedAmountInCents > 0 }.sumOf { it.amountInCents },
            message = message
        )
    }.catch { throwable ->
        val text = if (throwable is IOException) {
            "读取本地账目失败，请稍后重试"
        } else {
            "加载流水失败：${throwable.message ?: "未知错误"}"
        }
        emit(TransactionListUiState(message = text))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = TransactionListUiState()
    )

    /**
     * 更新搜索关键字。
     *
     * @param keyword 用户输入的关键字
     */
    fun onKeywordChange(keyword: String) {
        keywordFlow.value = keyword.take(MAX_KEYWORD_LENGTH)
    }

    /**
     * 删除一条流水。
     *
     * @param id 流水主键
     */
    fun delete(id: Long) {
        viewModelScope.launch {
            repository.delete(id)
                .onSuccess { messageFlow.value = "已删除该记录" }
                .onFailure { throwable ->
                    messageFlow.value = "删除失败：${throwable.message ?: "请稍后重试"}"
                }
        }
    }

    /** 清除提示信息，避免重复弹出 */
    fun consumeMessage() {
        messageFlow.value = null
    }

    private companion object {
        const val MAX_KEYWORD_LENGTH = 20
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
