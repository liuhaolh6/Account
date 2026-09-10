package com.example.ledger.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** 全局唯一的 DataStore 实例，挂在 Context 扩展属性上避免重复创建 */
private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "ledger_settings"
)

/**
 * 轻量偏好存储：保存月度预算等少量配置。
 * 这类数据没有查询需求，用 DataStore 比 Room 更合适。
 *
 * @param context 应用上下文
 */
class SettingsDataStore(private val context: Context) {

    /**
     * 读取月度预算。
     *
     * @return 预算金额（分）的流，未设置时默认 0（表示未配置预算）
     */
    val monthlyBudgetCents: Flow<Long> = context.settingsDataStore.data
        .map { preferences -> preferences[KEY_MONTHLY_BUDGET] ?: DEFAULT_BUDGET_CENTS }

    /**
     * 保存月度预算。
     *
     * @param cents 预算金额（分）
     */
    suspend fun saveMonthlyBudget(cents: Long) {
        context.settingsDataStore.edit { preferences ->
            preferences[KEY_MONTHLY_BUDGET] = cents
        }
    }

    private companion object {
        val KEY_MONTHLY_BUDGET = longPreferencesKey("monthly_budget_cents")
        const val DEFAULT_BUDGET_CENTS = 0L
    }
}
