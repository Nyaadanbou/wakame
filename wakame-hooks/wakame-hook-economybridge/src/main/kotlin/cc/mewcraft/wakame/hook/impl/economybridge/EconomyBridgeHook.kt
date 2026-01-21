package cc.mewcraft.wakame.hook.impl.economybridge

import cc.mewcraft.economy.api.EconomyProvider
import cc.mewcraft.wakame.integration.Hook
import cc.mewcraft.wakame.registry.BuiltInRegistries
import su.nightexpress.economybridge.EconomyBridge
import su.nightexpress.economybridge.ItemBridge

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