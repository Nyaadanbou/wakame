package cc.mewcraft.wakame.compatibility.economy

import cc.mewcraft.economy.api.EconomyProvider
import cc.mewcraft.wakame.economy.Economy
import java.util.UUID

object GlobalEconomy : Economy {
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