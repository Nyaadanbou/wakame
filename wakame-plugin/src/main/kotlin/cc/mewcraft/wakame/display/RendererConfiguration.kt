package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.Reloadable
import cc.mewcraft.wakame.attribute.base.AttributeModifier
import cc.mewcraft.wakame.reloadable
import cc.mewcraft.wakame.util.NekoConfigurationLoader
import cc.mewcraft.wakame.util.NekoConfigurationNode
import cc.mewcraft.wakame.util.requireKt
import me.lucko.helper.text3.mini
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.slf4j.Logger

internal class RendererConfiguration(
    loader: NekoConfigurationLoader,
    private val logger: Logger,
) : Reloadable {
    companion object {
        private const val RENDERER_ORDER = "renderer_order"
        private const val RENDERER_STYLE = "renderer_style"

        private const val RENDERER_FIXED_LORE_SYMBOL = '^'
        private const val RENDERER_CONDITION_LORE_SYMBOL = "/"
    }

    private val root by reloadable { loader.load() }

    //<editor-fold desc="renderer_style.meta">
    val nameFormat by reloadable { root.node(RENDERER_STYLE, "meta", "name").requireKt<String>() }
    val loreFormat by reloadable {
        val loreNode = root.node(RENDERER_STYLE, "meta", "lore")
        MetaStylizer.LoreFormat(
            header = loreNode.node("header").requireKt<List<String>>().takeUnless(List<String>::isEmpty),
            line = loreNode.node("line").requireKt<String>(),
            bottom = loreNode.node("bottom").requireKt<List<String>>().takeUnless(List<String>::isEmpty)
        )
    }
    val levelFormat by reloadable { root.node(RENDERER_STYLE, "meta", "level").requireKt<String>() }
    val rarityFormat by reloadable { root.node(RENDERER_STYLE, "meta", "rarity").requireKt<String>() }
    val elementFormat by reloadable { getListFormat(root.node(RENDERER_STYLE, "meta", "element")) }
    val kizamiFormat by reloadable { getListFormat(root.node(RENDERER_STYLE, "meta", "kizami")) }
    val skinFormat by reloadable { root.node(RENDERER_STYLE, "meta", "skin").requireKt<String>() }
    val skinOwnerFormat by reloadable { root.node(RENDERER_STYLE, "meta", "skin_owner").requireKt<String>() }

    private fun getListFormat(node: NekoConfigurationNode): MetaStylizer.ListFormat {
        return MetaStylizer.ListFormat(
            merged = node.node("merged").requireKt<String>(),
            single = node.node("single").requireKt<String>(),
            separator = node.node("separator").requireKt<String>()
        )
    }
    //</editor-fold>

    //<editor-fold desc="renderer_style.operation">
    val operationFormat by reloadable {
        val operationNode = root.node(RENDERER_STYLE, "operation")
        AttributeModifier.Operation.entries.associateWith { operation ->
            operationNode.node(operation.key).requireKt<String>()
        }
    }
    //</editor-fold>

    //<editor-fold desc="renderer_style.attribute">
    val attributeFormats by reloadable {
        val attributeNode = root.node(RENDERER_STYLE, "attribute")
        attributeNode.childrenMap()
            .mapKeys { (key, _) -> Key.key(key as String) }
            .mapValues { (_, node) -> node.requireKt<String>() }
    }
    //</editor-fold>

    //<editor-fold desc="renderer_order">
    private val _fixedLoreLines: MutableList<FixedLoreLine> = arrayListOf()

    val fixedLoreLines: List<FixedLoreLine>
        get() = _fixedLoreLines

    private val _loreLineIndexes: MutableMap<Key, Int> = hashMapOf()

    val loreLineIndexes: Map<Key, Int>
        get() = _loreLineIndexes

    private fun loadConfiguration() {
        _fixedLoreLines.clear()
        _loreLineIndexes.clear()

        val primaryList = root.node(RENDERER_ORDER).node("primary").requireKt<List<String>>()

        for ((rawIndex, rawKey) in primaryList.withIndex()) {
            val loreIndex: LoreIndex = when {
                rawKey.startsWith(RENDERER_FIXED_LORE_SYMBOL) -> {
                    CustomFixedLoreIndex(
                        rawIndex = rawIndex,
                    ).also {
                        _fixedLoreLines.add(FixedLoreLineImpl(it.computeFullKeys().first(), listOf(rawKey.substringAfter(RENDERER_FIXED_LORE_SYMBOL).mini)))
                    }
                }

                rawKey.length > 1 && rawKey.startsWith(RENDERER_CONDITION_LORE_SYMBOL) -> {
                    TODO("实现按条件插入的空行")
                }

                rawKey == RENDERER_CONDITION_LORE_SYMBOL -> {
                    EmptyFixedLoreIndex(
                        rawIndex = rawIndex,
                    ).also {
                        _fixedLoreLines.add(FixedLoreLineImpl(it.computeFullKeys().first(), listOf(Component.empty())))
                    }
                }

                rawKey.startsWith("meta:") -> {
                    MetaLoreIndex(
                        rawKey = RawKey.key(rawKey),
                        rawIndex = rawIndex
                    )
                }

                rawKey.startsWith("attribute:") -> {
                    AttributeLoreIndex(
                        rawKey = RawKey.key(rawKey),
                        rawIndex = rawIndex
                    )
                }

                rawKey.startsWith("ability:") -> {
                    AbilityLoreIndex(
                        rawKey = RawKey.key(rawKey),
                        rawIndex = rawIndex
                    )
                }

                else -> {
                    throw IllegalArgumentException("Unknown key '$rawKey' while loading $RENDERER_CONFIG_FILE")
                }
            }

            val fullKeys = loreIndex.computeFullKeys()

            for ((fullIndex, fullKey) in fullKeys.withIndex()) {
                // 有添加失败 (例如不该重复的内容出现重复的了) 的情况就 throw
                val notContains = _loreLineIndexes.putIfAbsent(fullKey, rawIndex + fullIndex /* 从0开始 */) == null
                require(notContains) { "Key $fullKey has already been added to indexes. Please remove the duplicates" }
            }
        }
    }
    //</editor-fold>

    override fun onReload() {
        loadConfiguration()
    }
}