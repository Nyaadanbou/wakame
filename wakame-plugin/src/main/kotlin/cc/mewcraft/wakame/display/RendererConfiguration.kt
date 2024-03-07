package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.NekoNamespaces
import cc.mewcraft.wakame.argument.StringArgumentQueue
import cc.mewcraft.wakame.attribute.AttributeModifier
import cc.mewcraft.wakame.attribute.Attributes
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.registry.AttributeRegistry
import cc.mewcraft.wakame.reloadable
import cc.mewcraft.wakame.util.NekoConfigurationLoader
import cc.mewcraft.wakame.util.NekoConfigurationNode
import cc.mewcraft.wakame.util.requireKt
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.spongepowered.configurate.CommentedConfigurationNode
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

internal class RendererConfiguration(
    loader: NekoConfigurationLoader,
) : Initializable, KoinComponent {
    companion object {
        private const val RENDERER_LAYOUT_LINE_PATTERN = "\\((.+?)\\)(.*)"
        private const val RENDERER_LAYOUT_NODE = "renderer_layout"
        private const val RENDERER_STYLE_NODE = "renderer_style"
    }

    private val mm: MiniMessage by inject(mode = LazyThreadSafetyMode.NONE)
    private val root: CommentedConfigurationNode by reloadable { loader.load() }

    //<editor-fold desc="renderer_style.meta">
    /**
     * 名字的渲染格式。
     */
    val nameFormat: String by reloadable { root.node(RENDERER_STYLE_NODE, "meta", "name").requireKt<String>() }

    /**
     * 描述的渲染格式。
     */
    val loreFormat: ItemMetaStylizer.LoreFormat by reloadable {
        with(root.node(RENDERER_STYLE_NODE, "meta", "lore")) {
            ItemMetaStylizerImpl.LoreFormatImpl(
                line = node("line").requireKt<String>(),
                header = node("header").requireKt<List<String>>().takeIf(List<String>::isNotEmpty),
                bottom = node("bottom").requireKt<List<String>>().takeIf(List<String>::isNotEmpty)
            )
        }
    }

    /**
     * 等级的渲染格式。
     */
    val levelFormat: String by reloadable { root.node(RENDERER_STYLE_NODE, "meta", "level").requireKt<String>() }

    /**
     * 稀有度的渲染格式。
     */
    val rarityFormat: String by reloadable { root.node(RENDERER_STYLE_NODE, "meta", "rarity").requireKt<String>() }

    /**
     * 元素的渲染格式。
     */
    val elementFormat: ItemMetaStylizer.ListFormat by reloadable { getListFormat(root.node(RENDERER_STYLE_NODE, "meta", "element")) }

    /**
     * 铭刻的渲染格式。
     */
    val kizamiFormat: ItemMetaStylizer.ListFormat by reloadable { getListFormat(root.node(RENDERER_STYLE_NODE, "meta", "kizami")) }

    /**
     * 保养度的渲染格式。
     */
    val durabilityFormat: String by reloadable { root.node(RENDERER_STYLE_NODE, "meta", "durability").requireKt<String>() }

    /**
     * 皮肤的渲染格式。
     */
    val skinFormat: String by reloadable { root.node(RENDERER_STYLE_NODE, "meta", "skin").requireKt<String>() }

    /**
     * 皮肤所有者的渲染格式。
     */
    val skinOwnerFormat: String by reloadable { root.node(RENDERER_STYLE_NODE, "meta", "skin_owner").requireKt<String>() }

    private fun getListFormat(node: NekoConfigurationNode): ItemMetaStylizer.ListFormat {
        return ItemMetaStylizerImpl.ListFormatImpl(
            merged = node.node("merged").requireKt<String>(),
            single = node.node("single").requireKt<String>(),
            separator = node.node("separator").requireKt<String>()
        )
    }
    //</editor-fold>

    //<editor-fold desc="renderer_style.attribute">
    /**
     * 空词条栏（属性）的渲染格式。
     */
    val emptyAttributeText: List<String> by reloadable {
        root.node(RENDERER_STYLE_NODE, "attribute", "empty").requireKt<List<String>>()
    }

    /**
     * 所有属性的渲染格式。
     *
     * ## 映射说明
     * - `map key` 跟 [AttributeRegistry] 里的一致，不是 [FullKey]
     * - `map value` 就是配置文件里对应的字符串值，无需做任何处理
     *
     * **注意该映射不包含 [Attributes.ATTACK_SPEED_LEVEL]**
     */
    val attributeFormats: Map<Key, String> by reloadable {
        root.node(RENDERER_STYLE_NODE, "attribute", "value").childrenMap()
            .mapKeys { (k, _) -> Key.key(NekoNamespaces.ATTRIBUTE, k as String) }
            .filter { (k, _) -> k != Attributes.ATTACK_SPEED_LEVEL.key() }
            .mapValues { (_, v) -> v.requireKt<String>() }
    }

    /**
     * 攻击速度的渲染格式。
     */
    val attackSpeedFormat: AttributeStylizer.AttackSpeedFormat by reloadable {
        val node = root.node(RENDERER_STYLE_NODE, "attribute", "value", "attack_speed_level")
        AttributeStylizerImpl.AttackSpeedFormatImpl(
            merged = node.node("merged").requireKt<String>(),
            levels = node.node("levels").childrenMap()
                .map { (key, node) -> (key as String).toInt() to node.requireKt<String>() }
                .toMap()
        )
    }

    /**
     * 运算模式的渲染格式。
     */
    val operationFormats: Map<AttributeModifier.Operation, String> by reloadable {
        AttributeModifier.Operation.entries.associateWith { operation ->
            root.node(RENDERER_STYLE_NODE, "attribute", "operation", operation.key).requireKt<String>()
        }
    }
    //</editor-fold>

    //<editor-fold desc="renderer_style.ability">
    /**
     * 空词条栏的渲染格式（技能）。
     */
    val emptyAbilityText: List<String> by reloadable {
        root.node(RENDERER_STYLE_NODE, "ability", "empty").requireKt<List<String>>()
    }

    /**
     * 所有技能共用的渲染格式。
     */
    val commonAbilityFormat: Unit by reloadable {
        // TODO
    }

    /**
     * 个别技能独有的渲染格式。
     */
    val abilityFormats: Unit by reloadable {
        // TODO
    }
    //</editor-fold>

    //<editor-fold desc="renderer_layout">
    /**
     * 所有的 [RawKey]，用于判断内容是否需要渲染。
     *
     * 如果一个 [RawKey] 不在该集合里，则说明不应该渲染。
     */
    val rawKeys: Set<RawKey> get() = _rawKeys

    /**
     * 用于查询指定内容的 [LoreMeta]。
     */
    val loreMetaLookup: Map<FullKey, LoreMeta> get() = _loreMetaLookup

    /**
     * 用于查询指定内容的 [FullIndex]。
     */
    val loreIndexLookup: Map<FullKey, FullIndex> get() = _loreIndexLookup

    /**
     * 始终要渲染的内容。这些内容的文本在物品中始终不变。
     */
    val fixedLoreLines: Collection<LoreLine> get() = _fixedLoreLines

    /**
     * 带有默认值的内容。当源数据不存在时将采用这里的默认值。
     */
    val defaultLoreLines: Collection<LoreLine> get() = _defaultLoreLines

    private val _rawKeys: MutableSet<RawKey> = ConcurrentHashMap.newKeySet()
    private val _loreMetaLookup: MutableMap<FullKey, LoreMeta> = ConcurrentHashMap()
    private val _loreIndexLookup: MutableMap<FullKey, FullIndex> = ConcurrentHashMap()
    private val _fixedLoreLines: MutableCollection<LoreLine> = CopyOnWriteArrayList()
    private val _defaultLoreLines: MutableCollection<LoreLine> = CopyOnWriteArrayList()

    private fun loadLayout() {
        _rawKeys.clear()
        _loreMetaLookup.clear()
        _loreIndexLookup.clear()
        _fixedLoreLines.clear()
        _defaultLoreLines.clear()

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

                rawLine.startsWith(NekoNamespaces.ITEM_META + ":") -> {
                    ret = MetaLoreMeta(RawKey.key(rawLine), rawIndex, default)
                }

                else -> {
                    throw IllegalArgumentException("Unknown key '$rawLine' while loading config $RENDERER_CONFIG_FILE")
                }
            }
            return ret
        }

        fun String.spiltAndDeserialize(): List<Component> =
            this.split("\\r").map(mm::deserialize)  // 以 '\r' 为分隔符，将文本分割为多行

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

        // *accumulative* offset of the full index
        var accIndexOffset = 0

        // loop through each primary line and initialize LoreMeta
        for ((rawIndex, rawLine) in primaryLines.withIndex()) {
            val loreMeta = createLoreMeta(rawIndex, rawLine)

            // populate the raw keys
            _rawKeys += loreMeta.rawKey

            val fullIndexes = loreMeta.fullIndexes(accIndexOffset).onEach { (fullKey, fullIndex) ->
                // populate the index lookup
                val absent = (_loreIndexLookup.putIfAbsent(fullKey, fullIndex) == null)
                require(absent) { "Key $fullKey has already been added to indexes" }

                // populate the meta lookup
                _loreMetaLookup[fullKey] = loreMeta
            }
            // Accumulate the number of derived lines.
            // Minus one to neglect non-derived lines.
            accIndexOffset += fullIndexes.size - 1

            // populate the fixed lore lines
            if (loreMeta is FixedLoreMeta) {
                _fixedLoreLines += FixedLineImpl(loreMeta.fullKeys.first(), loreMeta.components)
            }

            // populate the default lore lines
            if (loreMeta is DynamicLoreMeta) {
                val default = loreMeta.default ?: continue

                // if the lore meta has a default value, add it to the default lore lines
                _defaultLoreLines += loreMeta.fullKeys.map { key ->
                    when (loreMeta) {
                        is AbilityLoreMeta -> AbilityLineImpl(key, default)
                        is AttributeLoreMeta -> AttributeLineImpl(key, default)
                        is MetaLoreMeta -> ItemMetaLineImpl(key, default)
                    }
                }
            }
        }
    }
    //</editor-fold>

    override fun onPostWorld() {
        loadLayout()
    }

    override fun onReload() {
        loadLayout()
    }
}