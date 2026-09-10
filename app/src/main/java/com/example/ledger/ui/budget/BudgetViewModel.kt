package com.example.ledger.ui.budget

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ledger.data.local.SettingsDataStore
import com.example.ledger.domain.model.TransactionType
import com.example.ledger.domain.repository.LedgerRepository
import com.example.ledger.util.AmountValidator
import com.example.ledger.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 预算页界面状态。
 *
 * @property budgetInput 预算输入框的原始文本
 * @property budgetCents 已保存的预算（分），0 表示未设置
 * @property spentCents 本月已支出（分）
 * @property monthLabel 当前月份文本
 * @property inputError 输入校验错误提示
 * @property message 操作反馈提示
 * @property isSaving 是否正在保存
 */
data class BudgetUiState(
    val budgetInput: String = "",
    val budgetCents: Long = 0L,
    val spentCents: Long = 0L,
    val monthLabel: String = DateUtils.formatMonth(),
    val inputError: String? = null,
    val message: String? = null,
    val isSaving: Boolean = false
) {
    /** 已使用比例，未设置预算时为 0 */
    val usedRatio: Float
        get() = if (budgetCents > 0L) {
            (spentCents.toDouble() / budgetCents).toFloat().coerceIn(0f, 1f)
        } else {
            0f
        }

    /** 剩余可用金额（分），可能为负，表示超支 */
    val remainingCents: Long
        get() = budgetCents - spentCents

    /** 是否超支 */
    val isOverBudget: Boolean
        get() = budgetCents > 0L && spentCents > budgetCents
}

/**
 * 预算 ViewModel。
 *
 * 继承 [AndroidViewModel] 是为了拿到 Context 去创建 DataStore，
 * 这是本项目唯一需要 Context 的 ViewModel。
 *
 * @param application 应用实例
 * @param repository 记账仓库，用于统计本月支出
 */
class BudgetViewModel(
    application: Application,
    private val repository: LedgerRepository
) : AndroidViewModel(application) {

    private val settings = SettingsDataStore(application)

    private val _uiState = MutableStateFlow(BudgetUiState())
    val uiState: StateFlow<BudgetUiState> = _uiState.asStateFlow()

    init {
        observeBudget()
        observeSpent()
    }

    /**
     * 更新预算输入。
     *
     * @param input 用户输入原文
     */
    fun onBudgetChange(input: String) {
        val filtered = input.filter { it.isDigit() || it == '.' }.take(MAX_INPUT_LENGTH)
        _uiState.update { it.copy(budgetInput = filtered, inputError = null) }
    }

    /** 清除提示信息 */
    fun consumeMessage() {
        _uiState.update { it.copy(message = null) }
    }

    /**
     * 校验并保存预算。
     */
    fun saveBudget() {
        val state = _uiState.value
        if (state.isSaving) return

        val cents = AmountValidator.parseToCents(state.budgetInput)
        if (cents == null) {
            _uiState.update {
                it.copy(inputError = "请输入 0.01 ~ 9999999.99 之间的预算金额")
            }
            return
        }

        _uiState.update { it.copy(isSaving = true, inputError = null) }

        viewModelScope.launch {
            runCatching { settings.saveMonthlyBudget(cents) }
                .onSuccess {
                    _uiState.update { current ->
                        current.copy(isSaving = false, message = "预算已保存")
                    }
                }
                .onFailure { throwable ->
                    _uiState.update { current ->
                        current.copy(
                            isSaving = false,
                            message = "保存失败：${throwable.message ?: "本地存储不可用，请重试"}"
                        )
                    }
                }
        }
    }

    /** 订阅预算变化，同时把已保存的金额回填到输入框（仅首次） */
    private fun observeBudget() {
        viewModelScope.launch {
            settings.monthlyBudgetCents.collect { cents ->
                _uiState.update { state ->
                    state.copy(
                        budgetCents = cents,
                        budgetInput = if (cents > 0L) {
                            AmountValidator.formatCents(cents)
                        } else {
                            state.budgetInput
                        }
                    )
                }
            }
        }
    }

    /**
     * 持续订阅本月支出。
     *
     * 早期版本用一次性查询，导致记账后回到预算页仍显示旧金额；
     * 改为订阅 Flow 后，任何新增/删除/修改流水都会自动重新计算。
     */
    private fun observeSpent() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            repository.observeSummarySource(
                startMillis = DateUtils.startOfMonth(now),
                endMillis = DateUtils.endOfMonth(now)
            ).catch { throwable ->
                _uiState.update {
                    it.copy(message = "本月支出读取失败：${throwable.message ?: "请稍后重试"}")
                }
            }.collect { list ->
                // 只统计支出类型，金额字段恒为正，无需再取绝对值
                val spent = list
                    .filter { it.type == TransactionType.EXPENSE }
                    .sumOf { it.amountInCents }
                _uiState.update { it.copy(spentCents = spent) }
            }
        }
    }

    private companion object {
        const val MAX_INPUT_LENGTH = 9
    }
}
