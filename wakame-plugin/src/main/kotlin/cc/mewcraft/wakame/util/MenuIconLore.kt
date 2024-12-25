package cc.mewcraft.wakame.util

import cc.mewcraft.wakame.Injector
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.config.configurate.TypeSerializer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.StyleBuilderApplicable
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.getList
import java.lang.reflect.Type

/**
 * 代表一个专用于菜单图标的 `minecraft:lore`, 提供方便的占位符解析功能.
 *
 * ### 实例化方式
 * - 从配置文件构建: 使用配套的 [MenuIconLoreSerializer] 来反序列化.
 * - 从代码直接构建: 使用静态函数 [MenuIconLore.build].
 */
data class MenuIconLore(
    private val lines: List<Line>,
) {

    constructor(builder: MainBuilder) : this(builder.build().lines)

    companion object {
        private val MM = Injector.get<MiniMessage>()

        fun build(init: MainBuilder.() -> Unit): MenuIconLore {
            return MenuIconLore(MainBuilder().apply(init))
        }
    }

    /**
     * 解析 [MenuIconLore] 中的所有行, 返回一个 [Component] 的列表.
     *
     * @param config 解析的配置
     * @return 解析后的 [Component] 列表
     */
    fun resolve(config: LineConfig): List<Component> {
        return lines.flatMap { line ->
            when (line) {
                is Line.Standard -> {
                    val rawText = line.rawText
                    val tagResolver = config.getTagResolver()
                    val component = MM.deserialize(rawText, tagResolver)
                    val result = listOf(component)
                    result
                }

                is Line.Folded -> {
                    val key = line.key
                    val rawText = line.rawText
                    val folded = config.getFoldedText(key) ?: return@flatMap emptyList()
                    val tagResolver = config.getTagResolver()
                    val result = folded.map { line -> MM.deserialize(rawText, TagResolver.resolver(tagResolver, Placeholder.component(key, line))) }
                    result
                }
            }
        }
    }

    /**
     * 解析 [MenuIconLore] 中的所有行, 返回一个 [Component] 的列表.
     *
     * @param dict 占位符的映射表
     * @param dsl 解析的配置, 通过 DSL 构建
     * @return 解析后的 [Component] 列表
     */
    fun resolve(dict: MenuIconDictionary = MenuIconDictionary(), dsl: LineConfigBuilder.() -> Unit): List<Component> {
        return resolve(LineConfigBuilder(dict).apply(dsl).build())
    }

    /**
     * 储存 [MenuIconLore] 中的一行原始的字符串.
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
         * 包含最多1个折叠的占位符, 也可以包含0或多个普通的 [Tag].
         * 解析后变成一个 [Component] 的列表, 替换当前行为多行内容.
         *
         * 例如在配置文件中:
         * ```yaml
         * - "*any prefix* <choice_list>" # <choice_list> 是一个折叠的占位符
         * ```
         *
         * 然后这个 <choice_list> 有3个 [Component] 分别是 "abc", "def", "ghi", 那么解析后就会变成:
         * ```yaml
         * - "*any prefix* abc"
         * - "*any prefix* def"
         * - "*any prefix* ghi"
         * ```
         *
         * @param key 折叠的占位符的标识符, 用于在 [LineConfig] 中查找对应的 [Component] 列表
         * @param rawText 用于解析的文本, 必须包含一个占位符 [key], 可以包含若干普通的 [Tag]
         */
        class Folded(val key: String, rawText: String) : Line(rawText)
    }

    /**
     * 代表 [MenuIconLore] 中的配置文件, 将作为参数传入 [MenuIconLore.resolve].
     */
    class LineConfig(
        private val globalTagResolver: TagResolver,
        private val foldedLineCreatorMap: Map<String, List<Component>>,
    ) {

        companion object {
            fun build(dict: MenuIconDictionary = MenuIconDictionary(), dsl: LineConfigBuilder.() -> Unit): LineConfig {
                return LineConfigBuilder(dict).apply(dsl).build()
            }
        }

        fun getTagResolver(): TagResolver {
            return globalTagResolver
        }

        fun getFoldedText(tag: String): List<Component>? {
            return foldedLineCreatorMap[tag]
        }
    }

    @DslMarker
    annotation class MenuIconLoreDsl

    @MenuIconLoreDsl
    class MainBuilder {
        private val lines: MutableList<Line> = mutableListOf()

        fun standard(rawText: String) {
            lines.add(Line.Standard(rawText))
        }

        fun folded(key: String, rawText: String) {
            lines.add(Line.Folded(key, rawText))
        }

        fun build(): MenuIconLore {
            return MenuIconLore(lines)
        }
    }

    /**
     * 使用上必须让 [standard] 先于 [folded] 调用,
     * 否则 [folded] 不会使用全部的 [TagResolver].
     */
    @MenuIconLoreDsl
    class LineConfigBuilder(
        private val dictionary: MenuIconDictionary,
    ) {
        private var tagResolver: TagResolver? = null
        private val tagResolverBuilder: TagResolver.Builder = TagResolver.builder()
        private val foldedLineMap: MutableMap<String, List<Component>> = mutableMapOf()

        fun dict(key: String): String {
            // 开发日记 2024/12/25: 返回空字符串?
            return dictionary[key] ?: error("no such key in dict: $key")
        }

        // 使用该函数来直接添加 TagResolver.
        fun standard(resolver: TagResolver) {
            tagResolverBuilder.resolver(resolver)
        }

        // 使用该函数以 DSL 的形式添加 TagResolver.
        // DSL 可方便使用 MenuIconDict 中的映射.
        fun standard(dsl: PlaceholderTagResolverBuilder.() -> Unit) {
            tagResolverBuilder.resolver(PlaceholderTagResolverBuilder(dictionary).apply(dsl).build())
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
        // DSL 可方便使用 MenuIconDict 中的映射.
        // TODO: 如果在 build 之前就调用了这个函数, 那么最终的 Global TagResolver 相当于提前构建了.
        //  试试通过代码的方式来强制实行以下限制:
        //  1. 要求 standard 必须先于所有 folded 调用
        //  2. 如果在 build 之前就调用了 folded, 应该给出一些提示?
        fun folded(key: String, dsl: FoldedLineBuilder.() -> Unit) {
            if (tagResolver == null) {
                tagResolver = tagResolverBuilder.build()
            }
            foldedLineMap[key] = FoldedLineBuilder(dictionary, tagResolver!!).apply(dsl).build()
        }

        fun build(): LineConfig {
            if (tagResolver == null) {
                tagResolver = tagResolverBuilder.build()
            }
            return LineConfig(tagResolver!!, foldedLineMap)
        }
    }

    @MenuIconLoreDsl
    class FoldedLineBuilder(
        private val dictionary: MenuIconDictionary,
        private val globalTagResolver: TagResolver,
    ) {
        private val foldedLineList: MutableList<Component> = mutableListOf()

        fun dict(key: String): String {
            // 开发日记 2024/12/25: 返回空字符串?
            return dictionary[key] ?: error("no such key in dict: $key")
        }

        fun literal(text: String) {
            foldedLineList.add(MM.deserialize(text, globalTagResolver))
        }

        fun literal(component: Component) {
            foldedLineList.add(component)
        }

        fun resolve(key: String, dsl: PlaceholderTagResolverBuilder.() -> Unit) {
            val rawText = dict(key)
            val resolver = PlaceholderTagResolverBuilder(dictionary).apply(dsl).build() // local resolver
            val parsed = MM.deserialize(rawText, resolver, globalTagResolver)
            foldedLineList.add(parsed)
        }

        fun build(): List<Component> {
            return foldedLineList
        }
    }

    @MenuIconLoreDsl
    class PlaceholderTagResolverBuilder(
        private val dictionary: MenuIconDictionary,
    ) {
        private val tagResolverBuilder: TagResolver.Builder = TagResolver.builder()

        fun dict(key: String): String {
            // 开发日记 2024/12/25: 返回空字符串?
            return dictionary[key] ?: error("no such key in dict: $key")
        }

        /**
         * @see Placeholder.parsed
         */
        fun parsed(key: String, value: String) {
            tagResolverBuilder.resolver(Placeholder.parsed(key, value))
        }

        /**
         * @see Placeholder.unparsed
         */
        fun unparsed(key: String, value: String) {
            tagResolverBuilder.resolver(Placeholder.unparsed(key, value))
        }

        /**
         * @see Placeholder.component
         */
        fun component(key: String, value: Component) {
            tagResolverBuilder.resolver(Placeholder.component(key, value))
        }

        /**
         * @see Placeholder.styling
         */
        fun styling(key: String, vararg value: StyleBuilderApplicable) {
            tagResolverBuilder.resolver(Placeholder.styling(key, *value))
        }

        fun build(): TagResolver {
            return tagResolverBuilder.build()
        }
    }
}

