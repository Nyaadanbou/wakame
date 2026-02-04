package cc.mewcraft.wakame.hook.impl.nightcore

import cc.mewcraft.economy.api.Currency
import cc.mewcraft.economy.api.EconomyProvider
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import su.nightexpress.nightcore.integration.currency.type.AbstractCurrency
import su.nightexpress.nightcore.util.NumberUtil
import java.util.*
import kotlin.math.floor

class EconomyCurrency(
    private val currency: Currency,
) : AbstractCurrency(currency.name) {

    companion object {

        @JvmStatic
        fun currencies(): Set<EconomyCurrency> {
            return EconomyProvider.get().getLoadedCurrencies()
                .filter(Currency::isDefaultCurrency)
                .map(::EconomyCurrency)
                .toHashSet()
        }
    }

    override fun getBalance(player: Player): Double {
        return EconomyProvider.get().getBalance(player.uniqueId, this.currency)
    }

    override fun getBalance(playerId: UUID): Double {
        return EconomyProvider.get().getBalance(playerId, this.currency)
    }

    override fun give(player: Player, amount: Double) {
        EconomyProvider.get().deposit(player.uniqueId, amount, this.currency)
    }

    override fun give(playerId: UUID, amount: Double) {
        EconomyProvider.get().deposit(playerId, amount, this.currency)
    }

    override fun take(player: Player, amount: Double) {
        EconomyProvider.get().withdraw(player.uniqueId, amount, this.currency)
    }

    override fun take(playerId: UUID, amount: Double) {
        EconomyProvider.get().withdraw(playerId, amount, this.currency)
    }

    override fun canHandleDecimals(): Boolean {
        return this.currency.isDecimalSupported
    }

    override fun canHandleOffline(): Boolean {
        return true
    }

    override fun getName(): String {
        return this.currency.symbolOrEmpty
    }

    override fun getFormat(): String {
        return "@" // 我们直接处理自己的格式
    }

    override fun getIcon(): ItemStack {
        return ItemStack(Material.SUNFLOWER)
    }

    override fun floorIfNeeded(amount: Double): Double {
        return if (!this.currency.isDecimalSupported) floor(amount) else amount
    }

    override fun formatValue(amount: Double): String {
        return NumberUtil.format(this.floorIfNeeded(amount));
    }

    override fun format(amount: Double): String {
        return this.currency.fancyFormat(amount)
    }

    override fun applyFormat(format: String, amount: Double): String {
        throw UnsupportedOperationException()
    }
}