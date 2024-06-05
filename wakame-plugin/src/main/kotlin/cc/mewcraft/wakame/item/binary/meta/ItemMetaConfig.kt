package cc.mewcraft.wakame.item.binary.meta

import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.config.derive
import cc.mewcraft.wakame.config.entry
import cc.mewcraft.wakame.registry.ItemMetaRegistry
import cc.mewcraft.wakame.util.toSimpleString
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import net.kyori.adventure.extra.kotlin.join
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.component
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import java.util.stream.Stream

internal sealed class ItemMetaConfig(
    path: String,
) {
    val config: ConfigProvider by lazy { ItemMetaRegistry.CONFIG.derive(path) }

    /**
     * Tooltips for single text.
     *
     * @property single The format of the single text.
     */
    inner class SingleTooltips : Examinable {
        val single: String by config.entry("tooltips", "single")

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
    inner class MergedTooltips : Examinable {
        val merged: String by config.entry("tooltips", "merged")
        val single: String by config.entry("tooltips", "single")
        val separator: String by config.entry("tooltips", "separator")

        /**
         * A convenience function to stylize a list of objects.
         */
        inline fun <T> render(
            collection: Collection<T>,
            extractor: (T) -> Component,
        ): List<Component> {
            val merged = collection
                .mapTo(ObjectArrayList(collection.size)) { ItemMetaSupport.mini().deserialize(single, component("single", extractor(it))) }
                .join(JoinConfiguration.separator(ItemMetaSupport.mini().deserialize(separator)))
                .let { ItemMetaSupport.mini().deserialize(merged, component("merged", it)) }
            return listOf(merged)
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
    inner class LoreTooltips : Examinable {
        val line: String by config.entry("tooltips", "line")
        val header: List<String>? by config.entry("tooltips", "header")
        val bottom: List<String>? by config.entry("tooltips", "bottom")

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
