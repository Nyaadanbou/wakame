package cc.mewcraft.wakame.item.display.implementation.common

import cc.mewcraft.wakame.item.data.impl.AttributeCore
import cc.mewcraft.wakame.item.data.impl.Core
import cc.mewcraft.wakame.item.display.*
import cc.mewcraft.wakame.item.feature.EnchantSlotFeature
import cc.mewcraft.wakame.rarity.Rarity
import cc.mewcraft.wakame.registry.entry.RegistryEntry
import cc.mewcraft.wakame.util.adventure.removeItalic
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import it.unimi.dsi.fastutil.objects.ObjectImmutableList
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.NodeKey
import xyz.xenondevs.commons.collections.takeUnlessEmpty
import java.util.Collections.emptyList

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
        val lines = data.mapTo(ObjectArrayList(size)) { MiniMessage.miniMessage().deserialize(tooltip.line, Placeholder.component("line", it)) }
        val header: List<Component> = tooltip.header.takeUnlessEmpty()?.mapTo(ObjectArrayList(tooltip.header.size), MiniMessage.miniMessage()::deserialize) ?: ObjectImmutableList.of()
        val bottom: List<Component> = tooltip.bottom.takeUnlessEmpty()?.mapTo(ObjectArrayList(tooltip.bottom.size), MiniMessage.miniMessage()::deserialize) ?: ObjectImmutableList.of()
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
        if (data.isEmpty())
            return IndexedText.NO_OP
        return SimpleIndexedText(index, data.map { (enchantment, level) -> enchantment.displayName(level).removeItalic })
    }
}

/**
 * @property normal 没有额外附魔槽位时的提示文本
 * @property extra 拥有额外附魔槽位时的提示文本
 */
@ConfigSerializable
internal data class EnchantSlotRendererFormat(
    override val namespace: String,
    private val normal: String = "<!i><gray>附魔槽位: <white><used>/<max_base>",
    private val extra: String = "<!i><gray>附魔槽位: <white><used>/<max_base> (+<max_extra>)",
) : RendererFormat.Simple {
    override val id: String = "enchant_slots"
    override val index: DerivedIndex = Key.key(namespace, id)
    override val textMetaFactory: TextMetaFactory = TextMetaFactory.fixed()
    override val textMetaPredicate: TextMetaFactoryPredicate = TextMetaFactoryPredicate.literal(namespace, id)

    fun render(item: ItemStack): IndexedText {
        val used = EnchantSlotFeature.getSlotUsed(item)
        val maxBase = EnchantSlotFeature.getSlotBase(item)
        val maxExtra = EnchantSlotFeature.getSlotExtra(item)
        val maxTotal = maxBase + maxExtra
        if (maxTotal <= 0) return IndexedText.NO_OP
        val resolver = TagResolver.resolver(
            Placeholder.component("used", Component.text(used)),
            Placeholder.component("max_base", Component.text(maxBase)),
            Placeholder.component("max_extra", Component.text(maxExtra)),
            Placeholder.component("max_total", Component.text(maxTotal)),
        )
        return if (maxExtra <= 0) {
            SimpleIndexedText(index, listOf(MiniMessage.miniMessage().deserialize(normal, resolver)))
        } else {
            SimpleIndexedText(index, listOf(MiniMessage.miniMessage().deserialize(extra, resolver)))
        }
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

    fun renderSimple(rarity: RegistryEntry<Rarity>): IndexedText {
        return SimpleIndexedText(
            index, listOf(
                MiniMessage.miniMessage().deserialize(
                    simple,
                    Placeholder.component("rarity_display_name", rarity.unwrap().displayName)
                )
            )
        )
    }

    fun renderComplex(rarity: RegistryEntry<Rarity>, modCount: Int): IndexedText {
        return SimpleIndexedText(
            index, listOf(
                MiniMessage.miniMessage().deserialize(
                    complex,
                    Placeholder.component("rarity_display_name", rarity.unwrap().displayName),
                    Placeholder.component("reforge_mod_count", Component.text(modCount.toString()))
                )
            )
        )
    }
}

@ConfigSerializable
internal data class CoreRendererFormat(
    override val namespace: String,
    @NodeKey
    override val id: String,
) : RendererFormat.Simple {
    override val index: DerivedIndex = createIndex()
    override val textMetaFactory: TextMetaFactory = TextMetaFactory()
    override val textMetaPredicate: TextMetaFactoryPredicate = TextMetaFactoryPredicate(namespace, id)
    private val unknownIndex: Key = Key.key(namespace, "unknown")

    fun render(data: Core): IndexedText {
        val core = (data as? AttributeCore)
            ?: return SimpleIndexedText(unknownIndex, emptyList())
        return SimpleIndexedText(index, core.description)
    }
}