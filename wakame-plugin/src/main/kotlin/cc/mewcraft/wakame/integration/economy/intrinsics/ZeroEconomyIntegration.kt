package cc.mewcraft.wakame.integration.economy.intrinsics

import cc.mewcraft.wakame.integration.economy.EconomyIntegration
import cc.mewcraft.wakame.integration.economy.EconomyType
import java.util.*

internal object ZeroEconomyIntegration : EconomyIntegration {

    override val type: EconomyType = EconomyType.ZERO
    override fun has(uuid: UUID, amount: Double): Result<Boolean> = Result.success(false)
    override fun take(uuid: UUID, amount: Double): Result<Boolean> = Result.success(false)
    override fun give(uuid: UUID, amount: Double): Result<Boolean> = Result.success(false)

}