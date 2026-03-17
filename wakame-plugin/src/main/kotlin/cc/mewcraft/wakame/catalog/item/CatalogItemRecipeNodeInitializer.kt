package cc.mewcraft.wakame.catalog.item

import cc.mewcraft.lazyconfig.configurate.require
import cc.mewcraft.wakame.KoishDataPaths
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.catalog.item.node.*
import cc.mewcraft.wakame.gui.BasicMenuSettings
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.registry.DynamicRegistries
import cc.mewcraft.wakame.registry.RegistryLoader
import cc.mewcraft.wakame.util.IdePauser
import cc.mewcraft.wakame.util.KoishKey
import cc.mewcraft.wakame.util.configurate.yamlLoader
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.inventory.*
import kotlin.io.path.readText
import org.bukkit.inventory.Recipe as BukkitRecipe

@Init(
    stage = InitStage.POST_WORLD,
    runAfter = [
        CatalogItemMenuSettings::class,
    ]
)
internal object CatalogItemRecipeNodeInitializer : RegistryLoader {

    // 配方类型名 → 菜单 key 的映射 (从 mappings.yml 中读取)
    private val RECIPE_TYPE_TO_MENU_KEY: HashMap<String, String> = HashMap()

    @InitFun
    fun init() {
        loadMappings()
        DynamicRegistries.CATALOG_ITEM_MINECRAFT_RECIPE_NODE.resetRegistry()
        applyDataToRegistry(DynamicRegistries.CATALOG_ITEM_MINECRAFT_RECIPE_NODE::add)
    }

    fun reload() {
        loadMappings()
        applyDataToRegistry(DynamicRegistries.CATALOG_ITEM_MINECRAFT_RECIPE_NODE::upsert)
    }

    private fun loadMappings() {
        val mappingsFile = KoishDataPaths.CONFIGS.resolve("catalog/item/layout/node/recipe/mappings.yml")
        RECIPE_TYPE_TO_MENU_KEY.clear()

        try {
            val loader = yamlLoader { withDefaults() }
            val rootNode = loader.buildAndLoadString(mappingsFile.readText())

            val regexCache = HashMap<String, Regex>()

            // 所有已知的配方类型名
            val knownTypeNames = listOf(
                CatalogItemNodeType.BLASTING_RECIPE,
                CatalogItemNodeType.CAMPFIRE_RECIPE,
                CatalogItemNodeType.FURNACE_RECIPE,
                CatalogItemNodeType.SHAPED_RECIPE,
                CatalogItemNodeType.SHAPELESS_RECIPE,
                CatalogItemNodeType.SMITHING_TRANSFORM_RECIPE,
                CatalogItemNodeType.SMITHING_TRIM_RECIPE,
                CatalogItemNodeType.SMOKING_RECIPE,
                CatalogItemNodeType.STONECUTTING_RECIPE,
            ).map(CatalogItemNodeType::name)

            for ((nodeKey, node) in rootNode.node("menu_setting_mappings").childrenMap()) {
                val regex = regexCache.computeIfAbsent(nodeKey.toString(), ::Regex)
                for (typeName in knownTypeNames) {
                    if (typeName.matches(regex)) {
                        RECIPE_TYPE_TO_MENU_KEY.putIfAbsent(typeName, node.require<String>())
                    }
                }
            }
        } catch (e: Throwable) {
            IdePauser.pauseInIde(e)
            LOGGER.error("Failed to read catalog recipe mappings from: '${mappingsFile}'", e)
        }
    }

    /**
     * 根据配方类型名获取菜单设置.
     */
    private fun getMenuSettingsForType(typeName: String): BasicMenuSettings {
        val menuKey = RECIPE_TYPE_TO_MENU_KEY[typeName]
        return menuKey?.let(CatalogItemMenuSettings::getMenuSettings)
            ?: BasicMenuSettings(Component.text("Untitled"), emptyArray(), hashMapOf())
    }

    private fun applyDataToRegistry(registryAction: (KoishKey, CatalogItemRecipeNode) -> Unit) {
        var count = 0
        for (bukkitRecipe in Bukkit.recipeIterator()) {
            val catalogRecipe = bukkitRecipe.toCatalogItemNode() ?: continue
            try {
                registryAction(
                    catalogRecipe.recipeId,
                    catalogRecipe,
                )
                count++
            } catch (e: Throwable) {
                LOGGER.error("Failed to register catalog item recipe node for '${catalogRecipe.recipeId}'", e)
            }
        }
        LOGGER.info("Applied $count catalog item recipe nodes to catalog item recipe node registry")
    }

    /**
     * 方便函数.
     * 返回 `null` 意味着无法转化, 即图鉴不支持显示的特殊配方.
     */
    private fun BukkitRecipe.toCatalogItemNode(): CatalogItemRecipeNode? {
        return when (this) {
            is BlastingRecipe -> CatalogItemBlastingNode(this, getMenuSettingsForType(CatalogItemNodeType.BLASTING_RECIPE.name))
            is CampfireRecipe -> CatalogItemCampfireNode(this, getMenuSettingsForType(CatalogItemNodeType.CAMPFIRE_RECIPE.name))
            is FurnaceRecipe -> CatalogItemFurnaceNode(this, getMenuSettingsForType(CatalogItemNodeType.FURNACE_RECIPE.name))
            is ShapedRecipe -> CatalogItemShapedNode(this, getMenuSettingsForType(CatalogItemNodeType.SHAPED_RECIPE.name))
            is ShapelessRecipe -> CatalogItemShapelessNode(this, getMenuSettingsForType(CatalogItemNodeType.SHAPELESS_RECIPE.name))
            is SmithingTransformRecipe -> CatalogItemSmithingTransformNode(this, getMenuSettingsForType(CatalogItemNodeType.SMITHING_TRANSFORM_RECIPE.name))
            is SmithingTrimRecipe -> CatalogItemSmithingTrimNode(this, getMenuSettingsForType(CatalogItemNodeType.SMITHING_TRIM_RECIPE.name))
            is SmokingRecipe -> CatalogItemSmokingNode(this, getMenuSettingsForType(CatalogItemNodeType.SMOKING_RECIPE.name))
            is StonecuttingRecipe -> CatalogItemStonecuttingNode(this, getMenuSettingsForType(CatalogItemNodeType.STONECUTTING_RECIPE.name))
            else -> null
        }
    }
}
