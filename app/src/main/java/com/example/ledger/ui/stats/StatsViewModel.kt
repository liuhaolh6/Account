package com.example.ledger.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ledger.domain.model.Summary
import com.example.ledger.domain.model.TransactionType
import com.example.ledger.domain.repository.LedgerRepository
import com.example.ledger.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 统计页界面状态。
 *
 * @property monthMillis 当前统计月份（该月 1 日 00:00 的时间戳）
 * @property monthLabel 月份展示文本
 * @property type 统计维度，支出或收入
 * @property summary 汇总结果
 * @property isLoading 是否正在统计
 * @property error 统计失败的中文提示
 */
data class StatsUiState(
    val monthMillis: Long = DateUtils.startOfMonth(),
    val monthLabel: String = DateUtils.formatMonth(DateUtils.startOfMonth()),
    val type: TransactionType = TransactionType.EXPENSE,
    val summary: Summary = Summary(),
    val isLoading: Boolean = true,
    val error: String? = null
)

/**
 * 统计页 ViewModel：按月份与收支维度生成分类占比。
 *
 * @param repository 记账仓库
 */
class StatsViewModel(private val repository: LedgerRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    /**
     * 切换统计月份。
     *
     * @param offset 相对当前月份的偏移，-1 为上月
     */
    fun shiftMonth(offset: Int) {
        val target = DateUtils.shiftMonth(_uiState.value.monthMillis, offset)
        _uiState.update {
            it.copy(monthMillis = target, monthLabel = DateUtils.formatMonth(target))
        }
        refresh()
    }

    /**
     * 切换收入/支出维度。
     *
     * @param type 目标维度
     */
    fun switchType(type: TransactionType) {
        if (type == _uiState.value.type) return
        _uiState.update { it.copy(type = type) }
        refresh()
    }

    /**
     * 重新统计当前月份的数据。
     */
    fun refresh() {
        val state = _uiState.value
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            val start = state.monthMillis
            val end = DateUtils.shiftMonth(start, 1)
            repository.summarize(start, end, state.type)
                .onSuccess { summary ->
                    _uiState.update { it.copy(summary = summary, isLoading = false) }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "统计数据加载失败：${throwable.message ?: "请稍后重试"}"
                        )
                    }
                }
        }
    }
}
