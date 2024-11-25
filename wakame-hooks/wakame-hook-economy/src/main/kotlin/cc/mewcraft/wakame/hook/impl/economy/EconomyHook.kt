package cc.mewcraft.wakame.hook.impl.economy

import cc.mewcraft.economy.api.EconomyProvider
import cc.mewcraft.wakame.integration.Hook
import cc.mewcraft.wakame.integration.economy.EconomyIntegration
import cc.mewcraft.wakame.integration.economy.EconomyType
import java.util.UUID

@Hook(plugins = ["Economy"])
object EconomyHook : EconomyIntegration {
    override val type: EconomyType = EconomyType.ECONOMY

    override fun has(uuid: UUID, amount: Double): Result<Boolean> {
        return runCatching {
            val economy = EconomyProvider.get()
            val balance = economy.getBalance(uuid)
            val result = balance >= amount
            result
        }
    }

    override fun take(uuid: UUID, amount: Double): Result<Boolean> {
        return runCatching {
            val economy = EconomyProvider.get()
            val result = economy.withdraw(uuid, amount)
            result
        }
    }

    override fun give(uuid: UUID, amount: Double): Result<Boolean> {
        return runCatching {
            val economy = EconomyProvider.get()
            val result = economy.deposit(uuid, amount)
            result
        }
    }
}