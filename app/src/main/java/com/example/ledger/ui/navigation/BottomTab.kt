package com.example.ledger.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Savings
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * 底部导航项定义。
 *
 * @property route 目标路由
 * @property label 展示名称
 * @property icon 图标
 */
data class BottomTab(
    val route: String,
    val label: String,
    val icon: ImageVector
)

/** 四个主页面，顺序即底部导航的展示顺序 */
val bottomTabs: List<BottomTab> = listOf(
    BottomTab(Routes.HOME, "首页", Icons.Filled.Home),
    BottomTab(Routes.LIST, "流水", Icons.Filled.List),
    BottomTab(Routes.STATS, "统计", Icons.Filled.BarChart),
    BottomTab(Routes.BUDGET, "预算", Icons.Filled.Savings)
)

/** 参数缺失时的兜底值，详情页会提示"记录不存在"而非崩溃 */
const val INVALID_ID = -1L
