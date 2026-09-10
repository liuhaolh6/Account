package com.example.ledger.domain.repository

import com.example.ledger.domain.model.Summary
import com.example.ledger.domain.model.Transaction
import com.example.ledger.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow

/**
 * 记账数据的统一访问入口。
 *
 * 定义在 domain 层是为了让 ViewModel 只依赖抽象，便于换用缓存或远程实现，
 * 也方便在单元测试中用假实现替换数据库。
 */
interface LedgerRepository {

    /** 全部流水，按发生时间倒序，供界面持续观察 */
    fun observeAll(): Flow<List<Transaction>>

    /**
     * 按关键字查询流水，关键字匹配备注或分类名称。
     *
     * @param keyword 查询关键字，空白表示查询全部
     * @return 匹配的流水流
     */
    fun search(keyword: String): Flow<List<Transaction>>

    /**
     * 查询指定时间范围内的流水。
     *
     * @param startMillis 起始时间（含）
     * @param endMillis 结束时间（不含）
     */
    fun observeBetween(startMillis: Long, endMillis: Long): Flow<List<Transaction>>

    /**
     * 新增或更新一条流水。
     *
     * @param transaction 待保存的流水，id 为 0 时表示新增
     * @return 保存后的主键 id；失败时返回 [Result.failure]
     */
    suspend fun save(transaction: Transaction): Result<Long>

    /**
     * 删除一条流水。
     *
     * @param id 流水主键
     * @return 成功或失败结果
     */
    suspend fun delete(id: Long): Result<Unit>

    /**
     * 统计指定时间范围内的收支汇总。
     *
     * @param startMillis 起始时间（含）
     * @param endMillis 结束时间（不含）
     * @param type 需要细分的收支类型，用于生成分类占比
     * @return 汇总结果；失败时返回 [Result.failure]
     */
    suspend fun summarize(
        startMillis: Long,
        endMillis: Long,
        type: TransactionType
    ): Result<Summary>

    /**
     * 持续观察指定时间范围内的流水，用于页面自行做轻量汇总。
     *
     * 与 [summarize] 的区别：本方法返回 Flow，数据变化会自动推送，
     * 适合预算页这类需要"记账后立即刷新"的场景；
     * 而 [summarize] 是一次性快照，适合统计页手动切换月份后重查。
     *
     * @param startMillis 起始时间（含）
     * @param endMillis 结束时间（不含）
     * @return 该范围内流水的流
     */
    fun observeSummarySource(startMillis: Long, endMillis: Long): Flow<List<Transaction>>
}
