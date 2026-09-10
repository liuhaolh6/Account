package com.example.ledger.util

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * 金额校验与换算工具。
 *
 * 内部一律以"分"（Long）为存储单位，使用 BigDecimal 解析用户输入，
 * 避免 Double 的二进制精度误差导致 0.1 + 0.2 这类问题出现在账目里。
 */
object AmountValidator {

    /** 单笔金额上限（分）：一千万元，防止误输入超长数字撑爆统计 */
    private const val MAX_AMOUNT_IN_CENTS = 1_000_000_000L

    /**
     * 校验并解析用户输入的金额字符串。
     *
     * @param raw 用户输入的原始文本，允许包含首尾空格
     * @return 解析成功返回金额（分），失败返回 null
     */
    fun parseToCents(raw: String): Long? {
        val text = raw.trim()
        if (text.isEmpty()) return null
        // 拒绝非数字字符（含负号），金额方向由收支类型决定
        if (!text.matches(Regex("""^\d{1,9}(\.\d{1,2})?$"""))) return null

        val cents = runCatching {
            BigDecimal(text)
                .multiply(BigDecimal(100))
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact()
        }.getOrNull() ?: return null

        return cents.takeIf { it in 1..MAX_AMOUNT_IN_CENTS }
    }

    /**
     * 把用户输入格式化为规范的金额文本，便于保存前统一显示（如 ".5" -> "0.5"）。
     *
     * @param raw 用户输入的原始文本
     * @return 规范化后的文本；无法解析时原样返回
     */
    fun normalize(raw: String): String {
        val cents = parseToCents(raw) ?: return raw.trim()
        return formatCents(cents, withSign = false)
    }

    /**
     * 将"分"格式化为两位小数的金额文本。
     *
     * @param cents 金额（分）
     * @param withSign 是否输出正负号，统计场景下需要
     * @return 形如 "1,234.50" 或 "-1,234.50" 的文本
     */
    fun formatCents(cents: Long, withSign: Boolean = false): String {
        val value = BigDecimal(cents).divide(BigDecimal(100), 2, RoundingMode.HALF_UP)
        val text = value.abs().setScale(2, RoundingMode.HALF_UP).toPlainString()
        return when {
            !withSign -> text
            cents < 0L -> "-$text"
            else -> "+$text"
        }
    }

    /**
     * 校验备注长度，超长文本会让列表布局被撑破。
     *
     * @param note 用户输入的备注
     * @return 校验通过返回 null，否则返回中文错误提示
     */
    fun validateNote(note: String): String? =
        if (note.trim().length > 50) "备注不能超过 50 个字" else null
}
