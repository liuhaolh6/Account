package com.example.ledger.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ledger.domain.model.Transaction
import com.example.ledger.domain.repository.LedgerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 详情页界面状态。
 *
 * @property detail 流水详情，加载失败或未找到时为 null
 * @property message 操作提示（含错误），面向用户的中文文案
 * @property deleted 是否已删除，界面据此执行返回
 */
data class DetailUiState(
    val detail: Transaction? = null,
    val message: String? = null,
    val deleted: Boolean = false
)

/**
 * 流水详情 ViewModel：按 id 读取单条记录并支持删除。
 *
 * @param repository 记账仓库
 */
class DetailViewModel(private val repository: LedgerRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    /**
     * 加载指定流水。
     * 由于仓储接口未提供按 id 的单条查询，这里从全量流中筛选，
     * 数据量在个人记账场景下很小，代价可以接受。
     *
     * @param id 流水主键
     */
    fun load(id: Long) {
        viewModelScope.launch {
            runCatching {
                repository.observeAll().first().firstOrNull { it.id == id }
            }.onSuccess { transaction ->
                _uiState.update { state ->
                    if (transaction == null) {
                        state.copy(message = "未找到该记录，可能已被删除")
                    } else {
                        state.copy(detail = transaction)
                    }
                }
            }.onFailure { throwable ->
                _uiState.update { state ->
                    state.copy(message = "加载失败：${throwable.message ?: "未知错误"}")
                }
            }
        }
    }

    /**
     * 删除当前记录。
     */
    fun delete() {
        val id = _uiState.value.detail?.id ?: return
        viewModelScope.launch {
            repository.delete(id)
                .onSuccess {
                    _uiState.update { state -> state.copy(deleted = true) }
                }
                .onFailure { throwable ->
                    _uiState.update { state ->
                        state.copy(message = "删除失败：${throwable.message ?: "请稍后重试"}")
                    }
                }
        }
    }

    /** 清除提示信息 */
    fun consumeMessage() {
        _uiState.update { it.copy(message = null) }
    }
}
