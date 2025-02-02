package cc.mewcraft.wakame.integration.economy

import cc.mewcraft.wakame.integration.economy.intrinsics.VanillaEconomyIntegration
import java.util.*

object EconomyManager {

    // 初始化时, 将 LevelEconomyIntegration 作为默认的经济系统.
    // 如果有其他经济系统存在并且需要被使用, 该字段应该被重新赋值.
    internal var integration: EconomyIntegration = VanillaEconomyIntegration

    /**
     * 检查玩家是否有足够的货币.
     */
    fun has(uuid: UUID, amount: Double): Result<Boolean> {
        return integration.has(uuid, amount)
    }

    /**
     * 从玩家身上扣除货币.
     */
    fun take(uuid: UUID, amount: Double): Result<Boolean> {
        return integration.take(uuid, amount)
    }

    /**
     * 给玩家增加货币.
     */
    fun give(uuid: UUID, amount: Double): Result<Boolean> {
        return integration.give(uuid, amount)
    }
}