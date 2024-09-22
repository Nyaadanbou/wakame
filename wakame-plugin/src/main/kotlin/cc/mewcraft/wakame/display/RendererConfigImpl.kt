package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.argument.StringArgumentQueue
import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.config.entry
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger

internal class RendererConfigImpl(
    private val config: ConfigProvider,
) : RendererConfig, KoinComponent {

    companion object {
        const val RAW_LINE_PATTERN = "\\((.+?)\\)(.*)"
    }

    private val logger by inject<Logger>()
    private val miniMessage by inject<MiniMessage>()
    private val dynamicLoreMetaCreators by inject<DynamicLoreMetaCreators>()

    override val rawTooltipKeys: HashSet<RawTooltipKey> = HashSet()
    override val loreMetaMap: HashMap<TooltipKey, LoreMeta> = HashMap()
    override val loreIndexMap: HashMap<TooltipKey, TooltipIndex> = HashMap()
    override val constantLoreLines: ArrayList<LoreLine> = ArrayList()
    override val defaultLoreLines: ArrayList<LoreLine> = ArrayList()

    override fun loadLayout() {
        // Clear all data first
        rawTooltipKeys.clear()
        loreMetaMap.clear()
        loreIndexMap.clear()
        constantLoreLines.clear()
        defaultLoreLines.clear()

        //<editor-fold desc="Implementations of filling up the data above">
        val primaryRawLines by config.entry<List<String>>("primary")
        val rawLinePattern = RAW_LINE_PATTERN.toPattern()

        fun String.convertMiniMessageToComponents(): List<Component> {
            return this.split("\\r").map(miniMessage::deserialize)
        }

        fun createDynamicLoreMeta(rawIndex: Int, rawLine: String, default: List<Component>?): DynamicLoreMeta? {
            val creator = dynamicLoreMetaCreators.getApplicableCreator(rawLine) ?: run {
                logger.warn("Unrecognized raw line '$rawLine' while loading config '${config.relPath}'")
                return null
            }
            return creator.create(rawIndex, rawLine, default)
        }

        /**
         * Creates a lore meta from the config line.
         *
         * @param rawIndex the raw tooltip index in the config, without any modification
         * @param rawLine the raw line in the config, without any modification
         * @return a new instance
         */
        fun createLoreMeta(rawIndex: Int, rawLine: String): LoreMeta? {
            val ret: LoreMeta?
            val rawLineMatcher = rawLinePattern.matcher(rawLine)
            if (rawLineMatcher.matches()) {
                // 统一说明: {} 代表用户输入

                // 有参数，模式为 "({}){}"
                val params = rawLineMatcher.group(1)
                val queue = StringArgumentQueue(params.split(':'))
                when (queue.pop()) {

                    // 解析为 "(fixed){}", "(fixed:{}){}"
                    ConstantLoreMeta.NAMESPACE_CONSTANT -> {
                        val companionNamespace = queue.peek() // nullable
                        val customConstantText = rawLineMatcher.group(2)
                        ret = if (customConstantText.isBlank()) {
                            // 解析为 '(fixed)' 或 '(fixed:{})'
                            BlankConstantLoreMeta(rawIndex, companionNamespace)
                        } else {
                            // 解析为 '(fixed){}' 或 '(fixed:{}){}'
                            val constantText = customConstantText.convertMiniMessageToComponents()
                            CustomConstantLoreMeta(rawIndex, companionNamespace, constantText)
                        }
                    }

                    // 解析为 "(default:{}){}"
                    DynamicLoreMeta.NAMESPACE_DEFAULT -> {
                        val errorMessage = "Unknown syntax for '(default...)' while load config '$RENDERERS_CONFIG_DIR'. Correct syntax: `(default:'_text_'|blank|empty)_key_`"
                        val defaultText = queue.popOr(errorMessage).let {
                            when {
                                // 解析为 "(default:blank){}", "(default:empty){}", "(default:){}"
                                it.isBlank() || it == "blank" || it == "empty" -> {
                                    listOf(Component.empty())
                                }

                                // 解析为 "(default:'_text_'){}"
                                it.startsWith("'") && it.endsWith("'") -> {
                                    it.removeSurrounding("'").convertMiniMessageToComponents()
                                }

                                else -> {
                                    throw IllegalArgumentException(errorMessage)
                                }
                            }
                        }
                        ret = createDynamicLoreMeta(rawIndex, rawLineMatcher.group(2), defaultText)
                    }

                    // 配置文件写错了
                    else -> {
                        error("Unknown option '$params' while loading config '$RENDERERS_CONFIG_DIR'")
                    }
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
            val loreMeta = createLoreMeta(rawIndex, rawLine) ?: continue

            // populate the raw tooltip keys
            rawTooltipKeys += loreMeta.rawTooltipKey

            val fullIndexes = loreMeta.generateTooltipKeyIndexes(accIndexOffset).onEach { (fullKey, fullIndex) ->
                // populate the index lookup
                val absent = (loreIndexMap.putIfAbsent(fullKey, fullIndex) == null)
                require(absent) { "Key '$fullKey' has already been added to indexes" }

                // populate the meta lookup
                loreMetaMap[fullKey] = loreMeta
            }
            // Accumulate the number of derived lines.
            // Minus one to neglect non-derived lines.
            accIndexOffset += fullIndexes.size - 1

            // populate the constant lore lines
            if (loreMeta is ConstantLoreMeta) {
                constantLoreLines += LoreLine.constant(
                    loreMeta.generateTooltipKeys().first(),
                    loreMeta.components
                )
            }

            // populate the default lore lines
            if (loreMeta is DynamicLoreMeta) {
                val loreLineList = loreMeta.createDefault()
                if (loreLineList != null) {
                    defaultLoreLines += loreLineList
                }
            }
        }
        //</editor-fold>

        logger.info("Loaded renderer layout")
    }
}