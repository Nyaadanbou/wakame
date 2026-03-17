package cc.mewcraft.wakame.catalog.item

import cc.mewcraft.lazyconfig.configurate.require
import cc.mewcraft.wakame.KoishDataPaths
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.catalog.item.node.CatalogItemSingleSourceNode
import cc.mewcraft.wakame.gui.BasicMenuSettings
import cc.mewcraft.wakame.item.ItemRef
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.registry.BuiltInRegistries
import cc.mewcraft.wakame.registry.DynamicRegistries
import cc.mewcraft.wakame.registry.RegistryLoader
import cc.mewcraft.wakame.util.IdePauser
import cc.mewcraft.wakame.util.KoishKey
import cc.mewcraft.wakame.util.configurate.yamlLoader
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.spongepowered.configurate.kotlin.extensions.getList
import kotlin.io.path.*

@Init(
    stage = InitStage.POST_WORLD,
    runAfter = [
        CatalogItemMenuSettings::class, // 要等预设菜单布局载入好
    ]
)
internal object CatalogItemSingleSourceNodeInitializer : RegistryLoader {

    @InitFun
    fun init() {
        DynamicRegistries.CATALOG_ITEM_SINGLE_SOURCE_NODE.resetRegistry()
        applyDataToRegistry(DynamicRegistries.CATALOG_ITEM_SINGLE_SOURCE_NODE::add)
    }

    fun reload() {
        applyDataToRegistry(DynamicRegistries.CATALOG_ITEM_SINGLE_SOURCE_NODE::upsert)
    }

    private fun applyDataToRegistry(registryAction: (KoishKey, CatalogItemSingleSourceNode) -> Unit) {
        val nodeDir = KoishDataPaths.CONFIGS.resolve("catalog/item/node/single_source")
        val mappingsFile = KoishDataPaths.CONFIGS.resolve("catalog/item/layout/node/single_source/mappings.yml")

        // 读取 nodeId → menuKey 和 nodeId → iconKey 的映射
        val nodeIdToMenuId = HashMap<String, String>()
        val nodeIdToIconId = HashMap<String, Key>()
        val regexCache = HashMap<String, Regex>()

        // 首先收集所有单源节点的 ID (相对路径, 不含后缀)
        val nodeIds = HashSet<String>()
        if (nodeDir.exists()) {
            nodeDir.walk()
                .filter { it.isRegularFile() && it.extension == "yml" }
                .forEach { file ->
                    val nodeId = file.relativeTo(nodeDir).invariantSeparatorsPathString.substringBeforeLast('.')
                    nodeIds.add(nodeId)
                }
        }

        // 读取 mappings.yml 中的菜单设置映射和输入图标映射
        try {
            val loader = yamlLoader { withDefaults() }
            val rootNode = loader.buildAndLoadString(mappingsFile.readText())

            for ((nodeKey, node) in rootNode.node("menu_setting_mappings").childrenMap()) {
                val regex = regexCache.computeIfAbsent(nodeKey.toString(), ::Regex)
                for (nodeId in nodeIds) {
                    if (nodeId.matches(regex)) {
                        nodeIdToMenuId.putIfAbsent(nodeId, node.require<String>())
                    }
                }
            }

            for ((nodeKey, node) in rootNode.node("input_icon_mappings").childrenMap()) {
                val regex = regexCache.computeIfAbsent(nodeKey.toString(), ::Regex)
                for (nodeId in nodeIds) {
                    if (nodeId.matches(regex)) {
                        nodeIdToIconId.putIfAbsent(nodeId, node.require<Key>())
                    }
                }
            }
        } catch (e: Throwable) {
            IdePauser.pauseInIde(e)
            LOGGER.error("Failed to read catalog single source mappings from: '${mappingsFile.name}'", e)
        }

        // 遍历所有单源节点配置文件, 读取节点数据并注册
        var count = 0
        for (nodeId in nodeIds) {
            val file = nodeDir.resolve("$nodeId.yml")
            try {
                val loader = yamlLoader { withDefaults() }
                val rootNode = loader.buildAndLoadString(file.readText())

                val displayName = rootNode.node("display_name").require<String>()
                val outputItemIds = rootNode.node("output_items").getList<KoishKey>(emptyList())
                val outputItems = outputItemIds.mapNotNull { id ->
                    ItemRef.create(id).also {
                        if (it == null) LOGGER.warn("Invalid output item '$id' in single source node '$nodeId'")
                    }
                }

                val inputIcon = nodeIdToIconId[nodeId] ?: BuiltInRegistries.ITEM.defaultId
                val menuCfg = nodeIdToMenuId[nodeId]?.let(CatalogItemMenuSettings::getMenuSettings)
                    ?: BasicMenuSettings(Component.text("Untitled"), emptyArray(), hashMapOf())

                registryAction(
                    KoishKey.key(nodeId),
                    CatalogItemSingleSourceNode(
                        nodeId = nodeId,
                        displayName = displayName,
                        outputItems = outputItems,
                        inputIcon = inputIcon,
                        menuCfg = menuCfg,
                    )
                )
                count++
            } catch (e: Throwable) {
                IdePauser.pauseInIde(e)
                LOGGER.error("Failed to register catalog single source node from: '$nodeId'", e)
            }
        }
        LOGGER.info("Applied $count catalog item single source nodes to registry")
    }
}