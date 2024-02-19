package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.Reloadable
import cc.mewcraft.wakame.attribute.base.AttributeModifier
import cc.mewcraft.wakame.attribute.base.Attributes
import cc.mewcraft.wakame.reloadable
import cc.mewcraft.wakame.util.NekoConfigurationLoader
import cc.mewcraft.wakame.util.NekoConfigurationNode
import cc.mewcraft.wakame.util.requireKt
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage

internal class RendererConfiguration(
    loader: NekoConfigurationLoader,
    private val miniMessage: MiniMessage
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
        AttributeModifier.Operation.entries.associateWith { operation ->
            root.node(RENDERER_STYLE, "operation").node(operation.key).requireKt<String>()
        }
    }
    //</editor-fold>

    //<editor-fold desc="renderer_style.attribute">
    val attributeFormats by reloadable {
        root.node(RENDERER_STYLE, "attribute").childrenMap()
            .mapKeys { (key, _) -> Key.key("attribute", key as String) }
            .filter { (key, _) -> key != Attributes.ATTACK_SPEED_LEVEL.key() } // attack_speed_level is handled on its own
            .mapValues { (_, node) -> node.requireKt<String>() }
    }
    val attackSpeedFormat by reloadable {
        AttributeStylizer.AttackSpeedFormat(
            merged = root.node(RENDERER_STYLE, "attribute", "attack_speed_level", "merged").requireKt<String>(),
            levels = root.node(RENDERER_STYLE, "attribute", "attack_speed_level", "levels").childrenMap()
                .map { (key, node) -> (key as String).toInt() to node.requireKt<String>() }
                .toMap()
        )
    }
    //</editor-fold>

    //<editor-fold desc="renderer_style.cell">
    val emptyCellFormat by reloadable { root.node(RENDERER_STYLE, "cell").node("empty").requireKt<String>() }
    //</editor-fold>

    //<editor-fold desc="renderer_order">
    private val _allLoreIndexes: LinkedHashMap<FullKey, LoreIndex> = linkedMapOf()
    val allLoreIndexes: LinkedHashMap<FullKey, LoreIndex>
        get() = _allLoreIndexes

    private val _fixedLoreLines: MutableCollection<FixedLoreLine> = mutableListOf()
    val fixedLoreLines: Collection<FixedLoreLine>
        get() = _fixedLoreLines

    private val _loreLineIndexes: MutableMap<FullKey, Int> = mutableMapOf()
    val loreLineIndexes: Map<FullKey, Int /* FullIndex */>
        get() = _loreLineIndexes

    private fun loadConfiguration() {
        _fixedLoreLines.clear()
        _loreLineIndexes.clear()
        _allLoreIndexes.clear()

        val primaryList = root.node(RENDERER_ORDER).node("primary").requireKt<List<String>>()
        val operationIndex = root.node(RENDERER_ORDER).node("operation").requireKt<List<String>>()
        val elementIndex = root.node(RENDERER_ORDER).node("element").requireKt<List<String>>()

        for ((rawIndex, rawKey) in primaryList.withIndex()) {
            val loreIndex: LoreIndex = getLoreIndex(rawKey, rawIndex, AttributeLoreIndex.Rule(operationIndex, elementIndex))
            val fullKeys = loreIndex.computeFullKeys()

            for ((index, fullKey) in fullKeys.withIndex()) {
                val notContains = _allLoreIndexes.putIfAbsent(fullKey, loreIndex) == null
                val newIndex = index + rawIndex
                _loreLineIndexes[fullKey] = newIndex
                require(notContains) { "Key $fullKey has already been added to indexes. Please remove the duplicates" }
            }
        }
    }
    //</editor-fold>

    private fun getLoreIndex(rawKey: String, rawIndex: Int, rule: AttributeLoreIndex.Rule, currentDepth: Int = 0): LoreIndex {
        if (currentDepth > 3) {
            throw IllegalArgumentException("Too deep recursion while loading $RENDERER_CONFIG_FILE, is your configuration file correct?")
        }

        return when {
            rawKey.startsWith(RENDERER_FIXED_LORE_SYMBOL) -> {
                val fixedLoreIndex = CustomFixedLoreIndex(rawIndex)
                _fixedLoreLines.add(FixedLoreLineImpl(fixedLoreIndex.computeFullKeys().first(), listOf(miniMessage.deserialize(rawKey.substringAfter(RENDERER_FIXED_LORE_SYMBOL)))))
                fixedLoreIndex
            }

            rawKey.length > 1 && rawKey.startsWith(RENDERER_CONDITION_LORE_SYMBOL) -> {
                val sourceKey = rawKey.substringAfter(RENDERER_CONDITION_LORE_SYMBOL)
                FallbackLoreIndex(getLoreIndex(sourceKey, rawIndex, rule, currentDepth + 1))
            }

            rawKey == RENDERER_CONDITION_LORE_SYMBOL -> {
                val fixedLoreIndex = EmptyFixedLoreIndex(rawIndex)
                _fixedLoreLines.add(FixedLoreLineImpl(fixedLoreIndex.computeFullKeys().first(), listOf(Component.empty())))
                fixedLoreIndex
            }

            rawKey.startsWith("ability:") -> {
                AbilityLoreIndex(RawKey.key(rawKey), rawIndex)
            }

            rawKey.startsWith("attribute:") -> {
                AttributeLoreIndex(RawKey.key(rawKey), rawIndex, rule)
            }

            rawKey.startsWith("meta:") -> {
                MetaLoreIndex(RawKey.key(rawKey), rawIndex)
            }

            else -> {
                throw IllegalArgumentException("Unknown key '$rawKey' while loading $RENDERER_CONFIG_FILE")
            }
        }
    }

    override fun onReload() {
        loadConfiguration()
    }
}