package cc.mewcraft.wakame.display2.implementation

import cc.mewcraft.commons.collections.takeUnlessEmpty
import cc.mewcraft.wakame.display2.*
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import net.kyori.adventure.extra.kotlin.join
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.*
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.enchantments.Enchantment
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Setting
import kotlin.collections.component1
import kotlin.collections.component2

/**
 * 一种最简单的渲染格式.
 *
 * 只有一个格式 [tooltip], 里面可包含多个占位符.
 * 当生成最终内容时, 只需要进行一次替换即可.
 *
 * @param namespace 命名空间
 * @param id
 * @param tooltip 内容的格式, 可能包含多个占位符
 */
@ConfigSerializable
internal data class SingleValueRendererFormat(
    @Setting override val namespace: String,
    private val id: String,
    @Setting private val tooltip: String, // mini
) : RendererFormat.Simple, KoinComponent {
    override val index: Key = Key.key(namespace, id)
    private val miniMessage = get<MiniMessage>()

    fun render(): IndexedText {
        return SimpleIndexedText(index, listOf(miniMessage.deserialize(tooltip)))
    }

    fun render(resolver: TagResolver): IndexedText {
        return SimpleIndexedText(index, listOf(miniMessage.deserialize(tooltip, resolver)))
    }

    fun render(vararg resolver: TagResolver): IndexedText {
        return SimpleIndexedText(index, listOf(miniMessage.deserialize(tooltip, *resolver)))
    }
}

/**
 * 一种需要将多个对象合并成一个字符串的渲染格式.
 *
 * @param namespace
 * @param id
 */
@ConfigSerializable
internal data class AggregateValueRendererFormat(
    @Setting override val namespace: String,
    private val id: String,
    @Setting private val tooltip: Tooltip,
) : RendererFormat.Simple, KoinComponent {
    override val index: Key = Key.key(namespace, id)
    private val miniMessage = get<MiniMessage>()

    /**
     * A convenience function to stylize a list of objects.
     */
    fun <T> render(
        collection: Collection<T>,
        transform: (T) -> Component,
    ): IndexedText {
        val merged = collection
            .mapTo(ObjectArrayList(collection.size)) { miniMessage.deserialize(tooltip.single, component("single", transform(it))) }
            .join(JoinConfiguration.separator(miniMessage.deserialize(tooltip.separator)))
            .let { miniMessage.deserialize(tooltip.merged, component("merged", it)) }
            .let(::listOf)
        return SimpleIndexedText(index, merged)
    }

    /**
     * @param merged 合并后的字符串的格式
     * @param single 单个对象的字符串的格式
     * @param separator 分隔符
     */
    @ConfigSerializable
    data class Tooltip(
        @Setting val single: String,
        @Setting val separator: String,
        @Setting val merged: String,
    )
}

/**
 * 一种专用于额外的物品描述 (lore) 的渲染格式.
 *
 * @param namespace
 */
@ConfigSerializable
internal class ExtraLoreRendererFormat(
    @Setting override val namespace: String,
    @Setting private val tooltip: Tooltip,
) : RendererFormat.Simple, KoinComponent {
    override val index: Key = Key.key(namespace, "lore")
    private val miniMessage = get<MiniMessage>()

    fun render(lore: List<String>): IndexedText {
        val size = tooltip.header.size + lore.size + tooltip.bottom.size
        val lines = lore.mapTo(ObjectArrayList(size)) { miniMessage.deserialize(tooltip.line, parsed("line", it)) }
        val header = tooltip.header.takeUnlessEmpty()?.run { mapTo(ObjectArrayList(this.size), miniMessage::deserialize) }
        val bottom = tooltip.bottom.takeUnlessEmpty()?.run { mapTo(ObjectArrayList(this.size), miniMessage::deserialize) }
        lines.addAll(0, header)
        lines.addAll(bottom)
        return SimpleIndexedText(index, lines)
    }

    /**
     * @param line 每一行内容的格式, 可用的占位符 `<line>`
     * @param header 描述的头部文本, 没有可用的占位符
     * @param bottom 描述的底部文本, 没有可用的占位符
     */
    @ConfigSerializable
    data class Tooltip(
        @Setting val line: String,
        @Setting val header: List<String>,
        @Setting val bottom: List<String>,
    )
}

/**
 * 一种专用于物品魔咒 (enchantments) 的渲染格式.
 */
@ConfigSerializable
internal class EnchantmentRendererFormat(
    @Setting override val namespace: String,
) : RendererFormat.Simple, KoinComponent {
    override val index: Key = Key.key(namespace, "enchantments")

    fun render(enchantments: Map<Enchantment, Int>): IndexedText {
        return SimpleIndexedText(index, enchantments.map { (enchantment, level) -> enchantment.displayName(level) })
    }
}