package cc.mewcraft.wakame.display2.implementation

import cc.mewcraft.wakame.argument.StringArgumentQueue
import cc.mewcraft.wakame.display2.*
import cc.mewcraft.wakame.util.yamlConfig
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.slf4j.Logger
import org.spongepowered.configurate.kotlin.extensions.getList
import java.nio.file.Path
import kotlin.io.path.readText

/* 这里定义了可以在不同渲染器之间通用的 RendererLayout 实现 */

internal abstract class AbstractRendererLayout(
    protected val rendererFormats: AbstractRendererFormats,
) : RendererLayout, KoinComponent {
    companion object Shared {
        val UNPROCESSED_PRIMARY_LINE_PATTERN = "^(?>\\((.+?)\\))?(.*)$".toPattern()
    }

    protected val mm = get<MiniMessage>()
    protected val logger = get<Logger>()

    override val staticIndexedTexts: ArrayList<IndexedText> = ArrayList()
    override val defaultIndexedTexts: ArrayList<IndexedText> = ArrayList()
    private val textOrdinalMap = Object2IntOpenHashMap<DerivedIndex>().apply { defaultReturnValue(-1) }
    private val textMetadataMap = Object2ObjectOpenHashMap<DerivedIndex, TextMeta>()

    /**
     * 初始化本实例的所有状态.
     */
    fun initialize(layoutPath: Path) {
        reset()

        val root = yamlConfig { withDefaults() }.buildAndLoadString(layoutPath.readText())

        val unprocessedPrimary = root.node("primary").getList<String>(listOf())

        // accumulative offset of the [DerivedIndex]
        var accIndexOffset = 0

        // loop through each unprocessed primary line and
        // create corresponding TextMeta for each of them
        for ((sourceIndex, unprocessedLine) in unprocessedPrimary.withIndex()) {
            val textMeta = createTextMeta(unprocessedLine, sourceIndex) ?: continue
            val derivedOrdinals = textMeta.generateOrdinals(accIndexOffset)
                .onEach { (derivedIndex, derivedOrdinal) ->
                    // populate the ordinal lookup
                    textOrdinalMap[derivedIndex] = derivedOrdinal
                    // populate the metadata lookup
                    textMetadataMap[derivedIndex] = textMeta
                }

            // Accumulate the number of derived lines so far.
            // Do -1 to neglect the unprocessed line itself.
            accIndexOffset += derivedOrdinals.size - 1

            // populate the static indexed text
            if (textMeta is StaticTextMeta) {
                val idx = textMeta.generateIndexes().first()
                val txt = textMeta.contents
                staticIndexedTexts += StaticIndexedText(idx, txt)
            }
            // populate the indexed text with default contents
            else if (textMeta is SimpleTextMeta) {
                val defaultIndexedText = textMeta.createDefault()
                if (defaultIndexedText != null) {
                    defaultIndexedTexts += defaultIndexedText
                }
            }
        }
    }

    private fun createTextMeta(unprocessedLine: String, sourceOrdinal: SourceOrdinal): TextMeta? {
        val matcher = UNPROCESSED_PRIMARY_LINE_PATTERN.matcher(unprocessedLine)
        if (!matcher.matches()) {
            logger.warn("Invalid line: '$unprocessedLine'")
            return null
        }

        // Notes: 下面注释中的 {} 代表用户输入

        val group1 = matcher.group(1)
        val group2 = matcher.group(2)
        when {
            // 字符串包含参数, 模式为 "({}){}"
            group1 != null -> {
                val argsQueue = StringArgumentQueue(group1.split(':'))
                when (
                    val category = argsQueue.pop() // 取出第一个参数, 要么是 "default", 要么是 "fixed"
                ) {
                    // 类型为 static indexed text
                    StaticTextMeta.STATIC_IDENTIFIER -> {
                        val companionNamespace: String? = argsQueue.peek()
                        val customConstantText: String = group2
                        if (customConstantText.isBlank()) {
                            // 模式为 "(fixed)" 或 "(fixed:{})"
                            return BlankStaticTextMeta(sourceOrdinal, companionNamespace)
                        } else {
                            // 模式为 "(fixed){}" 或 "(fixed:{}){}"
                            val constantText = deserializeToComponents(customConstantText)
                            return CustomStaticTextMeta(sourceOrdinal, companionNamespace, constantText)
                        }
                    }

                    // 类型为 simple indexed text (带默认值)
                    SimpleTextMeta.DEFAULT_IDENTIFIER -> {
                        val errorMessage = "Unknown syntax for '(default:...)' while load config '???'. Correct syntax: `(default:'{}'|blank|empty){}`"
                        val defaultText = argsQueue.popOr(errorMessage).let {
                            when {
                                // 模式为 "(default:blank){}", "(default:empty){}", "(default:){}"
                                it == "blank" || it == "empty" -> {
                                    listOf(Component.empty())
                                }

                                // 模式为 "(default:'{}'){}"
                                it.startsWith("'") && it.endsWith("'") -> {
                                    deserializeToComponents(it.removeSurrounding("'"))
                                }

                                else -> {
                                    logger.warn("Invalid default text while reading line '$unprocessedLine'")
                                    return null
                                }
                            }
                        }
                        val sourceIndex = runCatching { Key.key(group2) }.getOrElse {
                            logger.warn("Invalid source index while reading line '$unprocessedLine'")
                            return null
                        }
                        return createTextMeta0(sourceIndex, sourceOrdinal, defaultText)
                    }

                    // 配置文件写错了
                    else -> {
                        error("Unknown option '$group1' while reading line '$unprocessedLine'")
                    }
                }
            }

            // 字符串不包含参数, 模式为 "{}"
            group2 != null -> {
                val sourceIndex = runCatching { Key.key(group2) }.getOrElse {
                    logger.warn("Invalid source index while reading line '$unprocessedLine'")
                    return null
                }
                return createTextMeta0(sourceIndex, sourceOrdinal, null)
            }
        }

        return null
    }

    private fun createTextMeta0(sourceIndex: SourceIndex, sourceOrdinal: SourceOrdinal, defaultText: List<Component>?): TextMeta? {
        val factory = rendererFormats.textMetaFactoryRegistry.getApplicableFactory(sourceIndex)
        if (factory == null) {
            logger.warn("Can't find a valid text meta factory for source index '$sourceIndex'")
            return null
        }
        return factory.create(sourceIndex, sourceOrdinal, defaultText)
    }

    private fun deserializeToComponents(text: String): List<Component> {
        return text.split("\\r").map(mm::deserialize)
    }

    /**
     * 重置本实例的所有状态.
     */
    protected fun reset() {
        staticIndexedTexts.clear()
        defaultIndexedTexts.clear()
        textOrdinalMap.clear()
        textMetadataMap.clear()
    }

    /**
     * 获取指定的 [index] 对应的*顺序*. 数值越小越靠前.
     *
     * 如果 [index] 没有对应的顺序, 则返回 `null`.
     */
    override fun getOrdinal(index: DerivedIndex): DerivedOrdinal? {
        val ret = textOrdinalMap.getInt(index)
        if (ret == -1) {
            logger.warn("Can't find ordinal for derived index '$index'")
            return null
        }
        return ret
    }

    /**
     * 获取指定的 [index] 对应的*元数据*.
     *
     * 如果 [index] 没有对应的元数据, 则返回 `null`.
     */
    override fun <T : TextMeta> getMetadata(index: DerivedIndex): T? {
        @Suppress("UNCHECKED_CAST")
        val ret = textMetadataMap[index] as T?
        if (ret == null) {
            logger.warn("Can't find metadata for derived index '$index'")
        }
        return ret
    }
}

