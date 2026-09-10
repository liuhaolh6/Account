package com.example.ledger.util

/**
 * 界面统一的加载状态封装。
 * 用密封接口而非布尔标志组合，可从类型上杜绝"既加载中又报错"这类非法状态。
 */
sealed interface UiState<out T> {

    /** 加载中 */
    data object Loading : UiState<Nothing>

    /**
     * 加载成功。
     *
     * @property data 业务数据
     */
    data class Success<T>(val data: T) : UiState<T>

    /**
     * 加载失败。
     *
     * @property message 面向用户的中文提示，可直接展示
     */
    data class Error(val message: String) : UiState<Nothing>

    /** 空闲状态，尚未发起请求或输入为空 */
    data object Idle : UiState<Nothing>
}
