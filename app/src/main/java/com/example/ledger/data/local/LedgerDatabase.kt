package com.example.ledger.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * 应用数据库。
 * version 从 1 开始，后续字段调整时必须提升版本并在 [MIGRATIONS] 中补充迁移脚本，
 * 否则用户升级后本地账单会被清空。
 */
@Database(
    entities = [TransactionEntity::class],
    version = 1,
    exportSchema = true
)
abstract class LedgerDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao

    companion object {

        private const val DB_NAME = "ledger.db"

        @Volatile
        private var instance: LedgerDatabase? = null

        /**
         * 获取数据库单例。
         * 采用双检锁保证多线程下只初始化一次，避免重复打开数据库连接。
         *
         * @param context 应用上下文，内部会取 applicationContext 防止内存泄漏
         * @return 数据库实例
         */
        fun getInstance(context: Context): LedgerDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    LedgerDatabase::class.java,
                    DB_NAME
                ).build().also { instance = it }
            }
    }
}
