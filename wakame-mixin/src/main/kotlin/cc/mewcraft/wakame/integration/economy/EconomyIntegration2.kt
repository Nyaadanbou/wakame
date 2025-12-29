package cc.mewcraft.wakame.integration.economy

import cc.mewcraft.wakame.integration.economy.intrinsics.VanillaEconomyIntegration
import java.util.*

interface EconomyIntegration2 {

    val type: EconomyType

    val defaultCurrency: String?

    fun has(user: UUID, amount: Double, currency: String? = null): Result<Boolean>

    fun hasAcc(user: UUID, amount: Double, currency: String? = null): Result<Boolean>

    fun take(user: UUID, amount: Double, currency: String? = null): Result<Boolean>

    fun give(user: UUID, amount: Double, currency: String? = null): Result<Boolean>

    companion object : EconomyIntegration2 {

        private var implementation: EconomyIntegration2 = VanillaEconomyIntegration

        fun setImplementation(impl: EconomyIntegration2) {
            implementation = impl
        }

        override val type: EconomyType get() = implementation.type
        override val defaultCurrency: String? get() = implementation.defaultCurrency
        override fun has(user: UUID, amount: Double, currency: String?): Result<Boolean> = implementation.has(user, amount, currency)
        override fun hasAcc(user: UUID, amount: Double, currency: String?): Result<Boolean> = implementation.hasAcc(user, amount, currency)
        override fun take(user: UUID, amount: Double, currency: String?): Result<Boolean> = implementation.take(user, amount, currency)
        override fun give(user: UUID, amount: Double, currency: String?): Result<Boolean> = implementation.give(user, amount, currency)
    }
}