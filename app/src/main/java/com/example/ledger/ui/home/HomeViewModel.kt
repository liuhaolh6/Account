package com.example.ledger.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ledger.domain.model.Transaction
import com.example.ledger.domain.repository.LedgerRepository
import com.example.ledger.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.io.IOException

/**
 * 首页界面状态。
 *
 * @property monthIncome 本月收入（分）
 * @property monthExpense 本月支出（分）
 * @property todayExpense 今日支出（分）
 * @property recent 最近的流水记录，最多 5 条
 * @property currentMonthLabel 当前月份文本，如"2026年09月"
 */
data class HomeUiState(
    val monthIncome: Long = 0L,
    val monthExpense: Long = 0L,
    val todayExpense: Long = 0L,
    val recent: List<Transaction> = emptyList(),
    val currentMonthLabel: String = "",
    val loadError: String? = null
)

/**
 * 首页 ViewModel：负责本月与今日的收支概览以及最近记录。
 *
 * @param repository 记账仓库
 */
class HomeViewModel(private val repository: LedgerRepository) : ViewModel() {

    private val now: Long = System.currentTimeMillis()

    private val monthRange = DateUtils.startOfMonth(now) to DateUtils.endOfMonth(now)
    private val todayRange = DateUtils.startOfDay(now) to DateUtils.endOfDay(now)

    /** 手动触发的错误提示，读取后由界面调用 [consumeError] 清除 */
    private val errorFlow = MutableStateFlow<String?>(null)

    val uiState: StateFlow<HomeUiState> = combine(
        repository.observeBetween(monthRange.first, monthRange.second),
        repository.observeBetween(todayRange.first, todayRange.second),
        errorFlow
    ) { monthList, todayList, error ->
        HomeUiState(
            monthIncome = monthList.filter { it.signedAmountInCents > 0 }.sumOf { it.amountInCents },
            monthExpense = monthList.filter { it.signedAmountInCents < 0 }.sumOf { it.amountInCents },
            todayExpense = todayList.filter { it.signedAmountInCents < 0 }.sumOf { it.amountInCents },
            recent = monthList.take(RECENT_LIMIT),
            currentMonthLabel = DateUtils.formatMonth(now),
            loadError = error
        )
    }.catch { throwable ->
        // 数据源异常时给用户中文提示，而不是让界面白屏
        val message = if (throwable is IOException) {
            "读取本地账目失败，请检查存储空间后重试"
        } else {
            "加载首页数据失败：${throwable.message ?: "未知错误"}"
        }
        emit(HomeUiState(currentMonthLabel = DateUtils.formatMonth(now), loadError = message))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = HomeUiState(currentMonthLabel = DateUtils.formatMonth(now))
    )

    /** 清除错误提示，避免旋转屏幕后重复弹出 */
    fun consumeError() {
        errorFlow.value = null
    }

    private companion object {
        const val RECENT_LIMIT = 5
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
