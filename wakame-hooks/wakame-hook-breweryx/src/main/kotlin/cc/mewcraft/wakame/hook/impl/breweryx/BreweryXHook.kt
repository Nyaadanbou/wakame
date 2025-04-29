package cc.mewcraft.wakame.hook.impl.breweryx

import cc.mewcraft.wakame.brewery.BrewRecipeManager
import cc.mewcraft.wakame.brewery.BrewRecipeRenderer
import cc.mewcraft.wakame.integration.Hook
import cc.mewcraft.wakame.registry2.BuiltInRegistries
import cc.mewcraft.wakame.util.Identifiers
import com.dre.brewery.BarrelWoodType
import com.dre.brewery.recipe.PluginItem
import cc.mewcraft.wakame.brewery.BarrelWoodType as KBarrelWoodType

@Hook(plugins = ["BreweryX"])
object BreweryXHook {

    init {
        registerItemHandlers()
        registerBarrelWoodTypes()
        registerApiImplementations()
    }

    private fun registerItemHandlers() {
        // 使 BreweryX 可以识别 Koish 物品
        PluginItem.registerForConfig("koish", ::KoishPluginItem)
        // 使 Koish 可以识别 BreweryX 物品
        BuiltInRegistries.ITEM_REF_HANDLER_EXTERNAL.add("brewery", BreweryXItemRefHandler)
    }

    private fun registerApiImplementations() {
        BrewRecipeManager.register(TheBrewRecipeManager)
        BrewRecipeRenderer.register(TheBrewRecipeRenderer)
    }

    private fun registerBarrelWoodTypes() {
        for (type in BarrelWoodType.entries) {
            val formattedName = type.formattedName
            val translatedName = type.formattedName // TODO #383: 支持 i18n
            val id = Identifiers.of(type.name.lowercase())
            val obj = KBarrelWoodType(formattedName, translatedName)
            KBarrelWoodType.REGISTRY.add(id, obj)
        }
    }
}

