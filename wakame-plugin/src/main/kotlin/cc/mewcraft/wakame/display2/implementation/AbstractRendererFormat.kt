package cc.mewcraft.wakame.display2.implementation

import cc.mewcraft.commons.collections.takeUnlessEmpty
import cc.mewcraft.wakame.Injector
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
import org.koin.core.component.get
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.*
import kotlin.collections.component1
import kotlin.collections.component2

/* 这里定义了常见的 RendererFormat 的实现/接口 */

/**
 * 一种最简单的渲染格式.
 *
 * 只有一个格式 [tooltip], 可以包含任意占位符.
 * 当生成内容时, 将对 [tooltip] 填充一次占位符.
 */
@ConfigSerializable
internal data class SingleValueRendererFormat(
    @Setting @Required
    override val namespace: String,
    @Setting @NodeKey
    override val id: String,
    @Setting
    private val tooltip: String, // mini message format
) : RendererFormat.Simple {
    override val index: Key = createIndex()

    fun render(): IndexedText {
        return SimpleIndexedText(index, listOf(MM.deserialize(tooltip)))
    }

    fun render(resolver: TagResolver): IndexedText {
        return SimpleIndexedText(index, listOf(MM.deserialize(tooltip, resolver)))
    }

    fun render(vararg resolver: TagResolver): IndexedText {
        return SimpleIndexedText(index, listOf(MM.deserialize(tooltip, *resolver)))
    }

    companion object Shared {
        private val MM = Injector.get<MiniMessage>()
    }
}

/**
 * 跟 [SingleValueRendererFormat] 类似, 只不过可以拥有多行.
 *
 * 只有一个格式 [tooltip], 可以包含任意占位符.
 * 当生成内容时, 将对 [tooltip] 填充一次占位符.
 */
@ConfigSerializable
internal data class ListValueRendererFormat(
    @Setting @Required
    override val namespace: String,
    @Setting @NodeKey
    override val id: String,
    @Setting
    private val tooltip: List<String>, // mini message format,
) : RendererFormat.Simple {
    override val index: Key = createIndex()

    fun render(): IndexedText {
        return SimpleIndexedText(index, tooltip.map(MM::deserialize))
    }

    fun render(resolver: TagResolver): IndexedText {
        return SimpleIndexedText(index, tooltip.map { MM.deserialize(it, resolver) })
    }

    fun render(vararg resolver: TagResolver): IndexedText {
        return SimpleIndexedText(index, tooltip.map { MM.deserialize(it, *resolver) })
    }

    companion object Shared {
        private val MM = Injector.get<MiniMessage>()
    }
}

/**
 * 一种需要将多个对象合并成一个字符串的渲染格式.
 */
@ConfigSerializable
internal data class AggregateValueRendererFormat(
    @Setting @Required
    override val namespace: String,
    @Setting @NodeKey
    override val id: String,
    @Setting
    private val tooltip: Tooltip, // mini message format
) : RendererFormat.Simple {
    override val index: Key = createIndex()

    /**
     * A convenience function to stylize a list of objects.
     */
    fun <T> render(
        collection: Collection<T>,
        transform: (T) -> Component,
    ): IndexedText {
        val merged = collection
            .mapTo(ObjectArrayList(collection.size)) { MM.deserialize(tooltip.single, component("single", transform(it))) }
            .join(JoinConfiguration.separator(MM.deserialize(tooltip.separator)))
            .let { MM.deserialize(tooltip.merged, component("merged", it)) }
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
        @Setting
        val single: String = "<single>",
        @Setting
        val separator: String = ", ",
        @Setting
        val merged: String = "FIXME: <merged>",
    )

    companion object Shared {
        private val MM = Injector.get<MiniMessage>()
    }
}

/**
 * 一种专用于额外的物品描述 (lore) 的渲染格式.
 *
 * @param tooltip 内容的格式, 不支持任何占位符
 */
@ConfigSerializable
internal data class ExtraLoreRendererFormat(
    @Setting @Required
    override val namespace: String,
    @Setting
    private val tooltip: Tooltip = Tooltip(),
) : RendererFormat.Simple {
    override val id: String = "lore"
    override val index: Key = Key.key(namespace, id)

    fun render(lore: List<String>): IndexedText {
        val size = tooltip.header.size + lore.size + tooltip.bottom.size
        val lines = lore.mapTo(ObjectArrayList(size)) { MM.deserialize(tooltip.line, parsed("line", it)) }
        val header = tooltip.header.takeUnlessEmpty()?.run { mapTo(ObjectArrayList(this.size), MM::deserialize) }
        val bottom = tooltip.bottom.takeUnlessEmpty()?.run { mapTo(ObjectArrayList(this.size), MM::deserialize) }
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
        @Setting
        val line: String = "<line>",
        @Setting
        val header: List<String> = listOf(),
        @Setting
        val bottom: List<String> = listOf(),
    )

    companion object Shared {
        private val MM = Injector.get<MiniMessage>()
    }
}

/**
 * 一种专用于物品魔咒 (enchantments) 的渲染格式.
 */
@ConfigSerializable
internal data class EnchantmentRendererFormat(
    @Setting @Required
    override val namespace: String,
) : RendererFormat.Simple {
    override val id: String = "enchantments"
    override val index: Key = Key.key(namespace, id)

    fun render(enchantments: Map<Enchantment, Int>): IndexedText {
        return SimpleIndexedText(index, enchantments.map { (enchantment, level) -> enchantment.displayName(level) })
    }
}