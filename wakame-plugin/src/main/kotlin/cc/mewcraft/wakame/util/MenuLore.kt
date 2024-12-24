package cc.mewcraft.wakame.util

import cc.mewcraft.wakame.Injector
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.koin.core.component.get

data class MenuLore(
    private val lines: List<Line>,
) {

    companion object {
        private val MM = Injector.get<MiniMessage>()
    }

    fun resolve(config: LineConfig): List<Component> {
        return lines.flatMap { line ->
            when (line) {
                is Line.Standard -> {
                    val rawText = line.rawText
                    val tagResolver = config.getTagResolver()
                    val component = MM.deserialize(rawText, tagResolver)
                    listOf(component)
                }

                is Line.Folded -> {
                    val key = line.key
                    val rawText = line.rawText
                    val folded = config.getFoldedText(key) ?: return@flatMap emptyList()
                    val tagResolver = config.getTagResolver()
                    folded.map { line ->
                        val tagResolver = TagResolver.resolver(tagResolver, Placeholder.component(key, line))
                        MM.deserialize(rawText, tagResolver)
                    }
                }
            }
        }
    }

    fun resolve(configDSL: LineConfigBuilder.() -> Unit): List<Component> {
        val config = LineConfigBuilder().apply(configDSL).build()
        return resolve(config)
    }

    /**
     * 代表 [MenuLore] 中的一行内容.
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
         */
        class Standard(rawText: String) : Line(rawText)

        /**
         * 包含折叠的占位符, 也可以包含普通的 [Tag].
         * 解析后变成一个 [Component] 的列表, 替换当前行为多行内容.
         *
         * 例如在配置文件中:
         * ```yaml
         * - "*prefix* <choice_list>"
         * ```
         *
         * 然后这个 <choice_list> 有3个 [Component], 那么解析后就会变成:
         * ```yaml
         * - "*prefix* abc"
         * - "*prefix* def"
         * - "*prefix* ghi"
         * ```
         */
        class Folded(val key: String, rawText: String) : Line(rawText)
    }

    /**
     * 代表 [MenuLore] 中的配置文件, 将作为参数传入 [MenuLore.resolve].
     */
    class LineConfig(
        private val tagResolverList: TagResolver,
        private val foldedLineMap: Map<String, List<Component>>,
    ) {
        fun getTagResolver(): TagResolver {
            return tagResolverList
        }

        fun getFoldedText(tag: String): List<Component>? {
            return foldedLineMap[tag]
        }
    }

}

@DslMarker
annotation class MenuLoreDsl

@MenuLoreDsl
class MenuLoreBuilder {
    private val lines = mutableListOf<MenuLore.Line>()

    fun standard(rawText: String) {
        lines.add(MenuLore.Line.Standard(rawText))
    }

    fun folded(key: String, rawText: String) {
        lines.add(MenuLore.Line.Folded(key, rawText))
    }

    fun build(): MenuLore = MenuLore(lines)
}

fun menuLore(init: MenuLoreBuilder.() -> Unit): MenuLore {
    return MenuLoreBuilder().apply(init).build()
}

@MenuLoreDsl
class LineConfigBuilder {
    private val tagResolverList = mutableListOf<TagResolver>()
    private val foldedLineMap = mutableMapOf<String, List<Component>>()

    fun standard(resolver: TagResolver) {
        tagResolverList.add(resolver)
    }

    fun folded(key: String, lines: List<Component>) {
        foldedLineMap[key] = lines
    }

    fun build(): MenuLore.LineConfig = MenuLore.LineConfig(TagResolver.resolver(tagResolverList), foldedLineMap)
}

fun lineConfig(init: LineConfigBuilder.() -> Unit): MenuLore.LineConfig {
    return LineConfigBuilder().apply(init).build()
}
