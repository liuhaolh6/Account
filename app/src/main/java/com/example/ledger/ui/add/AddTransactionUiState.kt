package com.example.ledger.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ledger.domain.model.Categories
import com.example.ledger.domain.model.Category
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
 * 记账表单状态。
 *
 * @property type 当前选中的收支类型
 * @property amountInput 用户输入的金额原文（仅在提交时校验，避免边打字边报错影响体验）
 * @property selectedCategoryId 选中的分类 id
 * @property note 备注
 * @property occurredAt 发生时间
 * @property amountError 金额字段的错误提示
 * @property noteError 备注字段的错误提示
 * @property isSaving 是否正在保存，用于禁用按钮防止重复提交
 * @property saveError 保存失败提示
 * @property saveSuccess 保存成功标记，界面消费后导航返回
 */
data class AddTransactionUiState(
    val type: TransactionType = TransactionType.EXPENSE,
    val amountInput: String = "",
    val selectedCategoryId: String = Categories.EXPENSE.first().id,
    val note: String = "",
    val occurredAt: Long = System.currentTimeMillis(),
    val amountError: String? = null,
    val noteError: String? = null,
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val saveSuccess: Boolean = false
) {
    /** 当前类型下的可选分类 */
    val categories: List<Category>
        get() = Categories.of(type)

    /** 当前选中的分类，兜底为首个分类 */
    val selectedCategory: Category
        get() = Categories.of(type).firstOrNull { it.id == selectedCategoryId }
            ?: Categories.of(type).first()

    /** 表单是否可提交：金额非空且不在保存中 */
    val canSubmit: Boolean
        get() = amountInput.isNotBlank() && !isSaving
}
