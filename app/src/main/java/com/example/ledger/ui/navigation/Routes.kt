package com.example.ledger.ui.navigation

/**
 * 全部页面路由。
 * 集中定义避免字符串散落各处，改动路由时只需修改一处。
 */
object Routes {

    const val HOME = "home"
    const val LIST = "list"
    const val STATS = "stats"
    const val ADD = "add"
    const val BUDGET = "budget"

    /** 详情页需要流水 id 作为参数 */
    const val DETAIL = "detail/{transactionId}"

    /**
     * 构造详情页的真实路由。
     *
     * @param id 流水主键
     * @return 形如 "detail/12" 的路由字符串
     */
    fun detail(id: Long): String = "detail/$id"

    const val ARG_TRANSACTION_ID = "transactionId"
}
