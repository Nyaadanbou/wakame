package cc.mewcraft.wakame.display

import cc.mewcraft.commons.provider.Provider
import cc.mewcraft.commons.provider.immutable.map
import cc.mewcraft.wakame.NekoNamespaces
import cc.mewcraft.wakame.argument.StringArgumentQueue
import cc.mewcraft.wakame.attribute.AttributeModifier
import cc.mewcraft.wakame.attribute.Attributes
import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.config.entry
import cc.mewcraft.wakame.config.node
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.registry.AttributeRegistry
import cc.mewcraft.wakame.reloadable
import cc.mewcraft.wakame.util.Key
import cc.mewcraft.wakame.util.krequire
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.spongepowered.configurate.ConfigurationNode
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

// TODO use ConfigProvider where possible

internal class RendererConfiguration(
    private val config: ConfigProvider,
) : Initializable, KoinComponent {
    companion object {
        private const val RENDERER_LAYOUT_LINE_PATTERN = "\\((.+?)\\)(.*)"
        private const val RENDERER_LAYOUT_NODE = "renderer_layout"
        private const val RENDERER_STYLE_NODE = "renderer_style"
    }

    private val mm: MiniMessage by inject(mode = LazyThreadSafetyMode.NONE)

    //<editor-fold desc="renderer_style.meta">
    /**
     * 名字的渲染格式。
     */
    val nameFormat: String by config.entry<String>(RENDERER_STYLE_NODE, "meta", "name")

    /**
     * 描述的渲染格式。
     */
    val loreFormat: ItemMetaStylizer.LoreFormat = with(config.node(RENDERER_STYLE_NODE, "meta", "lore")) {
        ItemMetaStylizerImpl.LoreFormatImpl(
            lineProvider = entry<String>("line"),
            headerProvider = entry<List<String>>("header").map { it.takeIf(List<String>::isNotEmpty) },
            bottomProvider = entry<List<String>>("bottom").map { it.takeIf(List<String>::isNotEmpty) }
        )
    }


    /**
     * 等级的渲染格式。
     */
    val levelFormat: String by config.entry<String>(RENDERER_STYLE_NODE, "meta", "level")

    /**
     * 稀有度的渲染格式。
     */
    val rarityFormat: String by config.entry<String>(RENDERER_STYLE_NODE, "meta", "rarity")

    /**
     * 元素的渲染格式。
     */
    val elementFormat: ItemMetaStylizer.ListFormat = getListFormat(config.node(RENDERER_STYLE_NODE, "meta", "element"))

    /**
     * 铭刻的渲染格式。
     */
    val kizamiFormat: ItemMetaStylizer.ListFormat = getListFormat(config.node(RENDERER_STYLE_NODE, "meta", "kizami"))

    /**
     * 保养度的渲染格式。
     */
    val durabilityFormat: String by config.entry<String>(RENDERER_STYLE_NODE, "meta", "durability")

    /**
     * 皮肤的渲染格式。
     */
    val skinFormat: String by config.entry<String>(RENDERER_STYLE_NODE, "meta", "skin")

    /**
     * 皮肤所有者的渲染格式。
     */
    val skinOwnerFormat: String by config.entry<String>(RENDERER_STYLE_NODE, "meta", "skin_owner")

    private fun getListFormat(provider: ConfigProvider): ItemMetaStylizer.ListFormat {
        return ItemMetaStylizerImpl.ListFormatImpl(
            mergedProvider = provider.entry<String>("merged"),
            singleProvider = provider.entry<String>("single"),
            separatorProvider = provider.entry<String>("separator")
        )
    }
    //</editor-fold>

    //<editor-fold desc="renderer_style.attsribute">
    /**
     * 空词条栏（属性）的渲染格式。
     */
    val emptyAttributeText: List<String> by config.entry<List<String>>(RENDERER_STYLE_NODE, "attribute", "empty")


    /**
     * 所有属性的渲染格式。
     *
     * ## 映射说明
     * - `map key` 跟 [AttributeRegistry] 里的一致，不是 [FullKey]
     * - `map value` 就是配置文件里对应的字符串值，无需做任何处理
     *
     * **注意该映射不包含 [Attributes.ATTACK_SPEED_LEVEL]**
     */
    val attributeFormats: Map<Key, String> by config.entry<Map<String, ConfigurationNode>>(RENDERER_STYLE_NODE, "attribute", "value")
        .map {
            it.mapKeys { (key, _) -> key }
                .filter { (key, _) -> key != Attributes.ATTACK_SPEED_LEVEL.key.value() }
                .mapKeys { (key, _) -> Key(NekoNamespaces.ATTRIBUTE, key) }
                .mapValues { (_, value) -> value.krequire<String>() }
                .withDefault { value -> "${value.asString()} (missing config)" }
        }

    /**
     * 攻击速度的渲染格式。
     */
    val attackSpeedFormat: AttributeStylizer.AttackSpeedFormat by config.node(RENDERER_STYLE_NODE, "attribute", "value", "attack_speed_level")
        .map {
            AttributeStylizerImpl.AttackSpeedFormatImpl(
                merged = it.node("merged").krequire<String>(),
                levels = it.node("levels")
                    .childrenMap()
                    .mapKeys { (key, _) -> key.toString().toInt() }
                    .mapValues { (_, value) -> value.krequire<String>() }
                    .withDefault { key -> "$key (missing config)" }
            )
        }


    /**
     * 运算模式的渲染格式。
     */
    val operationFormats: Map<AttributeModifier.Operation, Provider<String>> = AttributeModifier.Operation.entries.associateWith { operation ->
        config.entry<String>(RENDERER_STYLE_NODE, "attribute", "operation", operation.key)
    }
    //</editor-fold>

    //<editor-fold desc="renderer_style.skill">
    /**
     * 空词条栏的渲染格式（技能）。
     */
    val emptySkillText: List<String> by config.entry<List<String>>(RENDERER_STYLE_NODE, "skill", "empty")


    /**
     * 所有技能共用的渲染格式。
     */
    val commonSkillFormat: Unit by reloadable {
        // TODO
    }

    /**
     * 个别技能独有的渲染格式。
     */
    val skillFormats: Unit by reloadable {
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

        val primaryLines by config.entry<List<String>>(RENDERER_LAYOUT_NODE, "primary")
        val attDerivation = AttributeLoreMeta.Derivation(
            operationIndexProvider = config.entry<List<String>>(RENDERER_LAYOUT_NODE, "operation"),
            elementIndexProvider = config.entry<List<String>>(RENDERER_LAYOUT_NODE, "element")
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
                rawLine.startsWith(NekoNamespaces.SKILL + ":") -> {
                    ret = SkillLoreMeta(rawKey = Key(rawLine), rawIndex = rawIndex, default)
                }

                rawLine.startsWith(NekoNamespaces.ATTRIBUTE + ":") -> {
                    ret = AttributeLoreMeta(rawKey = Key(rawLine), rawIndex = rawIndex, default, attDerivation)
                }

                rawLine.startsWith(NekoNamespaces.ITEM_META + ":") -> {
                    ret = MetaLoreMeta(rawKey = Key(rawLine), rawIndex = rawIndex, default)
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
                        is MetaLoreMeta -> ItemMetaLineImpl(key, default)
                        is AttributeLoreMeta -> AttributeLineImpl(key, default)
                        is SkillLoreMeta -> SkillLineImpl(key, default)
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