package cc.mewcraft.wakame.display2.implementation

import cc.mewcraft.wakame.Injector
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.argument.StringArgumentQueue
import cc.mewcraft.wakame.display2.DerivedIndex
import cc.mewcraft.wakame.display2.DerivedOrdinal
import cc.mewcraft.wakame.display2.IndexedText
import cc.mewcraft.wakame.display2.RendererLayout
import cc.mewcraft.wakame.display2.SimpleTextMeta
import cc.mewcraft.wakame.display2.SourceIndex
import cc.mewcraft.wakame.display2.SourceOrdinal
import cc.mewcraft.wakame.display2.StaticIndexedText
import cc.mewcraft.wakame.display2.StaticTextMeta
import cc.mewcraft.wakame.display2.TextMeta
import cc.mewcraft.wakame.util.buildYamlConfigLoader
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.kyori.adventure.key.InvalidKeyException
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.spongepowered.configurate.kotlin.extensions.getList
import java.nio.file.Path
import kotlin.io.path.readText

/* 这里定义了可以在不同渲染器之间通用的 RendererLayout 实现 */

internal abstract class AbstractRendererLayout(
    protected val renderer: AbstractItemRenderer<*, *>,
) : RendererLayout {

    companion object Shared {
        val UNPROCESSED_PRIMARY_LINE_PATTERN = "^(?>\\((.+?)\\))?(.*)$".toPattern()
    }

    override val staticIndexedTextList: ArrayList<IndexedText> = ArrayList()
    override val defaultIndexedTextList: ArrayList<IndexedText> = ArrayList()

    protected val mm = Injector.get<MiniMessage>()

    // derived index (key) -> ordinal (int)
    private val indexedTextOrdinalMap = Object2IntOpenHashMap<DerivedIndex>().apply { defaultReturnValue(-1) }

    // derived index (key) -> text meta
    private val indexedTextMetadataMap = Object2ObjectOpenHashMap<DerivedIndex, TextMeta>()

    // 不包含 StaticIndexedText
    private val bakedIndexSet = ObjectOpenHashSet<DerivedIndex>()

    // 不包含 StaticIndexedText
    private val bakedIndexNamespaceSet = ObjectOpenHashSet<String>()

    // 不包含 StaticIndexedText
    private val bakedIndexIdSet = ObjectOpenHashSet<String>()

    /**
     * 重置本实例的所有状态.
     */
    protected fun reset() {
        staticIndexedTextList.clear()
        defaultIndexedTextList.clear()
        indexedTextOrdinalMap.clear()
        indexedTextMetadataMap.clear()
    }

    /**
     * 初始化本实例的所有状态.
     */
    fun initialize(layoutPath: Path) {
        reset()

        val rootNode = buildYamlConfigLoader { withDefaults() }.buildAndLoadString(layoutPath.readText())

        val unprocessedPrimary = rootNode.node("primary").getList<String>(listOf())

        // accumulative offset of the [DerivedIndex]
        var accIndexOffset = 0

        // loop through each unprocessed primary line and
        // create corresponding TextMeta for each of them
        for ((sourceOrdinal, unprocessedLine) in unprocessedPrimary.withIndex()) {
            val textMeta = createTextMeta(unprocessedLine, sourceOrdinal) ?: continue

            val derivedOrdinals = textMeta.deriveOrdinals(accIndexOffset).onEach { (derivedIndex, derivedOrdinal) ->
                // populate the ordinal lookup
                indexedTextOrdinalMap[derivedIndex] = derivedOrdinal
                // populate the metadata lookup
                indexedTextMetadataMap[derivedIndex] = textMeta

                // populate the set of baked indexes
                bakedIndexSet += derivedIndex
                // populate the set of baked index namespaces
                bakedIndexNamespaceSet += derivedIndex.namespace()
                // populate the set of baked index ids
                bakedIndexIdSet += derivedIndex.value()
            }

            // Accumulate the number of derived lines so far.
            // Do -1 to neglect the unprocessed line itself.
            accIndexOffset += derivedOrdinals.size - 1

            // populate the static indexed text
            if (textMeta is StaticTextMeta) {
                val idx = textMeta.derivedIndexes.first()
                val txt = textMeta.contents
                staticIndexedTextList += StaticIndexedText(idx, txt)
            }
            // populate the indexed text with default contents
            else if (textMeta is SimpleTextMeta) {
                val defaultIndexedText = textMeta.createDefault()
                if (defaultIndexedText != null) {
                    defaultIndexedTextList += defaultIndexedText
                }
            }
        }
    }

    private fun createTextMeta(unprocessedLine: String, sourceOrdinal: SourceOrdinal): TextMeta? {
        val matcher = UNPROCESSED_PRIMARY_LINE_PATTERN.matcher(unprocessedLine)
        if (!matcher.matches()) {
            LOGGER.warn("Invalid line: '$unprocessedLine'")
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
                    argsQueue.pop() // 取出第一个参数, 要么是 "default", 要么是 "fixed"
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
                                    LOGGER.warn("Invalid default text while reading line '$unprocessedLine'")
                                    return null
                                }
                            }
                        }
                        val sourceIndex = parseSourceIndex(group2) ?: return null
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
                val sourceIndex = parseSourceIndex(group2) ?: return null
                return createTextMeta0(sourceIndex, sourceOrdinal, null)
            }
        }

        return null
    }

    private fun createTextMeta0(sourceIndex: SourceIndex, sourceOrdinal: SourceOrdinal, defaultText: List<Component>?): TextMeta? {
        val factory = renderer.formats.textMetaFactoryRegistry.getApplicableFactory(sourceIndex)
        if (factory == null) {
            LOGGER.warn("Can't find a valid TextMetaFactory for source index '$sourceIndex'")
            return null
        }
        return factory.create(sourceIndex, sourceOrdinal, defaultText)
    }

    private fun parseSourceIndex(unprocessed: String): Key? {
        try {
            return Key.key(unprocessed)
        } catch (_: InvalidKeyException) {
            LOGGER.warn("Invalid source index while reading line '$unprocessed'")
            return null
        }
    }

    private fun deserializeToComponents(text: String): List<Component> {
        return text.split("\\r").map(mm::deserialize)
    }

    override fun getOrdinal(index: DerivedIndex): DerivedOrdinal {
        return indexedTextOrdinalMap.getInt(index)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : TextMeta> getMetadata(index: DerivedIndex): T? {
        val ret = indexedTextMetadataMap[index]
        if (ret == null) {
            LOGGER.warn("Can't find metadata for derived index '$index'")
        }
        return ret as T?
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
) : StaticTextMeta {
    override val derivedIndexes: List<DerivedIndex> = deriveIndexes()
}

/**
 * 用来描述其内容为“空白”的 [StaticIndexedText].
 */
// 无工厂实现, 原因同上
internal data class BlankStaticTextMeta(
    override val sourceOrdinal: SourceOrdinal,
    override val companionNamespace: String?,
) : StaticTextMeta {
    override val contents: List<Component> = listOf(Component.empty())
    override val derivedIndexes: List<DerivedIndex> = deriveIndexes()
}