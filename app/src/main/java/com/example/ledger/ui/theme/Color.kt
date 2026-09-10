package com.example.ledger.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * 应用配色。
 * 收入/支出使用固定语义色，保证在所有页面中"绿色=收入、红色=支出"的认知一致。
 */

// 浅色主题
val PrimaryLight = Color(0xFF1E88E5)
val OnPrimaryLight = Color(0xFFFFFFFF)
val PrimaryContainerLight = Color(0xFFD3E4FF)
val OnPrimaryContainerLight = Color(0xFF001C38)
val SecondaryLight = Color(0xFF565E71)
val BackgroundLight = Color(0xFFF7F9FC)
val SurfaceLight = Color(0xFFFFFFFF)
val OnSurfaceLight = Color(0xFF1A1C1E)
val SurfaceVariantLight = Color(0xFFE1E2EC)
val OnSurfaceVariantLight = Color(0xFF44474F)

// 深色主题
val PrimaryDark = Color(0xFF8FCDFF)
val OnPrimaryDark = Color(0xFF003256)
val PrimaryContainerDark = Color(0xFF00497B)
val OnPrimaryContainerDark = Color(0xFFD3E4FF)
val SecondaryDark = Color(0xFFBEC6DC)
val BackgroundDark = Color(0xFF121212)
val SurfaceDark = Color(0xFF1E1F22)
val OnSurfaceDark = Color(0xFFE3E2E6)
val SurfaceVariantDark = Color(0xFF44474F)
val OnSurfaceVariantDark = Color(0xFFC4C6D0)

/** 收入语义色（绿） */
val IncomeGreen = Color(0xFF2E7D32)

/** 支出语义色（红） */
val ExpenseRed = Color(0xFFD32F2F)

/** 预算进度条正常态 */
val BudgetNormal = Color(0xFF2E7D32)

/** 预算进度条接近上限 */
val BudgetWarning = Color(0xFFF9A825)

/** 预算进度条超支 */
val BudgetOver = Color(0xFFD32F2F)
