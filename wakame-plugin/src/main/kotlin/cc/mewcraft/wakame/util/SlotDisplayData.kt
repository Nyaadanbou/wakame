package cc.mewcraft.wakame.util

import cc.mewcraft.lazyconfig.configurate.SimpleSerializer
import cc.mewcraft.wakame.LOGGER
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.StyleBuilderApplicable
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.jetbrains.annotations.ApiStatus
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.getList
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Setting
import java.lang.reflect.Type

@DslMarker
annotation class SlotDisplayDataDsl

/**
 * A mark interface.
 */
interface SlotDisplayData {

    @SlotDisplayDataDsl
    open class PlaceholderBuilder(
        private val dictionary: SlotDisplayDictData,
    ) {
        private val builder: TagResolver.Builder = TagResolver.builder()

        /**
         * 从字典中获取一个值.
         */
        fun dict(key: String): String {
            // 开发日记 2024/12/25: 返回空字符串?
            return dictionary[key] ?: error("No such key in dict: $key")
        }

        /**
         * 添加一个占位符.
         * @see Placeholder.parsed
         */
        fun parsed(key: String, value: String) {
            builder.resolver(Placeholder.parsed(key, value))
        }

        /**
         * 添加一个占位符.
         * @see Placeholder.unparsed
         */
        fun unparsed(key: String, value: String) {
            builder.resolver(Placeholder.unparsed(key, value))
        }

        /**
         * 添加一个占位符.
         * @see Placeholder.component
         */
        fun component(key: String, value: Component) {
            builder.resolver(Placeholder.component(key, value))
        }

        /**
         * 添加一个占位符.
         * @see Placeholder.styling
         */
        fun styling(key: String, vararg value: StyleBuilderApplicable) {
            builder.resolver(Placeholder.styling(key, *value))
        }

        /**
         * 完成构建.
         */
        @ApiStatus.Internal
        fun build(): TagResolver {
            return builder.build()
        }
    }

}

/**
 * 菜单图标的字符串字典.
 */
@ConfigSerializable
data class SlotDisplayDictData(
    @Setting(nodeFromParent = true)
    private val dictionary: Map<String, String> = emptyMap(),
) : SlotDisplayData {

    /**
     * 获取该字典中指定键的值.
     */
    operator fun get(key: String): String? {
        return dictionary[key]
    }
}

@ConfigSerializable
data class SlotDisplayNameData(
    @Setting(nodeFromParent = true)
    val name: String,
) : SlotDisplayData {

    /**
     * 解析 [SlotDisplayNameData] 中的 [name], 返回一个 [Component].
     */
    fun resolve(placeholder: TagResolver): Component {
        return MiniMessage.miniMessage().deserialize(name, placeholder)
    }

    /**
     * 解析 [SlotDisplayNameData] 中的 [name], 返回一个 [Component].
     */
    fun resolve(dict: SlotDisplayDictData = SlotDisplayDictData(), dsl: SlotDisplayData.PlaceholderBuilder.() -> Unit): Component {
        return MiniMessage.miniMessage().deserialize(name, SlotDisplayData.PlaceholderBuilder(dict).apply(dsl).build())
    }
}

/**
 * 代表一个专用于菜单图标的 `minecraft:lore`, 提供方便的占位符解析功能.
 *
 * ### 实例化方式
 * - 从配置文件构建: 使用配套的 [SlotDisplayLoreDataSerializer] 来反序列化.
 * - 从代码直接构建: 使用静态函数 [SlotDisplayLoreData.configureAndBuild].
 */
