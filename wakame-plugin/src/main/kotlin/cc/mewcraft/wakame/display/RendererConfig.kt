package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.argument.StringArgumentQueue
import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.config.entry
import cc.mewcraft.wakame.display.DisplaySupport.RENDERER_CONFIG_LAYOUT_NODE_NAME
import cc.mewcraft.wakame.display.DisplaySupport.RENDERER_LAYOUT_LINE_PATTERN
import cc.mewcraft.wakame.initializer.Initializable
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap
import net.kyori.adventure.text.Component
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

internal class RendererConfig(
    private val config: ConfigProvider,
) : Initializable, KoinComponent {
    /**
     * 所有的 [RawKey]，用于判断内容是否需要渲染。
     *
     * 如果一个 [RawKey] 不在该集合里，则说明不应该渲染。
     */
    val rawKeys: Set<RawKey>
        get() = _rawKeys

    /**
     * 用于查询指定内容的 [LoreMeta]。
     */
    val loreMetaLookup: Map<TooltipKey, LoreMeta>
        get() = _loreMetaLookup

    /**
     * 用于查询指定内容的 [TooltipIndex]。
     */
    val loreIndexLookup: Map<TooltipKey, TooltipIndex>
        get() = _loreIndexLookup

    /**
     * 始终要渲染的内容。这些内容的文本在物品中始终不变。
     */
    val constantLoreLines: Collection<LoreLine>
        get() = _constantLoreLines

    /**
     * 带有默认值的内容。当源数据不存在时将采用这里的默认值。
     */
    val defaultLoreLines: Collection<LoreLine>
        get() = _defaultLoreLines

    //
    // Internal implementations
    //

    private val dynamicLoreMetaCreatorRegistry: DynamicLoreMetaCreatorRegistry by inject()

    private val _rawKeys: MutableSet<RawKey> = ConcurrentHashMap.newKeySet()
    private val _loreMetaLookup: MutableMap<TooltipKey, LoreMeta> = ConcurrentHashMap()
    private val _loreIndexLookup: MutableMap<TooltipKey, TooltipIndex> = ConcurrentHashMap()
    private val _constantLoreLines: MutableCollection<LoreLine> = CopyOnWriteArrayList()
    private val _defaultLoreLines: MutableCollection<LoreLine> = CopyOnWriteArrayList()

    private fun loadLayout() {
        DisplaySupport.LOGGER.info("Loading renderer layout")

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
                ?: throw IllegalArgumentException("Unrecognized raw line '$rawLine' while loading config $RENDERER_GLOBAL_CONFIG_FILE")
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
                // 统一说明: {} 代表用户输入

                // 有参数，模式为 "({}){}"
                val params = matcher.group(1)
                val queue = StringArgumentQueue(params.split(':'))
                when (queue.pop()) {

                    // 解析为 "(fixed){}", "(fixed:{}){}"
                    "fixed" -> {
                        val companionNamespace = queue.peek() // nullable
                        val customConstantText = matcher.group(2)
                        ret = if (customConstantText.isBlank()) {
                            // 解析为 '(fixed){空}' 或 '(fixed:{}){空}'
                            BlankConstantLoreMeta(rawIndex, companionNamespace)
                        } else {
                            // 解析为 '(fixed){}' 或 '(fixed:{}){}'
                            val constantText = customConstantText.deserializeMini()
                            CustomConstantLoreMeta(rawIndex, companionNamespace, constantText)
                        }
                    }

                    // 解析为 "(default:{}){}"
                    "default" -> {
                        val defaultText = queue.popOr(
                            "Unknown syntax for '(default...)' while load config $RENDERER_GLOBAL_CONFIG_FILE. Correct syntax: '(default:{text}|blank|empty){key}'"
                        ).let {
                            if (it.isBlank() || it == "blank" || it == "empty") {
                                listOf(Component.empty())
                            } else {
                                it.deserializeMini()
                            }
                        }
                        ret = createDynamicLoreMeta(rawIndex, matcher.group(2), defaultText)
                    }

                    // 配置文件写错了
                    else -> error("Unknown option '$params' while loading config $RENDERER_GLOBAL_CONFIG_FILE")
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
                _constantLoreLines += LoreLine.constant(loreMeta.generateFullKeys().first(), loreMeta.components)
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

    override fun onPostWorld() {
        loadLayout()
    }

    override fun onReload() {
        loadLayout()
    }
}

/**
 * A creator of a [DynamicLoreMeta].
 */
internal interface DynamicLoreMetaCreator {
    /**
     * The namespace of this creator. Used to identify different creators.
     */
    val namespace: String

    /**
     * Checks whether this creator is capable of creating a [LoreMeta]
     * from the given [rawLine].
     *
     * @param rawLine the raw line text
     * @return `true` if the [rawLine] is "legal" for this creator
     */
    fun test(rawLine: String): Boolean

    /**
     * Creates the [DynamicLoreMeta].
     *
     * @param rawIndex the raw index
     * @param rawLine the raw line text
     * @param default the default text if there is any
     * @return the created [LoreMeta]
     *
     * @throws IllegalArgumentException if the raw line text is unrecognized
     */
    fun create(rawIndex: RawIndex, rawLine: String, default: List<Component>?): DynamicLoreMeta
}

/**
 * 注册表之 [DynamicLoreMetaCreator].
 *
 * 使用该对象来注册一个 [DynamicLoreMetaCreator].
 */
internal class DynamicLoreMetaCreatorRegistry {
    private val creators: MutableMap<String, DynamicLoreMetaCreator> = Object2ObjectArrayMap()

    /**
     * 获取该注册表类所有的 [DynamicLoreMetaCreator].
     */
    fun entries(): Map<String, DynamicLoreMetaCreator> {
        return this.creators
    }

    /**
     * 注册一个 [DynamicLoreMetaCreator].
     *
     * 会覆盖 [DynamicLoreMetaCreator.namespace] 相同的实例.
     */
    fun register(creator: DynamicLoreMetaCreator) {
        this.creators += creator.namespace to creator
    }

    /**
     * 获取一个适用于 [rawLine] 的 [DynamicLoreMetaCreator].
     *
     * @param rawLine 配置文件中的原始字符串, 未经任何修改
     * @return 返回一个合适的 [DynamicLoreMetaCreator]
     */
    fun getApplicableCreator(rawLine: String): DynamicLoreMetaCreator? {
        return this.creators.values.firstOrNull { creator -> creator.test(rawLine) }
    }
}