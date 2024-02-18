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
    private val logger: Logger
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

    val operationFormat by reloadable {
        val operationNode = root.node(RENDERER_STYLE, "operation")
        AttributeModifier.Operation.entries.associateWith { operation ->
            operationNode.node(operation.key).requireKt<String>()
        }
    }

    val attributeFormats by reloadable {
        val attributeNode = root.node(RENDERER_STYLE, "attribute")
        attributeNode.childrenMap()
            .mapKeys { (key, _) -> Key.key(key as String) }
            .mapValues { (_, node) -> node.requireKt<String>() }
    }

    private val _fixedLoreLines: MutableList<FixedLoreLine> = arrayListOf()

    val fixedLoreLines: List<FixedLoreLine>
        get() = _fixedLoreLines

    private val _loreLineIndexes: MutableMap<Key, Int> = hashMapOf()

    val loreLineIndexes: Map<Key, Int>
        get() = _loreLineIndexes

    private fun loadConfiguration() {
        _fixedLoreLines.clear()
        _loreLineIndexes.clear()

        val loreOrderNode = root.node(RENDERER_ORDER)

        val primaryList = loreOrderNode.node("primary").requireKt<List<String>>()

        for ((index, key) in primaryList.withIndex()) {
            val loreIndex: LoreIndex = when {
                key.startsWith(RENDERER_FIXED_LORE_SYMBOL) -> {
                    _fixedLoreLines.add(FixedLoreLineImpl(RawKey.key(index.toString()), listOf(key.substringAfter(RENDERER_FIXED_LORE_SYMBOL).mini)))
                    FixedLoreIndex(
                        rawIndex = index,
                        isEmptyLine = false
                    )
                }

                key == RENDERER_CONDITION_LORE_SYMBOL -> {
                    _fixedLoreLines.add(FixedLoreLineImpl(RawKey.key(index.toString()), listOf(Component.empty())))
                    FixedLoreIndex(
                        rawIndex = index,
                        isEmptyLine = true
                    )
                }

                key.startsWith("meta:") -> {
                    MetaLoreIndex(
                        rawKey = RawKey.key(key),
                        rawIndex = index
                    )
                }

                key.startsWith("attribute:") -> {
                    AttributeLoreIndex(
                        rawKey = RawKey.key(key),
                        rawIndex = index
                    )
                }

                key.startsWith("ability:") -> {
                    AbilityLoreIndex(
                        rawKey = RawKey.key(key),
                        rawIndex = index
                    )
                }

                else -> {
                    logger.error("在加载 Renderer 配置时出现了错误", IllegalArgumentException("Unknown key: $key"))
                    continue
                }
            }

            val fullKeyList = loreIndex.computeFullKeys()

            for ((fullKeyIndex, newKey) in fullKeyList.withIndex()){
                // 有添加失败 (例如不该重复的内容出现重复的了) 的情况就 throw
                _loreLineIndexes.putIfAbsent(newKey, index + fullKeyIndex  /* 从0开始 */)
                    ?.let { throw IllegalStateException("Key $newKey has already been added to orders, maybe your config is error?") }
            }
        }
    }

    override fun onReload() {
        loadConfiguration()
    }
}