package cc.mewcraft.wakame.catalog.item

import cc.mewcraft.wakame.KoishDataPaths
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.Util
import cc.mewcraft.wakame.catalog.item.recipe.CatalogItemLootTableRecipe
import cc.mewcraft.wakame.catalog.item.recipe.MojangLootTable
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
import cc.mewcraft.wakame.util.require
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.minecraft.core.HolderLookup
import net.minecraft.core.registries.Registries
import kotlin.io.path.*
import kotlin.streams.asSequence

@Init(
    stage = InitStage.POST_WORLD,
    runAfter = [CatalogItemMenuSettings::class] // 要等预设菜单布局载入好
)
@Reload
internal object CatalogItemLootTableRecipeRegistryLoader : RegistryConfigStorage {

    // 默认的战利品表数量庞大, 使用较大的容量以减少哈希冲突
    private val MINECRAFT_LOOT_TABLE_MAP: HashMap<String, MojangLootTable> = HashMap(2048)

    @InitFun
    fun init() {
        reloadMinecraftLootTables()
        KoishRegistries.LOOT_TABLE_RECIPE.resetRegistry()
        applyDataToRegistry(KoishRegistries.LOOT_TABLE_RECIPE::add)
        KoishRegistries.LOOT_TABLE_RECIPE.freeze()
    }

    @ReloadFun
    fun reload() {
        reloadMinecraftLootTables()
        applyDataToRegistry(KoishRegistries.LOOT_TABLE_RECIPE::update)
    }

    // 重新读取服务端上的 Minecraft Loot Tables
    private fun reloadMinecraftLootTables() {
        val lookupProvider: HolderLookup.Provider = MINECRAFT_SERVER.reloadableRegistries().lookup() as HolderLookup.Provider
        val lootTableRegistryLookup: HolderLookup.RegistryLookup<MojangLootTable> = lookupProvider.lookupOrThrow(Registries.LOOT_TABLE)
        MINECRAFT_LOOT_TABLE_MAP.clear()
        MINECRAFT_LOOT_TABLE_MAP.putAll(lootTableRegistryLookup.listElements().asSequence().associate { holder ->
            holder.key().location().toString() to holder.value()
        })
    }

    private fun applyDataToRegistry(registryAction: (Identifier, CatalogItemLootTableRecipe) -> Unit) {
        val lootTableDir = KoishDataPaths.CONFIGS.resolve("catalog/item/loot_table/")

        // 所有要在图鉴中展示的战利品表的路径
        val lootTableIds = HashSet<String>(1024)
        val lootTableIdToMenuId = HashMap<String, String>(512)
        val lootTableIdToIconId = HashMap<String, Key>(512)

        // 为正则表达式创建临时缓存
        val regexCache = HashMap<String, Regex>(32)

        // 遍历所有文件, 读取菜单布局和图标
        lootTableDir.walk()
            .filter { it.extension == "yml" }
            .forEach { file ->
                LOGGER.info("Found ${file.name}")
                try {
                    val loader = buildYamlConfigLoader { withDefaults() }
                    val rootNode = loader.buildAndLoadString(file.readText())

                    for ((nodeKey, node) in rootNode.node("menu_settings").childrenMap()) {
                        val regex = regexCache.computeIfAbsent(nodeKey.toString(), ::Regex)
                        MINECRAFT_LOOT_TABLE_MAP.filter { (key, _) ->
                            key.matches(regex)
                        }.forEach { (key, _) ->
                            lootTableIds.add(key)
                            lootTableIdToMenuId.putIfAbsent(key, node.require<String>())
                        }
                    }

                    for ((nodeKey, node) in rootNode.node("icon").childrenMap()) {
                        val regex = regexCache.computeIfAbsent(nodeKey.toString(), ::Regex)
                        MINECRAFT_LOOT_TABLE_MAP.filter { (key, _) ->
                            key.matches(regex)
                        }.forEach { (key, _) ->
                            lootTableIds.add(key)
                            lootTableIdToIconId.putIfAbsent(key, node.require<Key>())
                        }
                    }
                } catch (e: Throwable) {
                    Util.pauseInIde(IllegalStateException("Can't load catalog loot table recipes in file: '${file.relativeTo(lootTableDir)}'", e))
                }
            }

        // 根据对应的菜单布局和图标, 注册战利品表配方
        lootTableIds.forEach { lootTableId ->
            val icon = lootTableIdToIconId[lootTableId] ?: KoishRegistries.ITEM.defaultId
            val menuSettings = lootTableIdToMenuId[lootTableId]?.let(CatalogItemMenuSettings::getMenuSettings)
                ?: BasicMenuSettings(Component.text("Untitled"), emptyArray(), hashMapOf())

            registryAction(
                Identifier.key(lootTableId),
                CatalogItemLootTableRecipe(
                    lootTableId = lootTableId,
                    lootTable = MINECRAFT_LOOT_TABLE_MAP[lootTableId]!!,
                    catalogIcon = icon,
                    catalogMenuSettings = menuSettings
                )
            )
        }
    }
}