object MenuIconLoreSerializer : TypeSerializer<MenuIconLore> {
    override fun deserialize(type: Type, node: ConfigurationNode): MenuIconLore {
        val rawTextList = node.getList<String>(emptyList())
        val resultLines = mutableListOf<MenuIconLore.Line>()
        for (rawText in rawTextList) {
            // 找出字符串中匹配 {...} 的内容, 但不包括被转义的 \{...\}
            val foldedRegex = """(?<!\\)\{([a-z0-9_]+)}""".toRegex()
            val foldedMatches = foldedRegex.findAll(rawText)
            // 一行只允许匹配一个折叠的占位符, 否则抛出异常
            if (foldedMatches.count() > 1) {
                LOGGER.error("Only one folded tag is allowed in a line of ${MenuIconLore::class.simpleName}! Treating it as a standard line. Line: \"$rawText\"")
                resultLines.add(MenuIconLore.Line.Standard(rawText))
                continue
            }
            // 如果匹配到了折叠的占位符, 则将其提取出来
            val foldedMatch = foldedMatches.firstOrNull()
            if (foldedMatch != null) {
                val key = foldedMatch.groupValues[1]
                // 再把 {} 替换成 <> 以便之后解析为 Tag;
                // 转义过的 {} 不会被替换!
                val foldedText = foldedMatch.value.replace("{", "<").replace("}", ">")
                val rawText = rawText.replace(foldedMatch.value, foldedText)
                resultLines.add(MenuIconLore.Line.Folded(key, rawText))
            } else {
                resultLines.add(MenuIconLore.Line.Standard(rawText))
            }
        }
        return MenuIconLore(resultLines)
    }
}
