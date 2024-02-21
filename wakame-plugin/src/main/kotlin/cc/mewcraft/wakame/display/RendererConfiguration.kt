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
        private const val RENDERER_ORDER = "renderer_layout"
        private const val RENDERER_STYLE = "renderer_style"

        // private const val RENDERER_FIXED_LORE_SYMBOL = '^'
        // private const val RENDERER_CONDITION_LORE_SYMBOL = "/"
        private const val RENDERER_LAYOUT_LINE_PATTERN = "\\((.+?)\\)(.*)"
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
            .filter { (key, _) -> key != Attributes.ATTACK_SPEED_LEVEL.key() } // attack_speed_level is handled separately
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

    //<editor-fold desc="renderer_layout">
    // val loreMetaLookup: LinkedHashMap<FullKey, LoreMeta> get() = _loreMetaLookup
    val fullIndexLookup: Map<FullKey, FullIndex> get() = _fullIndexLookup
    val fixedLoreLines: Collection<FixedLoreLine> get() = _fixedLoreLines

    // private val _loreMetaLookup: LinkedHashMap<FullKey, LoreMeta> = linkedMapOf()
    private val _fullIndexLookup: MutableMap<FullKey, FullIndex> = mutableMapOf()
    private val _fixedLoreLines: MutableCollection<FixedLoreLine> = mutableListOf()

    private fun loadConfiguration() {
        _fixedLoreLines.clear()
        _fullIndexLookup.clear()
        // _loreMetaLookup.clear()

        val primaryIndex = root.node(RENDERER_ORDER).node("primary").requireKt<List<String>>()
        val attDerivation = AttributeLoreMeta.Derivation(
            operationIndex = root.node(RENDERER_ORDER).node("operation").requireKt<List<String>>(),
            elementIndex = root.node(RENDERER_ORDER).node("element").requireKt<List<String>>()
        )

        val pattern = RENDERER_LAYOUT_LINE_PATTERN.toPattern()

        /**
         * Creates a dynamic lore meta from the config line.
         *
         * @param rawIndex the raw index in the config, without any modification
         * @param rawLine the raw line in the config, without any modification
         * @param default see the specification of [DynamicLoreMeta.default]
         * @return a new instance
         */
        fun createDynamicLoreMeta(rawIndex: Int, rawLine: String, default: List<Component>?): DynamicLoreMeta {
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
                            CustomFixedLoreMeta(rawIndex, companionNamespace, listOf(miniMessage.deserialize(customFixedText)))
                        }
                    }

                    // 解析为 '(default:...)...'
                    "default" -> {
                        val defaultText = queue.popOr("Unknown syntax for 'default'. Correct syntax: '(default:_text_|empty)_key_'").let {
                            if (it.isBlank() || it == "empty") {
                                listOf(Component.empty())
                            } else {
                                listOf(miniMessage.deserialize(it))
                            }
                        }
                        loreMeta = createDynamicLoreMeta(rawIndex, rawLine, defaultText)
                    }

                    else -> error("Unknown option '$params' while loading config $RENDERER_CONFIG_FILE")
                }
            } else {
                // 无参数 '...'
                loreMeta = createDynamicLoreMeta(rawIndex, rawLine, null)
            }
            return loreMeta
        }

        for ((rawIndex, rawLine) in primaryIndex.withIndex()) {
            val loreMeta = createLoreMeta(rawIndex, rawLine)

            // allow for global lookup for the indexes
            loreMeta.fullIndexes.forEach { (fullKey, fullIndex) ->
                val absent = (_fullIndexLookup.putIfAbsent(fullKey, fullIndex) == null)
                require(absent) { "Key $fullKey has already been added to indexes" }
            }

            // allow for global access to all the fixed lore lines
            if (loreMeta is FixedLoreMeta) {
                _fixedLoreLines += FixedLoreLineImpl(loreMeta.fullKeys.first(), loreMeta.components)
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