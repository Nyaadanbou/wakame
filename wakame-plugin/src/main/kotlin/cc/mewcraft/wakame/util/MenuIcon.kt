package cc.mewcraft.wakame.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.StyleBuilderApplicable
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

@DslMarker
annotation class MenuIconDsl

/**
 * A mark interface.
 */
interface MenuIcon {

    @MenuIconDsl
    open class PlaceholderTagResolverBuilder(
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

        /**
         * 构建.
         */
        fun build(): TagResolver {
            return tagResolverBuilder.build()
        }
    }

}