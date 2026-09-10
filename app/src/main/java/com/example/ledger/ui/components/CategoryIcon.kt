package com.example.ledger.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WorkOutline
import androidx.compose.material3.Icon

/**
 * 分类图标映射表。
 *
 * 分类数据里只存图标名称字符串，在这里映射为具体矢量图，
 * 使 data 层不依赖 Compose，也让图标替换只需改动本文件。
 */
private val iconMap: Map<String, ImageVector> = mapOf(
    "Restaurant" to Icons.Filled.Restaurant,
    "DirectionsBus" to Icons.Filled.DirectionsBus,
    "ShoppingBag" to Icons.Filled.ShoppingBag,
    "Home" to Icons.Filled.Home,
    "SportsEsports" to Icons.Filled.SportsEsports,
    "LocalHospital" to Icons.Filled.LocalHospital,
    "MenuBook" to Icons.Filled.MenuBook,
    "AccountBalanceWallet" to Icons.Filled.AccountBalanceWallet,
    "CardGiftcard" to Icons.Filled.CardGiftcard,
    "WorkOutline" to Icons.Filled.WorkOutline,
    "TrendingUp" to Icons.Filled.TrendingUp,
    "MoreHoriz" to Icons.Filled.MoreHoriz
)

/**
 * 按名称解析图标，未知名称回退到"其他"图标。
 *
 * @param name 图标名称
 * @return 对应的矢量图标
 */
fun resolveCategoryIcon(name: String): ImageVector =
    iconMap[name] ?: Icons.Filled.MoreHoriz

/**
 * 圆形分类图标。
 *
 * @param iconName 图标名称
 * @param colorArgb 图标底色（ARGB 值）
 * @param modifier 外部修饰符
 */
@Composable
fun CategoryIcon(
    iconName: String,
    colorArgb: Long,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .background(Color(colorArgb).copy(alpha = 0.15f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = resolveCategoryIcon(iconName),
            contentDescription = null,
            tint = Color(colorArgb),
            modifier = Modifier.size(22.dp)
        )
    }
}

/**
 * 空态占位视图，供列表无数据时复用。
 *
 * @param text 提示文案
 * @param modifier 外部修饰符
 */
@Composable
fun EmptyPlaceholder(text: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        androidx.compose.material3.Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
