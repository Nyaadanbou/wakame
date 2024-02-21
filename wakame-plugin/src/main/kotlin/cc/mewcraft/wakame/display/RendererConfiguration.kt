package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.NekoNamespaces
import cc.mewcraft.wakame.Reloadable
import cc.mewcraft.wakame.argument.StringArgumentQueue
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
        private const val RENDERER_LAYOUT_LINE_PATTERN = "\\((.+?)\\)(.*)"
        private const val RENDERER_LAYOUT_NODE = "renderer_layout"
        private const val RENDERER_STYLE_NODE = "renderer_style"
    }

    private val root: CommentedConfigurationNode by reloadable { loader.load() }

    //<editor-fold desc="renderer_style.meta">
    val nameFormat: String by reloadable { root.node(RENDERER_STYLE_NODE, "meta", "name").requireKt<String>() }
    val loreFormat: MetaStylizerImpl.LoreFormatImpl by reloadable {
        with(root.node(RENDERER_STYLE_NODE, "meta", "lore")) {
            MetaStylizerImpl.LoreFormatImpl(
                line = node("line").requireKt<String>(),
                header = node("header").requireKt<List<String>>().takeIf(List<String>::isNotEmpty),
                bottom = node("bottom").requireKt<List<String>>().takeIf(List<String>::isNotEmpty)
            )
        }
    }
    val levelFormat: String by reloadable { root.node(RENDERER_STYLE_NODE, "meta", "level").requireKt<String>() }
    val rarityFormat: String by reloadable { root.node(RENDERER_STYLE_NODE, "meta", "rarity").requireKt<String>() }
    val elementFormat: MetaStylizer.ListFormat by reloadable { getListFormat(root.node(RENDERER_STYLE_NODE, "meta", "element")) }
    val kizamiFormat: MetaStylizer.ListFormat by reloadable { getListFormat(root.node(RENDERER_STYLE_NODE, "meta", "kizami")) }
    val skinFormat: String by reloadable { root.node(RENDERER_STYLE_NODE, "meta", "skin").requireKt<String>() }
    val skinOwnerFormat: String by reloadable { root.node(RENDERER_STYLE_NODE, "meta", "skin_owner").requireKt<String>() }

    private fun getListFormat(node: NekoConfigurationNode): MetaStylizer.ListFormat {
        return MetaStylizerImpl.ListFormatImpl(
            merged = node.node("merged").requireKt<String>(),
            single = node.node("single").requireKt<String>(),
            separator = node.node("separator").requireKt<String>()
        )
    }
    //</editor-fold>

    //<editor-fold desc="renderer_style.attribute">
    val emptyAttributeText: List<String> by reloadable {
        root.node(RENDERER_STYLE_NODE, "attribute", "empty").requireKt<List<String>>()
    }
    val attributeFormats: Map<Key, String> by reloadable {
        root.node(RENDERER_STYLE_NODE, "attribute", "value").childrenMap()
            .mapKeys { (k, _) -> Key.key(NekoNamespaces.ATTRIBUTE, k as String) }
            .filter { (k, _) -> k != Attributes.ATTACK_SPEED_LEVEL.key() } // attack_speed_level is handled separately
            .mapValues { (_, v) -> v.requireKt<String>() }
    }
    val attackSpeedFormat: AttributeStylizer.AttackSpeedFormat by reloadable {
        val node = root.node(RENDERER_STYLE_NODE, "attribute", "value", "attack_speed_level")
        AttributeStylizerImpl.AttackSpeedFormatImpl(
            merged = node.node("merged").requireKt<String>(),
            levels = node.node("levels").childrenMap()
                .map { (key, node) -> (key as String).toInt() to node.requireKt<String>() }
                .toMap()
        )
    }
    val operationFormat: Map<AttributeModifier.Operation, String> by reloadable {
        AttributeModifier.Operation.entries.associateWith { operation ->
            root.node(RENDERER_STYLE_NODE, "attribute", "operation", operation.key).requireKt<String>()
        }
    }
    //</editor-fold>

    //<editor-fold desc="renderer_style.ability">
    val emptyAbilityText: List<String> by reloadable {
        root.node(RENDERER_STYLE_NODE, "ability", "empty").requireKt<List<String>>()
    }
    val commonAbilityFormat: Unit by reloadable {
        // TODO
    }
    val abilityFormats: Unit by reloadable {
        // TODO
    }
    //</editor-fold>

    //<editor-fold desc="renderer_layout">
    val loreMetaLookup: Map<FullKey, LoreMeta> get() = _loreMetaLookup
    val loreIndexLookup: Map<FullKey, FullIndex> get() = _loreIndexLookup
    val fixedLoreLines: Collection<LoreLine> get() = _fixedLoreLines
    val defaultLoreLines: Collection<LoreLine> get() = _defaultLoreLines

    private val _loreMetaLookup: MutableMap<FullKey, LoreMeta> = HashMap()
    private val _loreIndexLookup: MutableMap<FullKey, FullIndex> = HashMap()
    private val _fixedLoreLines: MutableCollection<LoreLine> = ArrayList()
    private val _defaultLoreLines: MutableCollection<LoreLine> = ArrayList()

    private fun loadConfiguration() {
        _loreIndexLookup.clear()
        _loreMetaLookup.clear()
        _fixedLoreLines.clear()

        val primaryLines = root.node(RENDERER_LAYOUT_NODE).node("primary").requireKt<List<String>>()
        val attDerivation = AttributeLoreMeta.Derivation(
            operationIndex = root.node(RENDERER_LAYOUT_NODE).node("operation").requireKt<List<String>>(),
            elementIndex = root.node(RENDERER_LAYOUT_NODE).node("element").requireKt<List<String>>()
        )

        val pattern = RENDERER_LAYOUT_LINE_PATTERN.toPattern()

        //<editor-fold desc="Helper functions to create LoreMeta">
        /**
         * Creates a lore meta from the config line.
         *
         * @param rawIndex the raw index in the config, without any modification
         * @param rawLine the raw line in the config, without any modification
         * @param default see the specification of [DynamicLoreMeta.default]
         * @return a new instance
         */
        fun createLoreMeta0(rawIndex: Int, rawLine: String, default: List<Component>?): LoreMeta {
            val ret: DynamicLoreMeta
            when {
                rawLine.startsWith(NekoNamespaces.ABILITY + ":") -> {
                    ret = AbilityLoreMeta(RawKey.key(rawLine), rawIndex, default)
                }

                rawLine.startsWith(NekoNamespaces.ATTRIBUTE + ":") -> {
                    ret = AttributeLoreMeta(RawKey.key(rawLine), rawIndex, default, attDerivation)
                }

                rawLine.startsWith(NekoNamespaces.META + ":") -> {
                    ret = MetaLoreMeta(RawKey.key(rawLine), rawIndex, default)
                }

                else -> {
                    throw IllegalArgumentException("Unknown key '$rawLine' while loading config $RENDERER_CONFIG_FILE")
                }
            }
            return ret
        }

        fun String.spiltAndDeserialize(): List<Component> =
            this.split("\\r").map(miniMessage::deserialize)  // 以 '\r' 为分隔符，将文本分割为多行

        /**
         * Creates a lore meta from the config line.
         *
         * @param rawIndex the raw index in the config, without any modification
         * @param rawLine the raw line in the config, without any modification
         * @return a new instance
         */
        fun createLoreMeta(rawIndex: Int, rawLine: String): LoreMeta {
            val loreMeta: LoreMeta
            val matcher = pattern.matcher(rawLine)
            if (matcher.matches()) {
                // 有参数 '(...)...'
                val params = matcher.group(1)
                val queue = StringArgumentQueue(params.split(':'))
                when (queue.pop()) {
                    // 解析为 '(fixed...)...'
                    "fixed" -> {
                        val companionNamespace = queue.peek() // nullable
                        val customFixedText = matcher.group(2)
                        loreMeta = if (customFixedText.isBlank()) {
                            // 解析为 '(fixed)无内容' 或 '(fixed:...)无内容'
                            EmptyFixedLoreMeta(rawIndex, companionNamespace)
                        } else {
                            // 解析为 '(fixed)有内容' 或 '(fixed:...)有内容'
                            val fixedTexts = customFixedText.spiltAndDeserialize()
                            CustomFixedLoreMeta(rawIndex, companionNamespace, fixedTexts)
                        }
                    }

                    // 解析为 '(default:...)...'
                    "default" -> {
                        val defaultText = queue.popOr("Unknown syntax for '(default...)' while load config $RENDERER_CONFIG_FILE. Correct syntax: '(default:_text_|empty)_key_'").let {
                            if (it.isBlank() || it == "empty") {
                                listOf(Component.empty())
                            } else {
                                it.spiltAndDeserialize()
                            }
                        }
                        loreMeta = createLoreMeta0(rawIndex, matcher.group(2), defaultText)
                    }

                    else -> error("Unknown option '$params' while loading config $RENDERER_CONFIG_FILE")
                }
            } else {
                // 无参数 '...'
                loreMeta = createLoreMeta0(rawIndex, rawLine, null)
            }
            return loreMeta
        }
        //</editor-fold>

        // loop through each primary line and initialize LoreMeta
        for ((rawIndex, rawLine) in primaryLines.withIndex()) {
            val loreMeta = createLoreMeta(rawIndex, rawLine)

            loreMeta.fullIndexes.forEach { (fullKey, fullIndex) ->
                // populate the index lookup
                val absent = (_loreIndexLookup.putIfAbsent(fullKey, fullIndex) == null)
                require(absent) { "Key $fullKey has already been added to indexes" }

                // populate the meta lookup
                _loreMetaLookup[fullKey] = loreMeta
            }

            // populate the fixed lore lines
            if (loreMeta is FixedLoreMeta) {
                _fixedLoreLines += FixedLoreLineImpl(loreMeta.fullKeys.first(), loreMeta.components)
            }

            // populate the default lore lines
            if (loreMeta is DynamicLoreMeta) {
                val default = loreMeta.default ?: continue

                // if the lore meta has a default value, add it to the default lore lines
                _defaultLoreLines += loreMeta.fullKeys.map { key ->
                    when (loreMeta) {
                        is AbilityLoreMeta -> AbilityLoreLineImpl(key, default)
                        is AttributeLoreMeta -> AttributeLoreLineImpl(key, default)
                        is MetaLoreMeta -> MetaLoreLineImpl(key, default)
                    }
                }
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