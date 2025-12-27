package cc.mewcraft.wakame.item.display.implementation.common

import cc.mewcraft.wakame.item.display.*
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import net.kyori.adventure.extra.kotlin.join
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.component
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.NodeKey

/**
 * 硬编码的渲染格式.
 *
 * 不包含任何现成的格式, 只提供最基本的格式信息.
 */
@ConfigSerializable
internal data class HardcodedRendererFormat(
    override val namespace: String,
    @NodeKey
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
    override val namespace: String,
    @NodeKey
    override val id: String,
    private val tooltip: String, // mini message format
) : RendererFormat.Simple {
    override val index: DerivedIndex = createIndex()
    override val textMetaFactory: TextMetaFactory = TextMetaFactory()
    override val textMetaPredicate: TextMetaFactoryPredicate = TextMetaFactoryPredicate(namespace, id)

    fun render(): IndexedText {
        return SimpleIndexedText(index, listOf(MiniMessage.miniMessage().deserialize(tooltip)))
    }

    fun render(resolver: TagResolver): IndexedText {
        return SimpleIndexedText(index, listOf(MiniMessage.miniMessage().deserialize(tooltip, resolver)))
    }

    fun render(vararg resolver: TagResolver): IndexedText {
        return SimpleIndexedText(index, listOf(MiniMessage.miniMessage().deserialize(tooltip, *resolver)))
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
    override val namespace: String,
    @NodeKey
    override val id: String,
    private val tooltip: List<String>, // mini message format,
) : RendererFormat.Simple {
    override val index: DerivedIndex = createIndex()
    override val textMetaFactory: TextMetaFactory = TextMetaFactory()
    override val textMetaPredicate: TextMetaFactoryPredicate = TextMetaFactoryPredicate(namespace, id)

    fun render(): IndexedText {
        return SimpleIndexedText(index, tooltip.map(MiniMessage.miniMessage()::deserialize))
    }

    fun render(resolver: TagResolver): IndexedText {
        return SimpleIndexedText(index, tooltip.map { MiniMessage.miniMessage().deserialize(it, resolver) })
    }

    fun render(vararg resolver: TagResolver): IndexedText {
        return SimpleIndexedText(index, tooltip.map { MiniMessage.miniMessage().deserialize(it, *resolver) })
    }
}

/**
 * 一种需要将多个对象合并成一个字符串的渲染格式.
 *
 * @param tooltip 内容的格式
 */
@ConfigSerializable
internal data class AggregateValueRendererFormat(
    override val namespace: String,
    @NodeKey
    override val id: String,
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
            .mapTo(ObjectArrayList(collection.size)) { MiniMessage.miniMessage().deserialize(tooltip.single, component("single", transform(it))) }
            .join(JoinConfiguration.separator(MiniMessage.miniMessage().deserialize(tooltip.separator)))
        return MiniMessage.miniMessage().deserialize(tooltip.merged, component("merged", merged), *resolver)
    }

    /**
     * @param merged 合并后的字符串的格式
     * @param single 单个对象的字符串的格式
     * @param separator 分隔符
     */
    @ConfigSerializable
    data class Tooltip(
        val single: String,
        val separator: String,
        val merged: String,
    )
}