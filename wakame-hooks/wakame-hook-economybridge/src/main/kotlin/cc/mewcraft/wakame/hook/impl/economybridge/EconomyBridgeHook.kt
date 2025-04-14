package cc.mewcraft.wakame.hook.impl.economybridge

import cc.mewcraft.economy.api.EconomyProvider
import cc.mewcraft.wakame.integration.Hook
import cc.mewcraft.wakame.item2.KoishItemRefHandler
import cc.mewcraft.wakame.util.Identifiers
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import su.nightexpress.economybridge.EconomyBridge
import su.nightexpress.economybridge.ItemBridge
import su.nightexpress.economybridge.api.Currency
import su.nightexpress.economybridge.item.handler.AbstractItemHandler
import java.util.*
import cc.mewcraft.economy.api.Currency as KoishCurrency

@Hook(plugins = ["EconomyBridge"])
object EconomyBridgeHook {

    init {
        // 向 EconomyBridge 注册 Koish 物品
        ItemBridge.getItemManager().register(KoishItemHandler)

        // 向 EconomyBridge 注册 Koish 货币
        // FIXME 这样注册的货币会在 EconomyBridge 执行重载指令时失效
        for (koishCurrency in EconomyProvider.get().loadedCurrencies) {
            EconomyBridge.getCurrencyManager().registerCurrency(EconomyCurrency(koishCurrency))
        }
    }

}

object KoishItemHandler : AbstractItemHandler() {

    override fun getName(): String {
        return KoishItemRefHandler.systemName
    }

    override fun canHandle(p0: ItemStack): Boolean {
        return KoishItemRefHandler.getId(p0) != null
    }

    override fun createItem(p0: String): ItemStack? {
        val id = Identifiers.tryParse(p0) ?: return null
        return KoishItemRefHandler.createItemStack(id, 1, null)
    }

    override fun getItemId(p0: ItemStack): String? {
        return KoishItemRefHandler.getId(p0)?.asString()
    }

    override fun isValidId(p0: String): Boolean {
        val id = Identifiers.of(p0)
        return KoishItemRefHandler.accepts(id)
    }
}

class EconomyCurrency(
    private val currency: KoishCurrency,
) : Currency {

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