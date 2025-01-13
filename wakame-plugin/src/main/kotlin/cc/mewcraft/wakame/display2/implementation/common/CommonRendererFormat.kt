package cc.mewcraft.wakame.display2.implementation.common

import cc.mewcraft.wakame.Injector
import cc.mewcraft.wakame.core.RegistryEntry
import cc.mewcraft.wakame.display2.DerivedIndex
import cc.mewcraft.wakame.display2.IndexedText
import cc.mewcraft.wakame.display2.RendererFormat
import cc.mewcraft.wakame.display2.SimpleIndexedText
import cc.mewcraft.wakame.display2.TextMetaFactory
import cc.mewcraft.wakame.display2.TextMetaFactoryPredicate
import cc.mewcraft.wakame.item.components.PortableCore
import cc.mewcraft.wakame.item.components.cells.AttributeCore
import cc.mewcraft.wakame.rarity.RarityType
import cc.mewcraft.wakame.util.removeItalic
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import it.unimi.dsi.fastutil.objects.ObjectImmutableList
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.component
import org.bukkit.enchantments.Enchantment
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.NodeKey
import xyz.xenondevs.commons.collections.takeUnlessEmpty

/* 这里定义了可以在不同渲染器之间通用的 RendererFormat 实现 */

/**
 * 一种专用于额外的物品描述 (lore) 的渲染格式.
 *
 * @param tooltip 内容的格式
 */
@ConfigSerializable
internal data class ExtraLoreRendererFormat(
    override val namespace: String,
    private val tooltip: Tooltip,
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
        val header: List<Component> = tooltip.header.takeUnlessEmpty()?.mapTo(ObjectArrayList(tooltip.header.size), MM::deserialize) ?: ObjectImmutableList.of()
        val bottom: List<Component> = tooltip.bottom.takeUnlessEmpty()?.mapTo(ObjectArrayList(tooltip.bottom.size), MM::deserialize) ?: ObjectImmutableList.of()
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
        val line: String,
        val header: List<String> = listOf(),
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
internal data class RarityRendererFormat(
    override val namespace: String,
    private val simple: String,
    private val complex: String,
) : RendererFormat.Simple {
    override val id: String = "rarity"
    override val index: DerivedIndex = createIndex()
    override val textMetaFactory: TextMetaFactory = TextMetaFactory()
    override val textMetaPredicate: TextMetaFactoryPredicate = TextMetaFactoryPredicate(namespace, id)

    fun renderSimple(rarity: RegistryEntry<RarityType>): IndexedText {
        return SimpleIndexedText(
            index, listOf(
                MM.deserialize(
                    simple,
                    component("rarity_display_name", rarity.value.displayName)
                )
            )
        )
    }

    fun renderComplex(rarity: RegistryEntry<RarityType>, modCount: Int): IndexedText {
        return SimpleIndexedText(
            index, listOf(
                MM.deserialize(
                    complex,
                    component("rarity_display_name", rarity.value.displayName),
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
    override val namespace: String,
    @NodeKey
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