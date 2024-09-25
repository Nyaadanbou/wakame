package cc.mewcraft.wakame.display2.implementation

import cc.mewcraft.commons.collections.takeUnlessEmpty
import cc.mewcraft.wakame.display2.DerivedTooltipIndex
import cc.mewcraft.wakame.display2.RendererFormat
import cc.mewcraft.wakame.display2.RendererFormats
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import net.kyori.adventure.extra.kotlin.join
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.component
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.getValue

internal abstract class AbstractRendererFormats : RendererFormats {
    /**
     * 设置指定的 [RendererFormat].
     */
    abstract fun <T : RendererFormat> set(id: String, format: T)
}

//<editor-fold desc="Concrete classes of RendererFormat">
/**
 * 一种最简单的渲染格式.
 *
 * 只有一个格式 [single], 里面可包含多个占位符.
 * 当生成最终内容时, 只需要进行一次替换即可.
 *
 * @param single 内容的格式, 可能包含多个占位符
 */
data class SingleValueRendererFormat(
    override val index: DerivedTooltipIndex,
    val single: String // mini
) : RendererFormat, KoinComponent {
    private val miniMessage: MiniMessage by inject()

    fun render(): Component {
        return miniMessage.deserialize(single)
    }

    fun render(resolver: TagResolver): Component {
        return miniMessage.deserialize(single, resolver)
    }

    fun render(vararg resolver: TagResolver): Component {
        return miniMessage.deserialize(single, *resolver)
    }
}

/**
 * 一种需要将多个对象合并成一个字符串的渲染格式.
 *
 * @param merged 合并后的字符串的格式
 * @param single 单个对象的字符串的格式
 * @param separator 分隔符
 */
data class CollectionRendererFormat(
    override val index: DerivedTooltipIndex,
    val merged: String,
    val single: String,
    val separator: String,
) : RendererFormat, KoinComponent {
    private val miniMessage: MiniMessage by inject()

    /**
     * A convenience function to stylize a list of objects.
     */
    fun <T> render(
        collection: Collection<T>,
        mapper: (T) -> Component,
    ): List<Component> {
        val merged = collection
            .mapTo(ObjectArrayList(collection.size)) { miniMessage.deserialize(single, component("single", mapper(it))) }
            .join(JoinConfiguration.separator(miniMessage.deserialize(separator)))
            .let { miniMessage.deserialize(merged, component("merged", it)) }
            .let(::listOf)
        return merged
    }
}

/**
 * 一种专用于额外的物品描述 (lore) 的渲染格式.
 *
 * @param line 每一行内容的格式, 可以有占位符
 * @param header 描述的头部文本, 不应该包含占位符
 * @param bottom 描述的底部文本, 不应该包含占位符
 */
data class ExtraLoreRendererFormat(
    override val index: DerivedTooltipIndex,
    val line: String,
    val header: List<String>,
    val bottom: List<String>,
) : RendererFormat, KoinComponent {
    private val miniMessage: MiniMessage by inject()

    fun render(lore: List<String>): List<Component> {
        val size = header.size + lore.size + bottom.size
        val lines = lore.mapTo(ObjectArrayList(size)) { miniMessage.deserialize(line, Placeholder.parsed("line", it)) }
        val header = header.takeUnlessEmpty()?.run { mapTo(ObjectArrayList(this.size), miniMessage::deserialize) }
        val bottom = bottom.takeUnlessEmpty()?.run { mapTo(ObjectArrayList(this.size), miniMessage::deserialize) }
        lines.addAll(0, header)
        lines.addAll(bottom)
        return lines
    }
}
//</editor-fold>