package cc.mewcraft.wakame.hook.impl.economy

import cc.mewcraft.economy.api.EconomyProvider
import cc.mewcraft.wakame.config.MAIN_CONFIG
import cc.mewcraft.wakame.config.entry
import cc.mewcraft.wakame.integration.Hook
import cc.mewcraft.wakame.integration.economy.EconomyIntegration2
import cc.mewcraft.wakame.integration.economy.EconomyType
import java.util.*

@Hook(plugins = ["Economy"])
object EconomyHook {

    private val ECONOMY_PROVIDER by MAIN_CONFIG.entry<EconomyType>("economy_provider")

    init {
        if (ECONOMY_PROVIDER == EconomyType.ECONOMY) {
            EconomyIntegration2.setImplementation(EconomyIntegrationImpl())
        }
    }
}

private class EconomyIntegrationImpl : EconomyIntegration2 {

    override val type: EconomyType = EconomyType.ECONOMY

    override val defaultCurrency: String
        get() = EconomyProvider.get().defaultCurrency.name

    override fun has(user: UUID, amount: Double, currency: String?): Result<Boolean> {
        return runCatching {
            val economy = EconomyProvider.get()
            val balance = if (currency == null) {
                economy.getBalance(user)
            } else {
                economy.getBalance(user, economy.getCurrency(currency) ?: throw IllegalArgumentException("Currency $currency not found"))
            }
            val result = balance >= amount
            result
        }
    }

    override fun take(user: UUID, amount: Double, currency: String?): Result<Boolean> {
        return runCatching {
            val economy = EconomyProvider.get()
            val result = if (currency == null) {
                economy.withdraw(user, amount)
            } else {
                economy.withdraw(user, amount, economy.getCurrency(currency) ?: throw IllegalArgumentException("Currency $currency not found"))
            }
            result
        }
    }

    override fun give(user: UUID, amount: Double, currency: String?): Result<Boolean> {
        return runCatching {
            val economy = EconomyProvider.get()
            val result = if (currency == null) {
                economy.deposit(user, amount)
            } else {
                economy.deposit(user, amount, economy.getCurrency(currency) ?: throw IllegalArgumentException("Currency $currency not found"))
            }
            result
        }
    }
}