package cc.mewcraft.wakame.catalog.item

import cc.mewcraft.lazyconfig.configurate.require
import cc.mewcraft.wakame.KoishDataPaths
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.catalog.item.node.CatalogItemLootTableNode
import cc.mewcraft.wakame.gui.BasicMenuSettings
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.registry.BuiltInRegistries
import cc.mewcraft.wakame.registry.DynamicRegistries
import cc.mewcraft.wakame.registry.RegistryLoader
import cc.mewcraft.wakame.util.IdePauser
import cc.mewcraft.wakame.util.KoishKey
import cc.mewcraft.wakame.util.MINECRAFT_SERVER
import cc.mewcraft.wakame.util.MojangLootTable
import cc.mewcraft.wakame.util.configurate.yamlLoader
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.minecraft.core.HolderLookup
import net.minecraft.core.registries.Registries
import kotlin.io.path.name
import kotlin.io.path.readText
import kotlin.streams.asSequence

@Init(
    stage = InitStage.POST_WORLD,
    runAfter = [
        CatalogItemMenuSettings::class, // 要等预设菜单布局载入好
    ]
)
internal object CatalogItemLootTableNodeInitializer : RegistryLoader {

    // 默认的战利品表数量庞大, 使用较大的容量以减少哈希冲突
    private val MINECRAFT_LOOT_TABLE_MAP: HashMap<String, MojangLootTable> = HashMap(2048)

    @InitFun
    fun init() {
        reloadMinecraftLootTables()
        DynamicRegistries.CATALOG_ITEM_MINECRAFT_LOOT_TABLE_NODE.resetRegistry()
        applyDataToRegistry(DynamicRegistries.CATALOG_ITEM_MINECRAFT_LOOT_TABLE_NODE::add)
    }

    fun reload() {
        reloadMinecraftLootTables()
        applyDataToRegistry(DynamicRegistries.CATALOG_ITEM_MINECRAFT_LOOT_TABLE_NODE::update)
    }

    // 重新读取服务端上的 Minecraft Loot Tables
    private fun reloadMinecraftLootTables() {
        val lookupProvider: HolderLookup.Provider = MINECRAFT_SERVER.reloadableRegistries().lookup() as HolderLookup.Provider
        val lootTableRegistryLookup: HolderLookup.RegistryLookup<MojangLootTable> = lookupProvider.lookupOrThrow(Registries.LOOT_TABLE)
        MINECRAFT_LOOT_TABLE_MAP.clear()
        MINECRAFT_LOOT_TABLE_MAP.putAll(lootTableRegistryLookup.listElements().asSequence().associate { holder ->
            holder.key().identifier().toString() to holder.value()
        })
    }

    private fun applyDataToRegistry(registryAction: (KoishKey, CatalogItemLootTableNode) -> Unit) {
        val mappingsFile = KoishDataPaths.CONFIGS.resolve("catalog/item/layout/node/loot_table/mappings.yml")

        // 所有要在图鉴中展示的战利品表的路径
        val lootTableIds = HashSet<String>(1024)
        val lootTableIdToMenuId = HashMap<String, String>(512)
        val lootTableIdToIconId = HashMap<String, Key>(512)

        // 为正则表达式创建临时缓存
        val regexCache = HashMap<String, Regex>(32)

        // 读取单个 mappings.yml 文件
        try {
            val loader = yamlLoader { withDefaults() }
            val rootNode = loader.buildAndLoadString(mappingsFile.readText())

            for ((nodeKey, node) in rootNode.node("menu_setting_mappings").childrenMap()) {
                val regex = regexCache.computeIfAbsent(nodeKey.toString(), ::Regex)
                MINECRAFT_LOOT_TABLE_MAP.filter { (key, _) ->
                    key.matches(regex)
                }.forEach { (key, _) ->
                    lootTableIds.add(key)
                    lootTableIdToMenuId.putIfAbsent(key, node.require<String>())
                }
            }

            for ((nodeKey, node) in rootNode.node("input_icon_mappings").childrenMap()) {
                val regex = regexCache.computeIfAbsent(nodeKey.toString(), ::Regex)
                MINECRAFT_LOOT_TABLE_MAP.filter { (key, _) ->
                    key.matches(regex)
                }.forEach { (key, _) ->
                    lootTableIds.add(key)
                    lootTableIdToIconId.putIfAbsent(key, node.require<Key>())
                }
            }
        } catch (e: Throwable) {
            IdePauser.pauseInIde(e)
            LOGGER.error("Failed to read catalog loot table mappings from: '${mappingsFile.name}'", e)
        }

        // 根据对应的菜单布局和图标, 注册战利品表配方
        for (lootTableId in lootTableIds) {
            val inputIcon = lootTableIdToIconId[lootTableId] ?: BuiltInRegistries.ITEM.defaultId
            val menuSettings = lootTableIdToMenuId[lootTableId]?.let(CatalogItemMenuSettings::getMenuSettings)
                ?: BasicMenuSettings(Component.text("Untitled"), emptyArray(), hashMapOf())

            try {
                registryAction(
                    KoishKey.key(lootTableId),
                    CatalogItemLootTableNode(
                        lootTableId = lootTableId,
                        lootTable = MINECRAFT_LOOT_TABLE_MAP[lootTableId] ?: error("Loot table '$lootTableId' not found in Minecraft loot tables"),
                        inputIcon = inputIcon,
                        menuCfg = menuSettings
                    )
                )
            } catch (e: Throwable) {
                IdePauser.pauseInIde(e)
                LOGGER.error("Failed to register catalog loot table node for loot table '$lootTableId'")
            }
        }
    }
}