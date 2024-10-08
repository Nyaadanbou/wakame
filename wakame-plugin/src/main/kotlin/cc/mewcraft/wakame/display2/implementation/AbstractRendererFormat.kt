package cc.mewcraft.wakame.display2.implementation

import cc.mewcraft.commons.collections.takeUnlessEmpty
import cc.mewcraft.wakame.display2.RendererFormat
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import net.kyori.adventure.extra.kotlin.join
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.*
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.enchantments.Enchantment
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import kotlin.collections.component1
import kotlin.collections.component2

internal abstract class AbstractRendererFormat : RendererFormat {

}

/**
 * 一种最简单的渲染格式.
 *
 * 只有一个格式 [single], 里面可包含多个占位符.
 * 当生成最终内容时, 只需要进行一次替换即可.
 *
 * @param namespace 命名空间
 * @param id
 * @param single 内容的格式, 可能包含多个占位符
 */
internal class SingleValueRendererFormat(
    override val namespace: String,
    id: String,
    private val single: String, // mini
) : RendererFormat.Simple, KoinComponent {
    override val index: Key = Key.key(namespace, id)
    private val miniMessage = get<MiniMessage>()

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
 * @param namespace
 * @param id
 * @param merged 合并后的字符串的格式
 * @param single 单个对象的字符串的格式
 * @param separator 分隔符
 */
internal class AggregateValueRendererFormat(
    override val namespace: String,
    id: String,
    private val merged: String,
    private val single: String,
    private val separator: String,
) : RendererFormat.Simple, KoinComponent {
    override val index: Key = Key.key(namespace, id)
    private val miniMessage = get<MiniMessage>()

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
 * @param namespace
 * @param line 每一行内容的格式, 可用的占位符 `<line>`
 * @param header 描述的头部文本, 没有可用的占位符
 * @param bottom 描述的底部文本, 没有可用的占位符
 */
internal class ExtraLoreRendererFormat(
    override val namespace: String,
    private val line: String,
    private val header: List<String>,
    private val bottom: List<String>,
) : RendererFormat.Simple, KoinComponent {
    override val index: Key = Key.key(namespace, "lore")
    private val miniMessage = get<MiniMessage>()

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

/**
 * 一种专用于物品魔咒 (enchantments) 的渲染格式.
 */
internal class EnchantmentRendererFormat(
    override val namespace: String,
) : RendererFormat.Simple, KoinComponent {
    override val index: Key = Key.key(namespace, "enchantments")

    fun render(enchantments: Map<Enchantment, Int>): List<Component> {
        return enchantments.map { (enchantment, level) -> enchantment.displayName(level) }
    }
}