/**
 * 用来描述不会衍生并且只有一个 [SourceIndex] 的 [IndexedText].
 *
 * 例如: 标准渲染器中的 `lore` 和 `level`.
 */
internal data class SingleSimpleTextMeta(
    override val sourceIndex: SourceIndex,
    override val sourceOrdinal: SourceOrdinal,
    override val defaultText: List<Component>?,
) : SimpleTextMeta {
    override fun createDefault(): List<IndexedText>? {
        return defaultText?.let { listOf(SimpleIndexedText(sourceIndex, it)) }
    }

    override fun generateIndexes(): List<DerivedIndex> {
        return listOf(sourceIndex)
    }
}

/**
 * 负责创建 [SingleSimpleTextMeta] 的工厂.
 *
 * @param namespace 命名空间
 * @param id 对应的 id
 */
internal data class SingleSimpleTextMetaFactory(
    override val namespace: String,
    private val id: String,
) : TextMetaFactory {
    override fun test(sourceIndex: SourceIndex): Boolean {
        return sourceIndex.namespace() == namespace && sourceIndex.value() == id
    }

    override fun create(sourceIndex: SourceIndex, sourceOrdinal: SourceOrdinal, defaultText: List<Component>?): SimpleTextMeta {
        return SingleSimpleTextMeta(sourceIndex, sourceOrdinal, defaultText)
    }
}

/**
 * 用来描述拥有自定义内容的 [StaticIndexedText].
 */
// 由于仅限内部使用, 所以无工厂实现 (可能统一比较好?)
// 就目前来说, 创建实例的职责是直接由配置序列化承担
internal data class CustomStaticTextMeta(
    override val sourceOrdinal: SourceOrdinal,
    override val companionNamespace: String?,
    override val contents: List<Component>,
) : StaticTextMeta

/**
 * 用来描述其内容为“空白”的 [StaticIndexedText].
 */
// 无工厂实现, 原因同上
internal data class BlankStaticTextMeta(
    override val sourceOrdinal: SourceOrdinal,
    override val companionNamespace: String?,
) : StaticTextMeta {
    override val contents: List<Component> = listOf(Component.empty())
}