package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.Namespaces
import cc.mewcraft.wakame.argument.StringArgumentQueue
import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.config.entry
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.item.binary.cell.core.attribute.AttributeLoreLine
import cc.mewcraft.wakame.item.binary.cell.core.attribute.AttributeLoreMeta
import cc.mewcraft.wakame.item.binary.cell.core.skill.SkillLoreLine
import cc.mewcraft.wakame.item.binary.cell.core.skill.SkillLoreMeta
import cc.mewcraft.wakame.item.binary.meta.ItemMetaLoreLine
import cc.mewcraft.wakame.item.binary.meta.ItemMetaLoreMeta
import cc.mewcraft.wakame.util.Key
import net.kyori.adventure.text.Component
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

internal class RendererConfiguration(
    private val config: ConfigProvider,
) : Initializable {
    private companion object {
        private const val RENDERER_LAYOUT_LINE_PATTERN = "\\((.+?)\\)(.*)"
        private const val RENDERER_LAYOUT_NODE = "renderer_layout"
    }

    /**
     * 所有的 [RawKey]，用于判断内容是否需要渲染。
     *
     * 如果一个 [RawKey] 不在该集合里，则说明不应该渲染。
     */
    val rawKeys: Set<RawKey>
        get() = rawKeys0

    /**
     * 用于查询指定内容的 [LoreMeta]。
     */
    val loreMetaLookup: Map<FullKey, LoreMeta>
        get() = loreMetaLookup0

    /**
     * 用于查询指定内容的 [FullIndex]。
     */
    val loreIndexLookup: Map<FullKey, FullIndex>
        get() = loreIndexLookup0

    /**
     * 始终要渲染的内容。这些内容的文本在物品中始终不变。
     */
    val constantLoreLines: Collection<LoreLine>
        get() = constantLoreLines0

    /**
     * 带有默认值的内容。当源数据不存在时将采用这里的默认值。
     */
    val defaultLoreLines: Collection<LoreLine>
        get() = defaultLoreLines0

    override fun onPostWorld() {
        loadLayout()
    }

    override fun onReload() {
        loadLayout()
    }

    private val rawKeys0: MutableSet<RawKey> = ConcurrentHashMap.newKeySet()
    private val loreMetaLookup0: MutableMap<FullKey, LoreMeta> = ConcurrentHashMap()
    private val loreIndexLookup0: MutableMap<FullKey, FullIndex> = ConcurrentHashMap()
    private val constantLoreLines0: MutableCollection<LoreLine> = CopyOnWriteArrayList()
    private val defaultLoreLines0: MutableCollection<LoreLine> = CopyOnWriteArrayList()

    private fun loadLayout() {
        rawKeys0.clear()
        loreMetaLookup0.clear()
        loreIndexLookup0.clear()
        constantLoreLines0.clear()
        defaultLoreLines0.clear()

        val primaryLines by config.entry<List<String>>(RENDERER_LAYOUT_NODE, "primary")
        val attDerivation = AttributeLoreMeta.Derivation(
            operationIndex = config.entry<List<String>>(RENDERER_LAYOUT_NODE, "operation"),
            elementIndex = config.entry<List<String>>(RENDERER_LAYOUT_NODE, "element")
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
                rawLine.startsWith(Namespaces.SKILL + ":") -> {
                    ret = SkillLoreMeta(rawKey = Key(rawLine), rawIndex = rawIndex, default = default)
                }

                rawLine.startsWith(Namespaces.ATTRIBUTE + ":") -> {
                    ret = AttributeLoreMeta(rawKey = Key(rawLine), rawIndex = rawIndex, default = default, derivation = attDerivation)
                }

                rawLine.startsWith(Namespaces.ITEM_META + ":") -> {
                    ret = ItemMetaLoreMeta(rawKey = Key(rawLine), rawIndex = rawIndex, default = default)
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
            fun String.spiltAndDeserialize(): List<Component> {
                return this.split("\\r").map(DisplaySupport.mini()::deserialize)  // 以 '\r' 为分隔符，将文本分割为多行
            }

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
                        val customConstantText = matcher.group(2)
                        loreMeta = if (customConstantText.isBlank()) {
                            // 解析为 '(fixed)无内容' 或 '(fixed:...)无内容'
                            EmptyConstantLoreMeta(rawIndex, companionNamespace)
                        } else {
                            // 解析为 '(fixed)有内容' 或 '(fixed:...)有内容'
                            val constantText = customConstantText.spiltAndDeserialize()
                            CustomConstantLoreMeta(rawIndex, companionNamespace, constantText)
                        }
                    }

                    // 解析为 '(default:...)...'
                    "default" -> {
                        val defaultText = queue.popOr(
                            "Unknown syntax for '(default...)' while load config $RENDERER_CONFIG_FILE. Correct syntax: '(default:_text_|empty)_key_'"
                        ).let {
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
            rawKeys0 += loreMeta.rawKey

            val fullIndexes = loreMeta.fullIndexes(accIndexOffset).onEach { (fullKey, fullIndex) ->
                // populate the index lookup
                val absent = (loreIndexLookup0.putIfAbsent(fullKey, fullIndex) == null)
                require(absent) { "Key $fullKey has already been added to indexes" }

                // populate the meta lookup
                loreMetaLookup0[fullKey] = loreMeta
            }
            // Accumulate the number of derived lines.
            // Minus one to neglect non-derived lines.
            accIndexOffset += fullIndexes.size - 1

            // populate the constant lore lines
            if (loreMeta is ConstantLoreMeta) {
                constantLoreLines0 += ConstantLoreLine(loreMeta.fullKeys.first(), loreMeta.components)
            }

            // populate the default lore lines
            if (loreMeta is DynamicLoreMeta) {
                val default = loreMeta.default ?: continue

                // if the lore meta has a default value, add it to the default lore lines
                defaultLoreLines0 += loreMeta.fullKeys.map { key ->
                    when (loreMeta) {
                        is AttributeLoreMeta -> AttributeLoreLine(key, default)
                        is ItemMetaLoreMeta -> ItemMetaLoreLine(key, default)
                        is SkillLoreMeta -> SkillLoreLine(key, default)
                        else -> error("Unknown option '$key'")
                    }
                }
            }
        }
    }
}