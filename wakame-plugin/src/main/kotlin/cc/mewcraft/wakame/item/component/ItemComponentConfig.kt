package cc.mewcraft.wakame.item.component

import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.config.derive
import cc.mewcraft.wakame.config.entry
import cc.mewcraft.wakame.item.binary.meta.ItemMetaSupport
import cc.mewcraft.wakame.registry.ItemComponentRegistry
import cc.mewcraft.wakame.util.toSimpleString
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import net.kyori.adventure.extra.kotlin.join
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.component
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import java.util.stream.Stream

abstract class ItemComponentConfig(
    path: String,
) {
    val config: ConfigProvider by lazy { ItemComponentRegistry.CONFIG.derive(path) }

    val enabled: Boolean by config.entry<Boolean>("enabled")
    val displayName: Component by config.entry<Component>("display_name")
    val showInTooltip: Boolean by config.entry<Boolean>("show_in_tooltip")

    // 根据具体的物品组件的配置结构, 实例化相应的 inner class

    /**
     * Tooltips for single text.
     *
     * @property single The format of the single text.
     */
    inner class SingleTooltip : Examinable {
        val single: String by config.entry<String>("tooltips", "single")

        fun render(): Component {
            return ItemComponentInjections.mini.deserialize(single)
        }

        fun render(resolver: TagResolver): Component {
            return ItemComponentInjections.mini.deserialize(single, resolver)
        }

        fun render(vararg resolver: TagResolver): Component {
            return ItemComponentInjections.mini.deserialize(single, *resolver)
        }

        override fun examinableProperties(): Stream<out ExaminableProperty> {
            return Stream.of(ExaminableProperty.of("single", single))
        }

        override fun toString(): String {
            return toSimpleString()
        }
    }

    /**
     * Tooltips for merged text.
     *
     * @property merged The format of all elements joined together.
     * @property single The format of a single element.
     * @property separator The format of the separator.
     */
    inner class MergedTooltip : Examinable {
        val merged: String by config.entry<String>("tooltips", "merged")
        val single: String by config.entry<String>("tooltips", "single")
        val separator: String by config.entry<String>("tooltips", "separator")

        /**
         * A convenience function to stylize a list of objects.
         */
        fun <T> render(
            collection: Collection<T>,
            extractor: (T) -> Component,
        ): List<Component> {
            return collection
                .mapTo(ObjectArrayList(collection.size)) {
                    ItemComponentInjections.mini.deserialize(single, component("single", extractor(it)))
                }
                .join(
                    JoinConfiguration.separator(ItemComponentInjections.mini.deserialize(separator))
                )
                .let {
                    ItemMetaSupport.mini().deserialize(merged, component("merged", it))
                }
                .let(::listOf)
        }

        override fun examinableProperties(): Stream<out ExaminableProperty> {
            return Stream.of(
                ExaminableProperty.of("merged", merged),
                ExaminableProperty.of("single", single),
                ExaminableProperty.of("separator", separator)
            )
        }

        override fun toString(): String {
            return toSimpleString()
        }
    }

    /**
     * Tooltips for lore.
     *
     * @property line The format of a single line.
     * @property header The header list. `null` indicates not adding the header.
     * @property bottom The bottom list. `null` indicates not adding the bottom.
     */
    inner class LoreTooltip : Examinable {
        val line: String by config.entry<String>("tooltips", "line")
        val header: List<String>? by config.entry<List<String>>("tooltips", "header")
        val bottom: List<String>? by config.entry<List<String>>("tooltips", "bottom")

        override fun examinableProperties(): Stream<out ExaminableProperty> {
            return Stream.of(
                ExaminableProperty.of("line", line),
                ExaminableProperty.of("header", header),
                ExaminableProperty.of("bottom", bottom)
            )
        }

        override fun toString(): String {
            return toSimpleString()
        }
    }
}