package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.NekoNamespaces
import cc.mewcraft.wakame.Reloadable
import cc.mewcraft.wakame.attribute.base.AttributeModifier
import cc.mewcraft.wakame.attribute.base.Attributes
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.reloadable
import cc.mewcraft.wakame.util.NekoConfigurationLoader
import cc.mewcraft.wakame.util.NekoConfigurationNode
import cc.mewcraft.wakame.util.requireKt
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.spongepowered.configurate.CommentedConfigurationNode

internal class RendererConfiguration(
    loader: NekoConfigurationLoader,
    private val miniMessage: MiniMessage,
) : Initializable, Reloadable {
    companion object {
        private const val RENDERER_ORDER = "renderer_layout"
        private const val RENDERER_STYLE = "renderer_style"

        private const val RENDERER_FIXED_LORE_SYMBOL = '^'
        private const val RENDERER_CONDITION_LORE_SYMBOL = "/"
    }

    private val root: CommentedConfigurationNode by reloadable { loader.load() }

    //<editor-fold desc="renderer_style.meta">
    val nameFormat: String by reloadable { root.node(RENDERER_STYLE, "meta", "name").requireKt<String>() }
    val loreFormat: MetaStylizerImpl.LoreFormatImpl by reloadable {
        with(root.node(RENDERER_STYLE, "meta", "lore")) {
            MetaStylizerImpl.LoreFormatImpl(
                line = node("line").requireKt<String>(),
                header = node("header").requireKt<List<String>>().takeIf(List<String>::isNotEmpty),
                bottom = node("bottom").requireKt<List<String>>().takeIf(List<String>::isNotEmpty)
            )
        }
    }
    val levelFormat: String by reloadable { root.node(RENDERER_STYLE, "meta", "level").requireKt<String>() }
    val rarityFormat: String by reloadable { root.node(RENDERER_STYLE, "meta", "rarity").requireKt<String>() }
    val elementFormat: MetaStylizer.ListFormat by reloadable { getListFormat(root.node(RENDERER_STYLE, "meta", "element")) }
    val kizamiFormat: MetaStylizer.ListFormat by reloadable { getListFormat(root.node(RENDERER_STYLE, "meta", "kizami")) }
    val skinFormat: String by reloadable { root.node(RENDERER_STYLE, "meta", "skin").requireKt<String>() }
    val skinOwnerFormat: String by reloadable { root.node(RENDERER_STYLE, "meta", "skin_owner").requireKt<String>() }

    private fun getListFormat(node: NekoConfigurationNode): MetaStylizer.ListFormat {
        return MetaStylizerImpl.ListFormatImpl(
            merged = node.node("merged").requireKt<String>(),
            single = node.node("single").requireKt<String>(),
            separator = node.node("separator").requireKt<String>()
        )
    }
    //</editor-fold>

    //<editor-fold desc="renderer_style.attribute">
    val emptyAttributeFormat: List<String> by reloadable {
        root.node(RENDERER_STYLE, "attribute", "empty").requireKt<List<String>>()
    }
    val operationFormat: Map<AttributeModifier.Operation, String> by reloadable {
        AttributeModifier.Operation.entries.associateWith { operation ->
            root.node(RENDERER_STYLE, "attribute", "operation").node(operation.key).requireKt<String>()
        }
    }
    val attributeFormats: Map<Key, String> by reloadable {
        root.node(RENDERER_STYLE, "attribute", "value").childrenMap()
            .mapKeys { (key, _) -> Key.key(NekoNamespaces.ATTRIBUTE, key as String) }
            .filter { (key, _) -> key != Attributes.ATTACK_SPEED_LEVEL.key() } // attack_speed_level is handled on its own
            .mapValues { (_, node) -> node.requireKt<String>() }
    }
    val attackSpeedFormat: AttributeStylizer.AttackSpeedFormat by reloadable {
        AttributeStylizerImpl.AttackSpeedFormatImpl(
            merged = root.node(RENDERER_STYLE, "attribute", "value", "attack_speed_level", "merged").requireKt<String>(),
            levels = root.node(RENDERER_STYLE, "attribute", "value", "attack_speed_level", "levels").childrenMap()
                .map { (key, node) -> (key as String).toInt() to node.requireKt<String>() }
                .toMap()
        )
    }
    //</editor-fold>

    //<editor-fold desc="renderer_style.ability">
    val emptyAbilityFormat: List<String> by reloadable {
        root.node(RENDERER_STYLE, "ability", "empty").requireKt<List<String>>()
    }
    val commonAbilityFormat: Unit by reloadable {
        // TODO
    }
    val abilityFormats: Unit by reloadable {
        // TODO
    }
    //</editor-fold>

    //<editor-fold desc="renderer_order">
    private val _loreMetaLookup: LinkedHashMap<FullKey, LoreMeta> = linkedMapOf()
    val loreMetaLookup: LinkedHashMap<FullKey, LoreMeta> get() = _loreMetaLookup

    private val _fullIndexLookup: MutableMap<FullKey, FullIndex> = mutableMapOf()
    val fullIndexLookup: Map<FullKey, FullIndex> get() = _fullIndexLookup

    private val _fixedLoreLines: MutableCollection<FixedLoreLine> = mutableListOf()
    val fixedLoreLines: Collection<FixedLoreLine> get() = _fixedLoreLines

    private fun loadConfiguration() {
        _fixedLoreLines.clear()
        _fullIndexLookup.clear()
        _loreMetaLookup.clear()

        val primaryIndex = root.node(RENDERER_ORDER).node("primary").requireKt<List<String>>()
        val operationIndex = root.node(RENDERER_ORDER).node("operation").requireKt<List<String>>()
        val elementIndex = root.node(RENDERER_ORDER).node("element").requireKt<List<String>>()

        for ((rawIndex, rawKey) in primaryIndex.withIndex()) {
            val loreIndex = getLoreIndex(rawKey, rawIndex, AttributeLoreMeta.Rule(operationIndex, elementIndex))
            val fullKeys = loreIndex.computeFullKeys()

            for ((localIndex, fullKey) in fullKeys.withIndex()) {
                val absent = _loreMetaLookup.putIfAbsent(fullKey, loreIndex) == null
                val newIndex = localIndex + rawIndex
                _fullIndexLookup[fullKey] = newIndex
                require(absent) { "Key $fullKey has already been added to indexes, please remove the duplicates" }
            }
        }
    }

    private fun getLoreIndex(rawKey: String, rawIndex: Int, rule: AttributeLoreMeta.Rule, canBeEmptyLine: Boolean = false): LoreMeta {
        return when {
            rawKey.startsWith(RENDERER_CONDITION_LORE_SYMBOL) && rawKey.length > 1 -> {
                val sourceKey = rawKey.substringAfter(RENDERER_CONDITION_LORE_SYMBOL)
                getLoreIndex(sourceKey, rawIndex, rule, true)
            }

            rawKey == RENDERER_CONDITION_LORE_SYMBOL -> {
                val emptyFixedLoreIndex = EmptyFixedLoreMeta(rawIndex)
                _fixedLoreLines.add(FixedLoreLineImpl(emptyFixedLoreIndex.computeFullKeys().first(), listOf(Component.empty())))
                emptyFixedLoreIndex
            }

            rawKey.startsWith(RENDERER_FIXED_LORE_SYMBOL) -> {
                val customFixedLoreIndex = CustomFixedLoreMeta(rawIndex)
                _fixedLoreLines.add(FixedLoreLineImpl(customFixedLoreIndex.computeFullKeys().first(), listOf(miniMessage.deserialize(rawKey.substringAfter(RENDERER_FIXED_LORE_SYMBOL)))))
                customFixedLoreIndex
            }

            rawKey.startsWith(NekoNamespaces.ABILITY + ":") -> {
                AbilityLoreMeta(RawKey.key(rawKey), rawIndex, canBeEmptyLine)
            }

            rawKey.startsWith(NekoNamespaces.ATTRIBUTE + ":") -> {
                AttributeLoreMeta(RawKey.key(rawKey), rawIndex, rule, canBeEmptyLine)
            }

            rawKey.startsWith(NekoNamespaces.META + ":") -> {
                MetaLoreMeta(RawKey.key(rawKey), rawIndex, canBeEmptyLine)
            }

            else -> {
                throw IllegalArgumentException("Unknown key '$rawKey' while loading $RENDERER_CONFIG_FILE")
            }
        }
    }
    //</editor-fold>

    override fun onPostWorld() {
        loadConfiguration()
    }

    override fun onReload() {
        loadConfiguration()
    }
}