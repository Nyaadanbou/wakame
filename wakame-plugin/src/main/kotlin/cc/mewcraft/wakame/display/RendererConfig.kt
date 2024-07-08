package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.argument.StringArgumentQueue
import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.config.entry
import cc.mewcraft.wakame.display.DisplaySupport.RENDERER_CONFIG_LAYOUT_NODE_NAME
import cc.mewcraft.wakame.display.DisplaySupport.RENDERER_LAYOUT_LINE_PATTERN
import cc.mewcraft.wakame.eventbus.PluginEventBus
import cc.mewcraft.wakame.initializer.Initializable
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

internal class RendererConfig(
    private val config: ConfigProvider,
) : Initializable, KoinComponent {
    /**
     * 所有的 [RawTooltipKey], 用于判断内容是否需要渲染.
     *
     * 如果一个 [RawTooltipKey] 不在该集合里, 则说明不应该渲染.
     */
    val rawTooltipKeys: Set<RawTooltipKey>
        get() = _rawTooltipKeys

    /**
     * 用于查询指定内容的 [LoreMeta].
     */
    val loreMetaLookup: Map<TooltipKey, LoreMeta>
        get() = _loreMetaLookup

    /**
     * 用于查询指定内容的 [TooltipIndex].
     */
    val loreIndexLookup: Map<TooltipKey, TooltipIndex>
        get() = _loreIndexLookup

    /**
     * 始终要渲染的内容. 这些内容的文本在物品中始终不变.
     */
    val constantLoreLines: Collection<LoreLine>
        get() = _constantLoreLines

    /**
     * 带有默认值的内容. 当源数据不存在时将采用这里的默认值.
     */
    val defaultLoreLines: Collection<LoreLine>
        get() = _defaultLoreLines

    //
    // Internal implementations
    //

    private val logger by inject<Logger>()
    private val miniMessage by inject<MiniMessage>()
    private val dynamicLoreMetaCreatorRegistry by inject<DynamicLoreMetaCreatorRegistry>()

    private val _rawTooltipKeys = ConcurrentHashMap.newKeySet<RawTooltipKey>()
    private val _loreMetaLookup = ConcurrentHashMap<TooltipKey, LoreMeta>()
    private val _loreIndexLookup = ConcurrentHashMap<TooltipKey, TooltipIndex>()
    private val _constantLoreLines = CopyOnWriteArrayList<LoreLine>()
    private val _defaultLoreLines = CopyOnWriteArrayList<LoreLine>()

    private fun loadLayout() {
        logger.info("Loading renderer layout")

        // Clear all data first
        _rawTooltipKeys.clear()
        _loreMetaLookup.clear()
        _loreIndexLookup.clear()
        _constantLoreLines.clear()
        _defaultLoreLines.clear()

        //<editor-fold desc="Implementation of filling up the maps/lists above">
        val primaryRawLines by config.entry<List<String>>(RENDERER_CONFIG_LAYOUT_NODE_NAME, "primary")
        val legalLinePattern = RENDERER_LAYOUT_LINE_PATTERN.toPattern()

        fun String.deserializeMini(): List<Component> {
            return this.split("\\r").map(miniMessage::deserialize)
        }

        fun createDynamicLoreMeta(rawIndex: Int, rawLine: String, default: List<Component>?): DynamicLoreMeta {
            val creator = dynamicLoreMetaCreatorRegistry.getApplicableCreator(rawLine)
                ?: throw IllegalArgumentException("Unrecognized raw line '$rawLine' while loading config $RENDERER_GLOBAL_CONFIG_FILE")
            return creator.create(rawIndex, rawLine, default)
        }

        /**
         * Creates a lore meta from the config line.
         *
         * @param rawIndex the raw tooltip index in the config, without any modification
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

        // *accumulative* offset of the [TooltipIndex]
        var accIndexOffset = 0

        // loop through each primary line and initialize LoreMeta
        for ((rawIndex, rawLine) in primaryRawLines.withIndex()) {
            val loreMeta = createLoreMeta(rawIndex, rawLine)

            // populate the raw tooltip keys
            _rawTooltipKeys += loreMeta.rawTooltipKey

            val fullIndexes = loreMeta.generateTooltipKeyIndexes(accIndexOffset).onEach { (fullKey, fullIndex) ->
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
                _constantLoreLines += LoreLine.constant(loreMeta.generateTooltipKeys().first(), loreMeta.components)
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

    override suspend fun onPostWorldAsync() {
        PluginEventBus.get().post(RendererConfigReloadEvent(rawTooltipKeys))
    }

    override fun onReload() {
        loadLayout()
    }

    override suspend fun onReloadAsync() {
        PluginEventBus.get().post(RendererConfigReloadEvent(rawTooltipKeys))
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
     * @param rawTooltipIndex the raw tooltip index
     * @param rawLine the raw line text
     * @param default the default text if there is any
     * @return the created [LoreMeta]
     *
     * @throws IllegalArgumentException if the raw line text is unrecognized
     */
    fun create(rawTooltipIndex: RawTooltipIndex, rawLine: String, default: List<Component>?): DynamicLoreMeta
}

/**
 * 注册表之 [DynamicLoreMetaCreator].
 *
 * 使用该对象来注册一个 [DynamicLoreMetaCreator].
 */
internal class DynamicLoreMetaCreatorRegistry : KoinComponent {
    private val logger by inject<Logger>()
    private val creators = Object2ObjectArrayMap<String, DynamicLoreMetaCreator>()

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
        logger.info("Registered DynamicLoreMetaCreator for '${creator.namespace}'")
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