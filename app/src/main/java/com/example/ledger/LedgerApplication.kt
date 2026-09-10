package com.example.ledger

import android.app.Application
import com.example.ledger.data.local.LedgerDatabase
import com.example.ledger.data.repository.LedgerRepositoryImpl
import com.example.ledger.domain.repository.LedgerRepository

/**
 * 应用级依赖容器。
 *
 * 项目当前规模不需要引入 Hilt 等依赖注入框架，用 Application 持有单例即可，
 * 既满足"界面—仓储"解耦，又避免额外的编译期开销。
 */
class LedgerApplication : Application() {

    /** 记账仓库，供各 ViewModel 通过工厂方法获取 */
    val ledgerRepository: LedgerRepository by lazy {
        LedgerRepositoryImpl.create(LedgerDatabase.getInstance(this))
    }
}
