package cc.mewcraft.wakame.hook.impl.thebrewingproject

import cc.mewcraft.wakame.hook.impl.thebrewingproject.integration.item.KoishIntegration
import cc.mewcraft.wakame.integration.Hook
import cc.mewcraft.wakame.integration.HookStage
import cc.mewcraft.wakame.registry.BuiltInRegistries
import dev.jsinco.brewery.bukkit.api.integration.IntegrationTypes


@Hook(
    plugins = ["TheBrewingProject"],
    stage = HookStage.PRE_WORLD
)
object TheBrewingProjectHook {

    init {
        // 使 Koish 可以识别 TheBrewingProject 物品
        registerKoishItemIntegration()

        // 使 TheBrewingProject 可以识别 Koish 物品
        registerTBPItemIntegration()
    }

    private fun registerKoishItemIntegration() {
        BuiltInRegistries.ITEM_REF_HANDLER_EXTERNAL.add("thebrewingproject", TheBrewingProjectItemRefHandler)
    }

    private fun registerTBPItemIntegration() {
        HookUtils.withApi { api ->
            api.integrationManager.register(IntegrationTypes.ITEM, KoishIntegration())
        }
    }
}