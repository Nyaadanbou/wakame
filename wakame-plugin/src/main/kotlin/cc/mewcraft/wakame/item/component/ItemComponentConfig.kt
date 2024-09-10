package cc.mewcraft.wakame.item.component

import cc.mewcraft.commons.provider.Provider
import cc.mewcraft.commons.provider.immutable.orElse
import cc.mewcraft.wakame.config.*
import cc.mewcraft.wakame.display2.RendererSystemName
import cc.mewcraft.wakame.util.toSimpleString
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import net.kyori.adventure.extra.kotlin.join
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.component
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.koin.core.component.KoinComponent
import java.util.concurrent.ConcurrentHashMap
import java.util.stream.Stream

/**
 * 封装了一个物品组件的配置文件.
 */
internal class ItemComponentConfig
private constructor(
    private val configPath: String,
) : KoinComponent {

    companion object {
        /**
         * 获取一个 [ItemComponentConfig] 实例.
         *
         * 用同一个 [configKey] 多次调用本函数返回的都是同一个实例.
         *
         * @param configKey 该组件在配置文件中的路径
         */
        fun provide(configKey: String): ItemComponentConfig {
            return objectPool.computeIfAbsent(configKey) {
                ItemComponentConfig(it)
            }
        }

        private val objectPool = ConcurrentHashMap<String, ItemComponentConfig>()
    }

    /**
     * 根配置文件.
     */
    val root by lazy { ItemComponentRegistry.CONFIG.derive(configPath) }

    /**
     * 该组件是否启用? (具体的作用之后再逐渐完善)
     */
    val enabled by root.entry<Boolean>("enabled")

    /**
     * 该组件的显示名字.
     */
    val displayName by root.entry<Component>("display_name")

    // inner class 使用说明:
    // 根据具体的物品组件的配置结构,
    // 实例化相应的 inner class.

    /**
     * Tooltips for single text.
     *
     * @property single The format of the single text.
     */
    inner class SingleTooltip : Examinable {
        private val singles = Object2ObjectArrayMap<RendererSystemName, Provider<String?>>()

        fun render(systemName: RendererSystemName): Component? {
            val single = single(systemName) ?: return null
            return ItemComponentInjections.miniMessage.deserialize(single)
        }

        fun render(systemName: RendererSystemName, resolver: TagResolver): Component? {
            val single = single(systemName) ?: return null
            return ItemComponentInjections.miniMessage.deserialize(single, resolver)
        }

        fun render(systemName: RendererSystemName, vararg resolver: TagResolver): Component? {
            val single = single(systemName) ?: return null
            return ItemComponentInjections.miniMessage.deserialize(single, *resolver)
        }

        private fun single(systemName: RendererSystemName): String? {
            return singles.computeIfAbsent(systemName) { name: RendererSystemName ->
                val provider = ItemComponentRegistry.getDescriptorsByRendererSystemName(name).derive(configPath)
                provider.optionalEntry<String>("tooltip", "single")
                    .orElse(provider.optionalEntry<String>("tooltip"))
            }.get()
        }

        override fun examinableProperties(): Stream<out ExaminableProperty> {
            return Stream.of(ExaminableProperty.of("singles", singles))
        }

        override fun toString(): String {
            return toSimpleString()
        }
    }

    /**
     * Tooltips for merged text.
     */
    inner class MergedTooltip : Examinable {
        private val merges = Object2ObjectArrayMap<RendererSystemName, TooltipStrings>()

        /**
         * A convenience function to stylize a list of objects.
         */
        fun <T> render(
            systemName: RendererSystemName,
            collection: Collection<T>,
            extractor: (T) -> Component,
        ): List<Component>? {
            val (merged, single, separator) = tooltipStrings(systemName)
            if (merged == null || single == null || separator == null)
                return null
            return collection
                .mapTo(ObjectArrayList(collection.size)) {
                    ItemComponentInjections.miniMessage.deserialize(single, component("single", extractor(it)))
                }
                .join(
                    JoinConfiguration.separator(ItemComponentInjections.miniMessage.deserialize(separator))
                )
                .let {
                    ItemComponentInjections.miniMessage.deserialize(merged, component("merged", it))
                }
                .let(::listOf)
        }

        private inner class TooltipStrings(
            merged: Provider<String?>,
            single: Provider<String?>,
            separator: Provider<String?>,
        ) {
            operator fun component1(): String? {
                return merged
            }

            operator fun component2(): String? {
                return single
            }

            operator fun component3(): String? {
                return separator
            }

            val merged: String? by merged
            val single: String? by single
            val separator: String? by separator
        }

        private fun tooltipStrings(systemName: RendererSystemName): TooltipStrings {
            return merges.computeIfAbsent(systemName) { name: RendererSystemName ->
                val provider = ItemComponentRegistry.getDescriptorsByRendererSystemName(name).derive(configPath)
                TooltipStrings(
                    provider.optionalEntry<String>("tooltip", "merged"),
                    provider.optionalEntry<String>("tooltip", "single"),
                    provider.optionalEntry<String>("tooltip", "separator")
                )
            }
        }

        override fun examinableProperties(): Stream<out ExaminableProperty> {
            return Stream.of(
                ExaminableProperty.of("merges", merges)
            )
        }

        override fun toString(): String {
            return toSimpleString()
        }
    }

    /**
     * Tooltips for lore.
     */
    inner class LoreTooltip : Examinable {
        private val lores = Object2ObjectArrayMap<RendererSystemName, TooltipStrings>()

        inner class TooltipStrings(
            line: Provider<String>,
            header: Provider<List<String>>,
            bottom: Provider<List<String>>,
        ) {
            operator fun component1(): String {
                return line
            }

            operator fun component2(): List<String> {
                return header
            }

            operator fun component3(): List<String> {
                return bottom
            }

            val line: String by line
            val header: List<String> by header
            val bottom: List<String> by bottom
        }

        fun tooltipStrings(systemName: RendererSystemName): TooltipStrings {
            return lores.computeIfAbsent(systemName) { name: RendererSystemName ->
                val provider = ItemComponentRegistry.getDescriptorsByRendererSystemName(name).derive(configPath)
                TooltipStrings(
                    provider.optionalEntry<String>("tooltip", "line").orElse(""),
                    provider.optionalEntry<List<String>>("tooltip", "header").orElse(emptyList()),
                    provider.optionalEntry<List<String>>("tooltip", "bottom").orElse(emptyList())
                )
            }
        }

        override fun examinableProperties(): Stream<out ExaminableProperty> {
            return Stream.of(
                ExaminableProperty.of("lores", lores)
            )
        }

        override fun toString(): String {
            return toSimpleString()
        }
    }
}