package com.example.ledger.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ledger.domain.model.Categories
import com.example.ledger.domain.model.Transaction
import com.example.ledger.domain.model.TransactionType
import com.example.ledger.domain.repository.LedgerRepository
import com.example.ledger.util.AmountValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 记账表单 ViewModel：负责输入校验与数据落库。
 *
 * @param repository 记账仓库
 */
class AddTransactionViewModel(private val repository: LedgerRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(AddTransactionUiState())
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()

    /**
     * 切换收支类型。
     * 类型变化后原分类可能不属于新类型，因此重置为该类型的首个分类。
     *
     * @param type 新的收支类型
     */
    fun onTypeChange(type: TransactionType) {
        if (type == _uiState.value.type) return
        _uiState.update { state ->
            state.copy(
                type = type,
                selectedCategoryId = Categories.of(type).first().id,
                amountError = null
            )
        }
    }

    /**
     * 更新金额输入。
     * 过滤非法字符并限制小数位，从源头减少校验失败的概率。
     *
     * @param input 用户输入原文
     */
    fun onAmountChange(input: String) {
        _uiState.update { it.copy(amountInput = sanitizeAmount(input), amountError = null) }
    }

    /**
     * 选择分类。
     *
     * @param categoryId 分类 id
     */
    fun onCategorySelect(categoryId: String) {
        _uiState.update { it.copy(selectedCategoryId = categoryId) }
    }

    /**
     * 更新备注。
     *
     * @param note 备注原文
     */
    fun onNoteChange(note: String) {
        _uiState.update { it.copy(note = note.take(MAX_NOTE_LENGTH), noteError = null) }
    }

    /**
     * 更新发生时间。
     *
     * @param millis 选择的时间戳
     */
    fun onDateChange(millis: Long) {
        _uiState.update { it.copy(occurredAt = millis) }
    }

    /** 关闭保存失败提示 */
    fun dismissSaveError() {
        _uiState.update { it.copy(saveError = null) }
    }

    /**
     * 校验并保存。
     * 校验不通过时只更新错误字段，不发起数据库写入。
     *
     * @param onSuccess 保存成功后的回调，由界面执行导航返回
     */
    fun submit(onSuccess: () -> Unit) {
        val current = _uiState.value
        val cents = AmountValidator.parseToCents(current.amountInput)
        if (cents == null) {
            _uiState.update { it.copy(amountError = amountErrorMessage(current.amountInput)) }
            return
        }
        val noteError = AmountValidator.validateNote(current.note)
        if (noteError != null) {
            _uiState.update { it.copy(noteError = noteError) }
            return
        }
        if (current.isSaving) return

        _uiState.update { it.copy(isSaving = true, saveError = null) }

        viewModelScope.launch {
            val transaction = Transaction(
                amountInCents = cents,
                type = current.type,
                categoryId = current.selectedCategoryId,
                note = current.note.trim(),
                occurredAt = current.occurredAt
            )
            repository.save(transaction)
                .onSuccess {
                    _uiState.update { state -> state.copy(isSaving = false, saveSuccess = true) }
                    onSuccess()
                }
                .onFailure { throwable ->
                    _uiState.update { state ->
                        state.copy(
                            isSaving = false,
                            saveError = "保存失败：${throwable.message ?: "数据库写入异常，请重试"}"
                        )
                    }
                }
        }
    }

    /** 关闭成功标记，避免返回后再次触发导航 */
    fun consumeSuccess() {
        _uiState.update { it.copy(saveSuccess = false) }
    }

    /**
     * 清洗金额输入：只保留数字与一个小数点，小数位截断到两位。
     *
     * @param input 用户输入原文
     * @return 清洗后的文本
     */
    private fun sanitizeAmount(input: String): String {
        val digitsOnly = input.filter { it.isDigit() || it == '.' }.take(MAX_AMOUNT_LENGTH)
        val firstDot = digitsOnly.indexOf('.')
        if (firstDot < 0) return digitsOnly
        val head = digitsOnly.substring(0, firstDot + 1)
        val tail = digitsOnly.substring(firstDot + 1).replace(".", "").take(2)
        return head + tail
    }

    /**
     * 根据输入内容给出针对性的中文提示，比统一的"输入错误"更有指导意义。
     *
     * @param raw 用户输入原文
     * @return 错误提示文本
     */
    private fun amountErrorMessage(raw: String): String = when {
        raw.isBlank() -> "请输入金额"
        raw == "." || raw.startsWith(".") -> "金额格式不正确，请输入如 12.50"
        raw.endsWith(".") -> "金额格式不正确，请补全小数部分"
        else -> "金额需为 0.01 ~ 9999999.99 之间的数字"
    }

    private companion object {
        const val MAX_AMOUNT_LENGTH = 9
        const val MAX_NOTE_LENGTH = 50
    }
}
