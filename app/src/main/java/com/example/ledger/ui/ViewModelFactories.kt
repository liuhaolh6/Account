package com.example.ledger.ui

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.ledger.LedgerApplication
import com.example.ledger.domain.repository.LedgerRepository
import com.example.ledger.ui.add.AddTransactionViewModel
import com.example.ledger.ui.budget.BudgetViewModel
import com.example.ledger.ui.detail.DetailViewModel
import com.example.ledger.ui.home.HomeViewModel
import com.example.ledger.ui.list.TransactionListViewModel
import com.example.ledger.ui.stats.StatsViewModel

/**
 * 统一的 ViewModel 工厂。
 *
 * 在没有依赖注入框架的前提下，集中在此把仓库注入 ViewModel，
 * 避免各页面各自 new 仓库导致数据库实例与测试替换困难。
 */
object ViewModelFactories {

    /** 首页 ViewModel */
    val Home = viewModelFactory {
        initializer { HomeViewModel(ledgerRepository()) }
    }

    /** 流水列表 ViewModel */
    val TransactionList = viewModelFactory {
        initializer { TransactionListViewModel(ledgerRepository()) }
    }

    /** 记账表单 ViewModel */
    val AddTransaction = viewModelFactory {
        initializer { AddTransactionViewModel(ledgerRepository()) }
    }

    /** 统计 ViewModel */
    val Stats = viewModelFactory {
        initializer { StatsViewModel(ledgerRepository()) }
    }

    /** 预算 ViewModel：AndroidViewModel 由工厂自动注入 Application，此处只需注入仓库 */
    val Budget = viewModelFactory {
        initializer { BudgetViewModel(application(), ledgerRepository()) }
    }

    /** 详情 ViewModel */
    val Detail = viewModelFactory {
        initializer { DetailViewModel(ledgerRepository()) }
    }

    /** 从 Application 中取出仓库单例 */
    private fun CreationExtras.ledgerRepository(): LedgerRepository =
        (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as LedgerApplication)
            .ledgerRepository

    /** 取出 Application 实例，供 AndroidViewModel 构造使用 */
    private fun CreationExtras.application(): Application =
        requireNotNull(this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) {
            "获取 Application 失败"
        }
}
