package cc.mewcraft.wakame.catalog.item

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.catalog.item.node.*
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.registry.DynamicRegistries
import cc.mewcraft.wakame.registry.RegistryLoader
import cc.mewcraft.wakame.util.KoishKey
import org.bukkit.Bukkit
import org.bukkit.inventory.*
import org.bukkit.inventory.Recipe as BukkitRecipe

@Init(
    stage = InitStage.POST_WORLD,
)
internal object CatalogItemRecipeNodeInitializer : RegistryLoader {

    @InitFun
    fun init() {
        DynamicRegistries.CATALOG_ITEM_STANDARD_RECIPE.resetRegistry()
        applyDataToRegistry(DynamicRegistries.CATALOG_ITEM_STANDARD_RECIPE::add)
        //DynamicRegistries.CATALOG_ITEM_STANDARD_RECIPE.freeze()
    }

    fun reload() {
        applyDataToRegistry(DynamicRegistries.CATALOG_ITEM_STANDARD_RECIPE::upsert)
    }

    private fun applyDataToRegistry(registryAction: (KoishKey, CatalogItemStandardNode) -> Unit) {
        var count = 0
        for (bukkitRecipe in Bukkit.recipeIterator()) {
            val catalogRecipe = bukkitRecipe.toCatalogRecipe() ?: continue
            try {
                registryAction(
                    catalogRecipe.recipeId,
                    catalogRecipe,
                )
                count++
            } catch (e: Throwable) {
                LOGGER.error("Failed to register catalog standard recipe for '${catalogRecipe.recipeId}'", e)
            }
        }
        LOGGER.info("Applied $count standard recipes to catalog registry")
    }

    /**
     * 方便函数.
     * 返回 `null` 意味着无法转化, 即图鉴不支持显示的特殊配方.
     */
    private fun BukkitRecipe.toCatalogRecipe(): CatalogItemStandardNode? {
        return when (this) {
            is BlastingRecipe -> CatalogItemBlastingNode(this)
            is CampfireRecipe -> CatalogItemCampfireNode(this)
            is FurnaceRecipe -> CatalogItemFurnaceNode(this)
            is ShapedRecipe -> CatalogItemShapedNode(this)
            is ShapelessRecipe -> CatalogItemShapelessNode(this)
            is SmithingTransformRecipe -> CatalogItemSmithingTransformNode(this)
            is SmithingTrimRecipe -> CatalogItemSmithingTrimNode(this)
            is SmokingRecipe -> CatalogItemSmokingNode(this)
            is StonecuttingRecipe -> CatalogItemStonecuttingNode(this)
            else -> null
        }
    }
}


