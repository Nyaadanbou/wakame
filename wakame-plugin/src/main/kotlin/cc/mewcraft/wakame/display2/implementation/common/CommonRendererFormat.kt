package cc.mewcraft.wakame.display2.implementation.common

import cc.mewcraft.wakame.Injector
import cc.mewcraft.wakame.display2.DerivedIndex
import cc.mewcraft.wakame.display2.IndexedText
import cc.mewcraft.wakame.display2.RendererFormat
import cc.mewcraft.wakame.display2.SimpleIndexedText
import cc.mewcraft.wakame.display2.TextMetaFactory
import cc.mewcraft.wakame.display2.TextMetaFactoryPredicate
import cc.mewcraft.wakame.item.components.PortableCore
import cc.mewcraft.wakame.item.components.cells.AttributeCore
import cc.mewcraft.wakame.rarity.Rarity
import cc.mewcraft.wakame.util.removeItalic
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import it.unimi.dsi.fastutil.objects.ObjectImmutableList
import net.kyori.adventure.extra.kotlin.join
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.component
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.enchantments.Enchantment
import org.koin.core.component.get
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.NodeKey
import org.spongepowered.configurate.objectmapping.meta.Required
import org.spongepowered.configurate.objectmapping.meta.Setting
import xyz.xenondevs.commons.collections.takeUnlessEmpty

/* 这里定义了可以在不同渲染器之间通用的 RendererFormat 实现 */

/**
 * 硬编码的渲染格式.
 *
 * 不包含任何现成的格式, 只提供最基本的格式信息.
 */
@ConfigSerializable
internal data class HardcodedRendererFormat(
    @Setting @Required
    override val namespace: String,
    @Setting @NodeKey
    override val id: String, // id 是配置文件指定的
) : RendererFormat.Simple {
    override val index: DerivedIndex = createIndex()
    override val textMetaFactory: TextMetaFactory = TextMetaFactory()
    override val textMetaPredicate: TextMetaFactoryPredicate = TextMetaFactoryPredicate(namespace, id)
}

