package com.example.ledger.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ledger.ui.ViewModelFactories
import com.example.ledger.ui.add.AddTransactionScreen
import com.example.ledger.ui.add.AddTransactionViewModel
import com.example.ledger.ui.budget.BudgetScreen
import com.example.ledger.ui.budget.BudgetViewModel
import com.example.ledger.ui.detail.DetailScreen
import com.example.ledger.ui.detail.DetailViewModel
import com.example.ledger.ui.home.HomeScreen
import com.example.ledger.ui.home.HomeViewModel
import com.example.ledger.ui.list.TransactionListScreen
import com.example.ledger.ui.list.TransactionListViewModel
import com.example.ledger.ui.stats.StatsScreen
import com.example.ledger.ui.stats.StatsViewModel

/**
 * 应用导航宿主。
 * 底部导航只在四个主页面显示，记账与详情页使用全屏，避免返回逻辑混乱。
 *
 * @param navController 导航控制器，默认由 Compose 管理
 */
@Composable
fun LedgerNavHost(navController: NavHostController = rememberNavController()) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = bottomTabs.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                LedgerBottomBar(
                    currentRoute = currentRoute,
                    onTabClick = { route ->
                        // 使用 singleTop 避免重复入栈导致返回键需要按多次
                        navController.navigate(route) {
                            popUpTo(Routes.HOME) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.HOME) {
                val viewModel: HomeViewModel = viewModel(factory = ViewModelFactories.Home)
                HomeScreen(
                    viewModel = viewModel,
                    onAddClick = { navController.navigate(Routes.ADD) },
                    onItemClick = { id -> navController.navigate(Routes.detail(id)) },
                    onViewAllClick = { navController.navigate(Routes.LIST) }
                )
            }

            composable(Routes.LIST) {
                val viewModel: TransactionListViewModel =
                    viewModel(factory = ViewModelFactories.TransactionList)
                TransactionListScreen(
                    viewModel = viewModel,
                    onItemClick = { id -> navController.navigate(Routes.detail(id)) }
                )
            }

            composable(Routes.STATS) {
                val viewModel: StatsViewModel = viewModel(factory = ViewModelFactories.Stats)
                StatsScreen(viewModel = viewModel)
            }

            composable(Routes.BUDGET) {
                val viewModel: BudgetViewModel = viewModel(factory = ViewModelFactories.Budget)
                BudgetScreen(viewModel = viewModel)
            }

            composable(Routes.ADD) {
                val viewModel: AddTransactionViewModel =
                    viewModel(factory = ViewModelFactories.AddTransaction)
                AddTransactionScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() }
                )
            }

            composable(
                route = Routes.DETAIL,
                arguments = listOf(
                    navArgument(Routes.ARG_TRANSACTION_ID) { type = NavType.LongType }
                )
            ) { entry ->
                val id = entry.arguments?.getLong(Routes.ARG_TRANSACTION_ID) ?: INVALID_ID
                val viewModel: DetailViewModel = viewModel(factory = ViewModelFactories.Detail)
                DetailScreen(
                    transactionId = id,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

/**
 * 底部导航栏。
 *
 * @param currentRoute 当前路由
 * @param onTabClick 点击标签的回调
 */
@Composable
private fun LedgerBottomBar(currentRoute: String?, onTabClick: (String) -> Unit) {
    NavigationBar {
        bottomTabs.forEach { tab ->
            NavigationBarItem(
                selected = currentRoute == tab.route,
                onClick = { onTabClick(tab.route) },
                icon = { Icon(imageVector = tab.icon, contentDescription = tab.label) },
                label = { Text(text = tab.label) }
            )
        }
    }
}
