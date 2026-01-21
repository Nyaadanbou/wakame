package cc.mewcraft.wakame.hook.impl.economybridge

import cc.mewcraft.economy.api.Currency
import cc.mewcraft.economy.api.EconomyProvider
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*

// 让 EconomyBridge 能识别来自 Economy 的货币
class EconomyCurrency(
    private val currency: Currency,
) : su.nightexpress.economybridge.api.Currency {

    override fun getBalance(p0: Player): Double {
        return EconomyProvider.get().getBalance(p0.uniqueId, currency)
    }

    override fun getBalance(p0: UUID): Double {
        return EconomyProvider.get().getBalance(p0, currency)
    }

    override fun give(p0: Player, p1: Double) {
        EconomyProvider.get().deposit(p0.uniqueId, p1, currency)
    }

    override fun give(p0: UUID, p1: Double) {
        EconomyProvider.get().deposit(p0, p1, currency)
    }

    override fun take(p0: Player, p1: Double) {
        EconomyProvider.get().withdraw(p0.uniqueId, p1, currency)
    }

    override fun take(p0: UUID, p1: Double) {
        EconomyProvider.get().withdraw(p0, p1, currency)
    }

    override fun format(amount: Double): String {
        return currency.fancyFormat(amount)
    }

    override fun canHandleDecimals(): Boolean {
        return currency.isDecimalSupported
    }

    override fun canHandleOffline(): Boolean {
        return true
    }

    override fun getOriginalId(): String {
        return currency.name
    }

    override fun getInternalId(): String {
        return currency.name.lowercase()
    }

    override fun getName(): String {
        return currency.displayName
    }

    override fun getDefaultName(): String {
        return currency.displayName
    }

    override fun getFormat(): String {
        return "@" // 这函数是干嘛用的?
    }

    override fun getIcon(): ItemStack {
        return ItemStack(Material.SUNFLOWER)
    }

    override fun getDefaultIcon(): ItemStack {
        return ItemStack(Material.SUNFLOWER)
    }
}