/**
 * 一种最简单的渲染格式.
 *
 * 只有一个格式 [tooltip], 可以包含任意占位符.
 * 当生成内容时, 将对 [tooltip] 填充一次占位符.
 *
 * @param tooltip 内容的格式
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
    override val index: DerivedIndex = createIndex()
    override val textMetaFactory: TextMetaFactory = TextMetaFactory()
    override val textMetaPredicate: TextMetaFactoryPredicate = TextMetaFactoryPredicate(namespace, id)

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
 *
 * @param tooltip 内容的格式
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
    override val index: DerivedIndex = createIndex()
    override val textMetaFactory: TextMetaFactory = TextMetaFactory()
    override val textMetaPredicate: TextMetaFactoryPredicate = TextMetaFactoryPredicate(namespace, id)

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
 *
 * @param tooltip 内容的格式
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
    override val index: DerivedIndex = createIndex()
    override val textMetaFactory: TextMetaFactory = TextMetaFactory()
    override val textMetaPredicate: TextMetaFactoryPredicate = TextMetaFactoryPredicate(namespace, id)

    /**
     * A convenience function to stylize a list of objects.
     */
    fun <T> render(
        collection: Collection<T>,
        transform: (T) -> Component,
    ): IndexedText {
        val merged = merge(collection, transform)
        return SimpleIndexedText(index, listOf(merged))
    }

    /**
     * A convenience function to stylize a list of objects.
     */
    fun <T> render(
        collection: Collection<T>,
        transform: (T) -> Component,
        vararg resolver: TagResolver, // 应用在 merge 之后
    ): IndexedText {
        val merged = merge(collection, transform, *resolver)
        return SimpleIndexedText(index, listOf(merged))
    }

    private fun <T> merge(
        collection: Collection<T>,
        transform: (T) -> Component,
        vararg resolver: TagResolver,
    ): Component {
        val merged = collection
            .mapTo(ObjectArrayList(collection.size)) { MM.deserialize(tooltip.single, component("single", transform(it))) }
            .join(JoinConfiguration.separator(MM.deserialize(tooltip.separator)))
        return MM.deserialize(tooltip.merged, component("merged", merged), *resolver)
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
 * @param tooltip 内容的格式
 */
@ConfigSerializable
internal data class ExtraLoreRendererFormat(
    @Setting @Required
    override val namespace: String,
    @Setting
    private val tooltip: Tooltip = Tooltip(),
) : RendererFormat.Simple {
    override val id: String = "lore"
    override val index: DerivedIndex = Key.key(namespace, id)
    override val textMetaFactory: TextMetaFactory = TextMetaFactory()
    override val textMetaPredicate: TextMetaFactoryPredicate = TextMetaFactoryPredicate(namespace, id)

    /**
     * @param data 额外的物品描述
     */
    fun render(data: List<Component>): IndexedText {
        val size = tooltip.header.size + data.size + tooltip.bottom.size
        val lines = data.mapTo(ObjectArrayList(size)) { MM.deserialize(tooltip.line, component("line", it)) }
        val header = tooltip.header.takeUnlessEmpty()?.mapTo(ObjectArrayList(tooltip.header.size), MM::deserialize) ?: ObjectImmutableList.of()
        val bottom = tooltip.bottom.takeUnlessEmpty()?.mapTo(ObjectArrayList(tooltip.bottom.size), MM::deserialize) ?: ObjectImmutableList.of()
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
    override val index: DerivedIndex = Key.key(namespace, id)
    override val textMetaFactory: TextMetaFactory = TextMetaFactory()
    override val textMetaPredicate: TextMetaFactoryPredicate = TextMetaFactoryPredicate(namespace, id)

    /**
     * @param data 魔咒和等级的映射
     */
    fun render(data: Map<Enchantment, Int>): IndexedText {
        return SimpleIndexedText(index, data.map { (enchantment, level) -> enchantment.displayName(level).removeItalic })
    }
}

@ConfigSerializable
data class RarityRendererFormat(
    override val namespace: String,
    private val simple: String,
    private val complex: String,
) : RendererFormat.Simple {
    override val id: String = "rarity"
    override val index: DerivedIndex = createIndex()
    override val textMetaFactory: TextMetaFactory = TextMetaFactory()
    override val textMetaPredicate: TextMetaFactoryPredicate = TextMetaFactoryPredicate(namespace, id)

    fun renderSimple(rarity: Rarity): IndexedText {
        return SimpleIndexedText(
            index, listOf(
                MM.deserialize(
                    simple,
                    component("rarity_display_name", rarity.displayName)
                )
            )
        )
    }

    fun renderComplex(rarity: Rarity, modCount: Int): IndexedText {
        return SimpleIndexedText(
            index, listOf(
                MM.deserialize(
                    complex,
                    component("rarity_display_name", rarity.displayName),
                    component("reforge_mod_count", Component.text(modCount.toString()))
                )
            )
        )
    }

    companion object {
        private val MM = Injector.get<MiniMessage>()
    }
}

@ConfigSerializable
internal data class PortableCoreRendererFormat(
    @Setting @Required
    override val namespace: String,
    @Setting @NodeKey
    override val id: String,
) : RendererFormat.Simple {
    override val index: DerivedIndex = createIndex()
    override val textMetaFactory: TextMetaFactory = TextMetaFactory()
    override val textMetaPredicate: TextMetaFactoryPredicate = TextMetaFactoryPredicate(namespace, id)
    private val unknownIndex: Key = Key.key(namespace, "unknown")

    fun render(data: PortableCore): IndexedText {
        val core = (data.wrapped as? AttributeCore)
            ?: return SimpleIndexedText(unknownIndex, emptyList())
        return SimpleIndexedText(index, core.description)
    }
}