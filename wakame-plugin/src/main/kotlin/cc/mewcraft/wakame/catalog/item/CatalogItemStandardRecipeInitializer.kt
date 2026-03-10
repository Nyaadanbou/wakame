package cc.mewcraft.wakame.catalog.item

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.catalog.item.recipe.*
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
internal object CatalogItemStandardRecipeInitializer : RegistryLoader {

    @InitFun
    fun init() {
        DynamicRegistries.CATALOG_ITEM_STANDARD_RECIPE.resetRegistry()
        applyDataToRegistry(DynamicRegistries.CATALOG_ITEM_STANDARD_RECIPE::add)
        DynamicRegistries.CATALOG_ITEM_STANDARD_RECIPE.freeze()
    }

    fun reload() {
        applyDataToRegistry(DynamicRegistries.CATALOG_ITEM_STANDARD_RECIPE::upsert)
    }

    private fun applyDataToRegistry(registryAction: (KoishKey, CatalogItemStandardRecipe) -> Unit) {
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
    private fun BukkitRecipe.toCatalogRecipe(): CatalogItemStandardRecipe? {
        return when (this) {
            is BlastingRecipe -> CatalogItemBlastingRecipe(this)
            is CampfireRecipe -> CatalogItemCampfireRecipe(this)
            is FurnaceRecipe -> CatalogItemFurnaceRecipe(this)
            is ShapedRecipe -> CatalogItemShapedRecipe(this)
            is ShapelessRecipe -> CatalogItemShapelessRecipe(this)
            is SmithingTransformRecipe -> CatalogItemSmithingTransformRecipe(this)
            is SmithingTrimRecipe -> CatalogItemSmithingTrimRecipe(this)
            is SmokingRecipe -> CatalogItemSmokingRecipe(this)
            is StonecuttingRecipe -> CatalogItemStonecuttingRecipe(this)
            else -> null
        }
    }
}


