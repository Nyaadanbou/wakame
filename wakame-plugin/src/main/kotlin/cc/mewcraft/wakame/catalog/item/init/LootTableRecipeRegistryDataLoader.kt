package cc.mewcraft.wakame.catalog.item.init

import cc.mewcraft.wakame.catalog.item.recipe.LootTableRecipe
import cc.mewcraft.wakame.catalog.item.recipe.MojangLootTable
import cc.mewcraft.wakame.core.ItemXFactoryRegistry
import cc.mewcraft.wakame.core.ItemXNeko
import cc.mewcraft.wakame.gui.BasicMenuSettings
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.lifecycle.reloader.Reload
import cc.mewcraft.wakame.lifecycle.reloader.ReloadFun
import cc.mewcraft.wakame.registry2.KoishRegistries
import cc.mewcraft.wakame.registry2.RegistryConfigStorage
import cc.mewcraft.wakame.util.Identifier
import cc.mewcraft.wakame.util.MINECRAFT_SERVER
import cc.mewcraft.wakame.util.buildYamlConfigLoader
import cc.mewcraft.wakame.util.krequire
import cc.mewcraft.wakame.util.text.mini
import net.kyori.adventure.text.Component
import net.minecraft.core.HolderLookup
import net.minecraft.core.registries.Registries
import kotlin.streams.asSequence

@Init(
    stage = InitStage.POST_WORLD,
    runAfter = [ItemCatalogMenuSettings::class] // 要等预设菜单布局载入好
)
@Reload
internal object LootTableRecipeRegistryDataLoader : RegistryConfigStorage {

    private val DEFAULT_ICON_ITEM = ItemXNeko("${KoishRegistries.ITEM.defaultId.namespace()}/${KoishRegistries.ITEM.defaultId.value()}")

    private val MINECRAFT_LOOT_TABLES: HashMap<String, MojangLootTable> = HashMap(1024)

    @InitFun
    fun init() {
        refreshMinecraftLootTable()
        KoishRegistries.LOOT_TABLE_RECIPE.resetRegistry()
        applyDataToRegistry(KoishRegistries.LOOT_TABLE_RECIPE::add)
        KoishRegistries.LOOT_TABLE_RECIPE.freeze()
    }

    @ReloadFun
    fun reload() {
        refreshMinecraftLootTable()
        applyDataToRegistry(KoishRegistries.LOOT_TABLE_RECIPE::update)
    }

    private fun refreshMinecraftLootTable() {
        val lookupProvider: HolderLookup.Provider = MINECRAFT_SERVER.reloadableRegistries().lookup() as HolderLookup.Provider
        val lootTableRegistryLookup: HolderLookup.RegistryLookup<MojangLootTable> = lookupProvider.lookupOrThrow(Registries.LOOT_TABLE)
        MINECRAFT_LOOT_TABLES.clear()
        MINECRAFT_LOOT_TABLES.putAll(lootTableRegistryLookup.listElements().asSequence().associate { holder ->
            holder.key().location().toString() to holder.value()
        })
    }

    private fun applyDataToRegistry(registryAction: (Identifier, LootTableRecipe) -> Unit) {
        val loader = buildYamlConfigLoader { withDefaults() }
        val rootNode = loader.buildAndLoadString(getFileInConfigDirectory("catalog/item/loot_tables.yml").readText())

        // 所有要在图鉴中展示的战利品表的路径
        val lootTablePaths = hashSetOf<String>()

        val titleMap = hashMapOf<String, String>()
        for ((nodeKey, node) in rootNode.node("title").childrenMap()) {
            val regex = Regex(nodeKey.toString())
            MINECRAFT_LOOT_TABLES.filter { (key, _) ->
                key.matches(regex)
            }.forEach { (key, _) ->
                lootTablePaths.add(key)
                titleMap.putIfAbsent(key, node.krequire<String>())
            }
        }

        val menuSettingsMap = hashMapOf<String, String>()
        for ((nodeKey, node) in rootNode.node("menu_settings").childrenMap()) {
            val regex = Regex(nodeKey.toString())
            MINECRAFT_LOOT_TABLES.filter { (key, _) ->
                key.matches(regex)
            }.forEach { (key, _) ->
                lootTablePaths.add(key)
                menuSettingsMap.putIfAbsent(key, node.krequire<String>())
            }
        }

        val iconMap = hashMapOf<String, String>()
        for ((nodeKey, node) in rootNode.node("icon").childrenMap()) {
            val regex = Regex(nodeKey.toString())
            MINECRAFT_LOOT_TABLES.filter { (key, _) ->
                key.matches(regex)
            }.forEach { (key, _) ->
                lootTablePaths.add(key)
                iconMap.putIfAbsent(key, node.krequire<String>())
            }
        }

        lootTablePaths.forEach { path ->
            val title = titleMap[path]?.mini ?: Component.text("Untitled")

            val iconString = iconMap[path]
            val iconItem = if (iconString != null) ItemXFactoryRegistry[iconString] else null

            val menuSettingsStr = menuSettingsMap[path]
            val menuSettings = if (menuSettingsStr != null) {
                ItemCatalogMenuSettings.getMenuSettings(menuSettingsStr)
            } else {
                BasicMenuSettings(Component.text("Untitled"), emptyArray(), hashMapOf())
            }

            registryAction(
                Identifier.key(path),
                LootTableRecipe(
                    lootTablePath = path,
                    lootTable = MINECRAFT_LOOT_TABLES[path]!!,
                    catalogIcon = iconItem ?: DEFAULT_ICON_ITEM,
                    catalogTitle = title,
                    catalogMenuSettings = menuSettings
                )
            )
        }
    }
}