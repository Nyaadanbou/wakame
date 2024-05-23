package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.argument.StringArgumentQueue
import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.config.entry
import cc.mewcraft.wakame.display.DisplaySupport.RENDERER_CONFIG_LAYOUT_NODE_NAME
import cc.mewcraft.wakame.display.DisplaySupport.RENDERER_LAYOUT_LINE_PATTERN
import cc.mewcraft.wakame.initializer.Initializable
import net.kyori.adventure.text.Component
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

internal interface RendererConfiguration : Initializable {
    /**
     * 所有的 [RawKey]，用于判断内容是否需要渲染。
     *
     * 如果一个 [RawKey] 不在该集合里，则说明不应该渲染。
     */
    val rawKeys: Set<RawKey>

    /**
     * 用于查询指定内容的 [LoreMeta]。
     */
    val loreMetaLookup: Map<FullKey, LoreMeta>

    /**
     * 用于查询指定内容的 [FullIndex]。
     */
    val loreIndexLookup: Map<FullKey, FullIndex>

    /**
     * 始终要渲染的内容。这些内容的文本在物品中始终不变。
     */
    val constantLoreLines: Collection<LoreLine>

    /**
     * 带有默认值的内容。当源数据不存在时将采用这里的默认值。
     */
    val defaultLoreLines: Collection<LoreLine>
}

internal class RendererConfigurationImpl(
    private val config: ConfigProvider,
) : RendererConfiguration, KoinComponent {
    private val dynamicLoreMetaCreatorRegistry: DynamicLoreMetaCreatorRegistry by inject()

    private val _rawKeys: MutableSet<RawKey> = ConcurrentHashMap.newKeySet()
    private val _loreMetaLookup: MutableMap<FullKey, LoreMeta> = ConcurrentHashMap()
    private val _loreIndexLookup: MutableMap<FullKey, FullIndex> = ConcurrentHashMap()
    private val _constantLoreLines: MutableCollection<LoreLine> = CopyOnWriteArrayList()
    private val _defaultLoreLines: MutableCollection<LoreLine> = CopyOnWriteArrayList()

    private fun loadLayout() {
        // Clear all data first
        _rawKeys.clear()
        _loreMetaLookup.clear()
        _loreIndexLookup.clear()
        _constantLoreLines.clear()
        _defaultLoreLines.clear()

        //<editor-fold desc="Implementation of filling up the maps/lists above">
        val primaryRawLines by config.entry<List<String>>(RENDERER_CONFIG_LAYOUT_NODE_NAME, "primary")
        val legalLinePattern = RENDERER_LAYOUT_LINE_PATTERN.toPattern()

        fun String.deserializeMini(): List<Component> {
            return this.split("\\r").map(DisplaySupport.MINI::deserialize)
        }

        fun createDynamicLoreMeta(rawIndex: Int, rawLine: String, default: List<Component>?): DynamicLoreMeta {
            val creator = dynamicLoreMetaCreatorRegistry.getApplicableCreator(rawLine)
                ?: throw IllegalArgumentException("Unrecognized raw line '$rawLine' while loading config $RENDERER_CONFIG_FILE")
            return creator.create(rawIndex, rawLine, default)
        }

        /**
         * Creates a lore meta from the config line.
         *
         * @param rawIndex the raw index in the config, without any modification
         * @param rawLine the raw line in the config, without any modification
         * @return a new instance
         */
        fun createLoreMeta(rawIndex: Int, rawLine: String): LoreMeta {
            val ret: LoreMeta
            val matcher = legalLinePattern.matcher(rawLine)
            if (matcher.matches()) {
                // 有参数，模式为 "({}){}"
                val params = matcher.group(1)
                val queue = StringArgumentQueue(params.split(':'))
                when (queue.pop()) {
                    // 具体解析为 "(fixed){}", "(fixed:{}){}"
                    "fixed" -> {
                        val companionNamespace = queue.peek() // nullable
                        val customConstantText = matcher.group(2)
                        ret = if (customConstantText.isBlank()) {
                            // 解析为 '(fixed){空}' 或 '(fixed:{}){空}'
                            EmptyConstantLoreMeta(rawIndex, companionNamespace)
                        } else {
                            // 解析为 '(fixed){}' 或 '(fixed:{}){}'
                            val constantText = customConstantText.deserializeMini()
                            CustomConstantLoreMeta(rawIndex, companionNamespace, constantText)
                        }
                    }

                    // 具体解析为 "(default:{}){}"
                    "default" -> {
                        val defaultText = queue.popOr(
                            "Unknown syntax for '(default...)' while load config $RENDERER_CONFIG_FILE. Correct syntax: '(default:_text_|empty)_key_'"
                        ).let {
                            if (it.isBlank() || it == "empty") {
                                listOf(Component.empty())
                            } else {
                                it.deserializeMini()
                            }
                        }
                        ret = createDynamicLoreMeta(rawIndex, matcher.group(2), defaultText)
                    }

                    else -> error("Unknown option '$params' while loading config $RENDERER_CONFIG_FILE")
                }
            } else {
                // 无参数，模式为 "{}"
                ret = createDynamicLoreMeta(rawIndex, rawLine, null)
            }
            return ret
        }

        // *accumulative* offset of the full index
        var accIndexOffset = 0

        // loop through each primary line and initialize LoreMeta
        for ((rawIndex, rawLine) in primaryRawLines.withIndex()) {
            val loreMeta = createLoreMeta(rawIndex, rawLine)

            // populate the raw keys
            _rawKeys += loreMeta.rawKey

            val fullIndexes = loreMeta.generateFullIndexMappings(accIndexOffset).onEach { (fullKey, fullIndex) ->
                // populate the index lookup
                val absent = (_loreIndexLookup.putIfAbsent(fullKey, fullIndex) == null)
                require(absent) { "Key $fullKey has already been added to indexes" }

                // populate the meta lookup
                _loreMetaLookup[fullKey] = loreMeta
            }
            // Accumulate the number of derived lines.
            // Minus one to neglect non-derived lines.
            accIndexOffset += fullIndexes.size - 1

            // populate the constant lore lines
            if (loreMeta is ConstantLoreMeta) {
                _constantLoreLines += ConstantLoreLine(loreMeta.generateFullKeys().first(), loreMeta.components)
            }

            // populate the default lore lines
            if (loreMeta is DynamicLoreMeta) {
                val loreLineList = loreMeta.createDefault()
                if (loreLineList != null) {
                    _defaultLoreLines += loreLineList
                }
            }
        }
        //</editor-fold>
    }

    override val rawKeys: Set<RawKey> = _rawKeys
    override val loreMetaLookup: Map<FullKey, LoreMeta> = _loreMetaLookup
    override val loreIndexLookup: Map<FullKey, FullIndex> = _loreIndexLookup
    override val constantLoreLines: Collection<LoreLine> = _constantLoreLines
    override val defaultLoreLines: Collection<LoreLine> = _defaultLoreLines

    override fun onPostWorld() {
        this.loadLayout()
    }

    override fun onReload() {
        this.loadLayout()
    }
}

internal class DynamicLoreMetaCreatorRegistryImpl : DynamicLoreMetaCreatorRegistry {
    private val creators: MutableList<DynamicLoreMetaCreator> = arrayListOf()

    override fun entries(): List<DynamicLoreMetaCreator> {
        return this.creators
    }

    override fun register(creator: DynamicLoreMetaCreator) {
        this.creators += creator
    }

    override fun getApplicableCreator(rawLine: String): DynamicLoreMetaCreator? {
        return this.creators.firstOrNull { creator -> creator.test(rawLine) }
    }
}