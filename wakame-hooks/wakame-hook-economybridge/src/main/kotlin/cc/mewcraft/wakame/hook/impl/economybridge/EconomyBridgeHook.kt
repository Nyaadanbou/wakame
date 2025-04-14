package cc.mewcraft.wakame.hook.impl.economybridge

import cc.mewcraft.economy.api.EconomyProvider
import cc.mewcraft.wakame.integration.Hook
import cc.mewcraft.wakame.item2.ItemRefHandler
import cc.mewcraft.wakame.item2.KoishItemRefHandler
import cc.mewcraft.wakame.registry2.BuiltInRegistries
import cc.mewcraft.wakame.util.Identifier
import cc.mewcraft.wakame.util.Identifiers
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import su.nightexpress.economybridge.EconomyBridge
import su.nightexpress.economybridge.ItemBridge
import su.nightexpress.economybridge.api.Currency
import su.nightexpress.economybridge.api.item.ItemHandler
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

        // 向 Koish 注册 ExcellentCrates 物品
        val handler = ItemBridge.getItemManager().getHandler("ExcellentCrates")
        if (handler != null) {
            BuiltInRegistries.ITEM_REF_HANDLER_EXTERNAL.add("excellentcrates", CrateItemRefHandler(handler))
        }
    }

}

// 让 Koish 能够识别 ExcellentCrates 的物品 (实体盲盒钥匙)
class CrateItemRefHandler(
    val handler: ItemHandler,
) : ItemRefHandler<ItemStack> {

    companion object {
        const val NAMESPACE = "excellentcrates"
    }

    override val systemName: String = "ExcellentCrates"

    override fun accepts(id: Identifier): Boolean {
        return id.namespace() == NAMESPACE && handler.isValidId(id.value())
    }

    override fun getId(stack: ItemStack): Identifier? {
        val id = handler.getItemId(stack) ?: return null
        return Identifiers.tryParse(NAMESPACE, id)
    }

    override fun getName(id: Identifier): Component? {
        return Component.text(id.asString())
    }

    override fun getInternalType(id: Identifier): ItemStack? {
        if (id.namespace() != NAMESPACE) return null
        return handler.createItem(id.value())
    }

    override fun createItemStack(id: Identifier, amount: Int, player: Player?): ItemStack? {
        return getInternalType(id)?.apply { this.amount = amount }
    }
}

// 让 EconomyBridge 能识别来自 Koish 的物品
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

// 让 EconomyBridge 能识别来自 Economy 的货币
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