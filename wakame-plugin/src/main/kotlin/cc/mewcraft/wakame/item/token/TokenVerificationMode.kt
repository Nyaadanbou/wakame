package cc.mewcraft.wakame.item.token

/**
 * 兑换券的令牌校验模式.
 *
 * - [REQUIRED]: 兑换时必须校验一次性令牌. 适用于高价值兑换券 (如涉及人民币的道具).
 * - [SKIPPED]: 兑换时跳过令牌校验, 直接执行动作. 适用于低价值、无需防复制保护的兑换券.
 */
enum class TokenVerificationMode {
    REQUIRED,
    SKIPPED,
}
