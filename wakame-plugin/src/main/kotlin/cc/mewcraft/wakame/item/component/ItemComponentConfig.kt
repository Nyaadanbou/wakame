package cc.mewcraft.wakame.item.component

import cc.mewcraft.commons.provider.immutable.map
import cc.mewcraft.wakame.config.derive
import cc.mewcraft.wakame.config.entry
import cc.mewcraft.wakame.display.RendererConfigReloadEvent
import cc.mewcraft.wakame.display.TooltipKey
import cc.mewcraft.wakame.eventbus.PluginEventBus
import cc.mewcraft.wakame.eventbus.subscribe
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
import org.koin.core.component.KoinComponent
import java.util.concurrent.ConcurrentHashMap
import java.util.stream.Stream

/**
 * 封装了一个物品组件的配置文件.
 */
internal class ItemComponentConfig
private constructor(
    private val configPath: String,
    private val tooltipKey: TooltipKey,
) : KoinComponent {

    companion object {
        /**
         * 获取一个 [ItemComponentConfig] 实例.
         *
         * 用同一个 [component] 多次调用本函数返回的都是同一个实例.
         *
         * @param component 物品组件
         */
        fun provide(component: ItemComponentMeta): ItemComponentConfig {
            return provide(component.configPath, component.tooltipKey)
        }

        /**
         * 获取一个 [ItemComponentConfig] 实例.
         *
         * 用同一个 [configPath] 多次调用本函数返回的都是同一个实例.
         *
         * @param configPath 该组件在配置文件中的路径
         * @param tooltipKey 该组件在提示框中的标识
         */
        fun provide(configPath: String, tooltipKey: TooltipKey): ItemComponentConfig {
            return objectPool.computeIfAbsent(configPath) {
                ItemComponentConfig(it, tooltipKey)
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
     * 该组件的展示名字.
     */
    val displayName by root.entry<Component>("display_name")

    /**
     * 该组件是否显示在提示框中.
     */
    var showInTooltip = false
        private set

    // inner class 使用说明:
    // 根据具体的物品组件的配置结构,
    // 实例化相应的 inner class.

    /**
     * Tooltips for discrete values.
     *
     * @property mappings the format of each discrete value.
     */
    inner class DiscreteTooltips : Examinable {
        val single: String by root.entry<String>("tooltips", "single")
        val mappings: Map<Int, Component> by root
            .entry<Map<Int, String>>("mappings")
            .map { map ->
                map.withDefault { int ->
                    "??? ($int)" // fallback for unknown discrete values
                }.mapValues { (_, v) ->
                    ItemComponentInjections.miniMessage.deserialize(v)
                }
            }

        fun render(value: Int): Component {
            return ItemComponentInjections.miniMessage.deserialize(single, component("value", mappings.getValue(value)))
        }
    }

    /**
     * Tooltips for single text.
     *
     * @property single The format of the single text.
     */
    inner class SingleTooltip : Examinable {
        val single: String by root.entry<String>("tooltips", "single")

        fun render(): Component {
            return ItemComponentInjections.miniMessage.deserialize(single)
        }

        fun render(resolver: TagResolver): Component {
            return ItemComponentInjections.miniMessage.deserialize(single, resolver)
        }

        fun render(vararg resolver: TagResolver): Component {
            return ItemComponentInjections.miniMessage.deserialize(single, *resolver)
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
        val merged: String by root.entry<String>("tooltips", "merged")
        val single: String by root.entry<String>("tooltips", "single")
        val separator: String by root.entry<String>("tooltips", "separator")

        /**
         * A convenience function to stylize a list of objects.
         */
        fun <T> render(
            collection: Collection<T>,
            extractor: (T) -> Component,
        ): List<Component> {
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
     * @property header The header list.
     * @property bottom The bottom list.
     */
    inner class LoreTooltip : Examinable {
        val line: String by root.entry<String>("tooltips", "line")
        val header: List<String> by root.entry<List<String>>("tooltips", "header")
        val bottom: List<String> by root.entry<List<String>>("tooltips", "bottom")

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

    init {
        // 订阅事件: 当 renderer config 重载时刷新 showInTooltip 的值
        PluginEventBus.get().subscribe<RendererConfigReloadEvent> {
            showInTooltip = tooltipKey in it.rawTooltipKeys
        }
    }
}