data class SlotDisplayLoreData(
    private val lines: List<Line>,
) : SlotDisplayData {

    companion object {

        /**
         * 配置并创建一个 [SlotDisplayLoreData] 实例.
         */
        fun configureAndBuild(init: Builder.() -> Unit): SlotDisplayLoreData {
            return SlotDisplayLoreData(Builder().apply(init))
        }
    }

    constructor(builder: Builder) : this(builder.build().lines)

    /**
     * 解析 [SlotDisplayLoreData] 中的所有行, 返回一个 [Component] 的列表.
     *
     * @param config 解析的配置
     * @return 解析后的 [Component] 列表
     */
    fun resolve(config: LineConfig): List<Component> {
        return lines.flatMap { line ->
            when (line) {
                is Line.Standard -> {
                    val rawText = line.rawText
                    val placeholders = config.getPlaceholders()
                    val resolved = MiniMessage.miniMessage().deserialize(rawText, placeholders)
                    listOf(resolved)
                }

                is Line.Folded -> {
                    val key = line.key
                    val rawText = line.rawText
                    val folded = config.getFoldedLines(key) ?: return@flatMap emptyList()
                    val placeholders = config.getPlaceholders()
                    folded.map { MiniMessage.miniMessage().deserialize(rawText, TagResolver.resolver(placeholders, Placeholder.component(key, it))) }
                }
            }
        }
    }

    /**
     * 解析 [SlotDisplayLoreData] 中的所有行, 返回一个 [Component] 的列表.
     *
     * @param dict 占位符的映射表
     * @param dsl 解析的配置, 通过 DSL 构建
     * @return 解析后的 [Component] 列表
     */
    fun resolve(dict: SlotDisplayDictData = SlotDisplayDictData(), dsl: LineConfig.Builder.() -> Unit): List<Component> {
        return resolve(LineConfig.Builder(dict).apply(dsl).build())
    }

    /**
     * 储存一行原始的字符串.
     */
    sealed class Line(val rawText: String) {
        /**
         * 仅包含普通的 [Tag].
         * 解析后变成一个单独的 [Component], 替换当前行.
         *
         * 例如在配置文件中:
         * ```yaml
         * - "*something* <some_tag>!"
         * ```
         *
         * @param rawText 用于解析的文本, 可以包含普通的 [Tag]
         */
        class Standard(rawText: String) : Line(rawText)

        /**
         * 包含最多1个折叠的占位符, 也可以包含 0 或多个普通的 [Tag].
         * 解析后变成一个 [Component] 的列表, 替换当前行为多行内容.
         *
         * 例如在配置文件中:
         * ```yaml
         * - "example text <choice_list>" # <choice_list> 是一个折叠的占位符
         * ```
         *
         * 然后这个 <choice_list> 有3个 [Component] 分别是 "abc", "def", "ghi", 那么解析后就会变成:
         * ```yaml
         * - "example text abc"
         * - "example text def"
         * - "example text ghi"
         * ```
         *
         * @param key 折叠的占位符的标识符, 用于在 [LineConfig] 中查找对应的 [Component] 列表
         * @param rawText 用于解析的文本, 必须包含一个占位符 [key], 可以包含若干普通的 [Tag]
         */
        class Folded(val key: String, rawText: String) : Line(rawText) {

            @SlotDisplayDataDsl
            class Builder(
                // 字典, 通常是外部物品配置文件里定义的
                private val dictionary: SlotDisplayDictData,
                // 专用于该 Line.Folded 的占位符, 仅在此生效
                private val placeholders: TagResolver,
            ) {
                // 已解析完成的 Components, 作为该 Line.Folded 的最终样子
                private val resolvedLines: MutableList<Component> = mutableListOf()

                /**
                 * 获取字典中的值, 如果不存在则抛异常.
                 */
                fun dict(key: String): String {
                    // 开发日记 2024/12/25: 返回空字符串?
                    return dictionary[key] ?: error("No such key in dict: $key")
                }

                /**
                 * 添加一行内容.
                 */
                fun literal(text: String) {
                    resolvedLines.add(MiniMessage.miniMessage().deserialize(text, placeholders))
                }

                /**
                 * 添加一行内容.
                 */
                fun literal(component: Component) {
                    resolvedLines.add(component)
                }

                /**
                 * 添加一行内容, 应用仅适用于这一行的占位符.
                 */
                fun resolve(key: String, dsl: DedicatedPlaceholderBuilder.() -> Unit) {
                    val rawText = dict(key)
                    // local tag resolver & preprocess logic
                    val (resolver, preprocess) = DedicatedPlaceholderBuilder(dictionary).apply(dsl).buildPreprocessResult()
                    val parsed = MiniMessage.miniMessage().deserialize(preprocess.applyTo(rawText), resolver, placeholders)
                    resolvedLines.add(parsed)
                }

                /**
                 * 构建并返回解析后的结果.
                 */
                @ApiStatus.Internal
                fun build(): List<Component> {
                    return resolvedLines
                }

                @SlotDisplayDataDsl
                class DedicatedPlaceholderBuilder(
                    dictionary: SlotDisplayDictData,
                ) : SlotDisplayData.PlaceholderBuilder(dictionary) {
                    private var preprocess: Preprocess? = null

                    // 用于在原始字符串被序列化成 Component 之前,
                    // 对原始字符串进行预处理, 例如替换某些标签.
                    //
                    // 只能调用一次! 调用多次将覆盖之前的数据.
                    fun preprocess(dsl: Preprocess.Builder.() -> Unit) {
                        if (preprocess == null) {
                            preprocess = Preprocess.build(dsl)
                        }
                    }

                    data class Result(
                        val resolver: TagResolver, val preprocess: Preprocess,
                    )

                    @ApiStatus.Internal
                    fun buildPreprocessResult(): Result {
                        return Result(build(), preprocess ?: Preprocess.NO_OPERATION)
                    }

                    class Preprocess private constructor(
                        private val replacement: Map<String, String> = mutableMapOf(),
                    ) {
                        companion object {
                            val NO_OPERATION = Preprocess(emptyMap())

                            @ApiStatus.Internal
                            fun build(dsl: Builder.() -> Unit): Preprocess {
                                return Builder().apply(dsl).build()
                            }
                        }

                        fun applyTo(source: String): String {
                            return replacement.entries.fold(source) { acc, (oldTag, newTag) ->
                                acc.replace("<$oldTag>", "<$newTag>")
                            }
                        }

                        @SlotDisplayDataDsl
                        class Builder {
                            private val replacement: MutableMap<String, String> = mutableMapOf()

                            fun replace(oldTag: String, newTag: String) {
                                replacement[oldTag] = newTag
                            }

                            @ApiStatus.Internal
                            fun build(): Preprocess {
                                return Preprocess(replacement)
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 代表 [SlotDisplayLoreData] 中的配置文件, 将作为参数传入 [SlotDisplayLoreData.resolve].
     */
    class LineConfig(
        private val placeholders: TagResolver,
        private val foldedLineMap: Map<String, List<Component>>,
    ) {

        companion object {
            fun build(dict: SlotDisplayDictData = SlotDisplayDictData(), dsl: Builder.() -> Unit): LineConfig {
                return Builder(dict).apply(dsl).build()
            }
        }

        fun getPlaceholders(): TagResolver {
            return placeholders
        }

        fun getFoldedLines(tag: String): List<Component>? {
            return foldedLineMap[tag]
        }

        /**
         * ### 注意事项!
         * 使用上必须让 [standard] 先于 [folded] 调用,
         * 否则 [folded] 不会使用全部的 [TagResolver].
         */
        @SlotDisplayDataDsl
        class Builder(
            private val dictionary: SlotDisplayDictData,
        ) {
            private var placeholders: TagResolver? = null
            private val placeholderBuilder: TagResolver.Builder = TagResolver.builder()
            private val foldedLineMap: MutableMap<String, List<Component>> = mutableMapOf()

            private fun freezePlaceholders() {
                if (placeholders == null) {
                    placeholders = placeholderBuilder.build()
                }
            }

            fun dict(key: String): String {
                // 开发日记 2024/12/25: 返回空字符串?
                return dictionary[key] ?: error("No such key in dict: $key")
            }

            // 使用该函数以 DSL 的形式添加 placeholders.
            // DSL 可以快速使用 SlotDisplayDict 中的映射.
            fun standard(dsl: SlotDisplayData.PlaceholderBuilder.() -> Unit) {
                placeholderBuilder.resolver(SlotDisplayData.PlaceholderBuilder(dictionary).apply(dsl).build())
            }

            // 使用该函数来直接添加折叠的占位符.
            fun folded(key: String, lines: List<Component>) {
                foldedLineMap[key] = lines
            }

            // 使用该函数来直接添加折叠的占位符.
            fun folded(key: String, vararg lines: Component) {
                foldedLineMap[key] = lines.toList()
            }

            // 使用该函数以 DSL 的形式添加折叠的占位符.
            // DSL 可快速使用 SlotDisplayDict 中的映射.
            // TODO: 如果在 build 之前就调用了这个函数, 那么最终的 Global Placeholder 相当于提前构建了.
            //  试试通过代码的方式来强制实行以下限制:
            //  1. 要求 standard 函数必须先于所有 folded 函数调用
            //  2. 如果在 build 之前就调用了 folded, 应该给出提示?
            fun folded(key: String, dsl: Line.Folded.Builder.() -> Unit) {
                freezePlaceholders()
                foldedLineMap[key] = Line.Folded.Builder(dictionary, placeholders!!).apply(dsl).build()
            }

            @ApiStatus.Internal
            fun build(): LineConfig {
                freezePlaceholders()
                return LineConfig(placeholders!!, foldedLineMap)
            }
        }
    }

    @SlotDisplayDataDsl
    class Builder {
        private val lines: MutableList<Line> = mutableListOf()

        fun standard(rawText: String) {
            lines.add(Line.Standard(rawText))
        }

        fun folded(key: String, rawText: String) {
            lines.add(Line.Folded(key, rawText))
        }

        @ApiStatus.Internal
        fun build(): SlotDisplayLoreData {
            return SlotDisplayLoreData(lines)
        }
    }
}

internal object SlotDisplayLoreDataSerializer : SimpleSerializer<SlotDisplayLoreData> {

    override fun deserialize(type: Type, node: ConfigurationNode): SlotDisplayLoreData {
        val rawTextList = node.getList<String>(emptyList())
        val resultLines = mutableListOf<SlotDisplayLoreData.Line>()
        for (rawText in rawTextList) {
            // 找出字符串中匹配 {...} 的内容, 但不包括被转义的 \{...\}
            val foldedRegex = """(?<!\\)\{([a-z0-9_]+)}""".toRegex()
            val foldedMatches = foldedRegex.findAll(rawText)
            // 一行只允许匹配一个折叠的占位符, 否则抛出异常
            if (foldedMatches.count() > 1) {
                LOGGER.error("Only one folded tag is allowed in a line of ${SlotDisplayLoreData::class.simpleName}! Treating it as a standard line. Line: \"$rawText\"")
                resultLines.add(SlotDisplayLoreData.Line.Standard(rawText))
                continue
            }
            // 如果匹配到了折叠的占位符, 则将其提取出来
            val foldedMatch = foldedMatches.firstOrNull()
            if (foldedMatch != null) {
                val key = foldedMatch.groupValues[1]
                // 再把 {} 替换成 <> 以便之后解析为 Tag;
                // 转义过的 {} 不会被替换!
                val foldedText = foldedMatch.value.replace("{", "<").replace("}", ">")
                val rawText2 = rawText.replace(foldedMatch.value, foldedText)
                resultLines.add(SlotDisplayLoreData.Line.Folded(key, rawText2))
            } else {
                resultLines.add(SlotDisplayLoreData.Line.Standard(rawText))
            }
        }
        return SlotDisplayLoreData(resultLines)
    }

}
