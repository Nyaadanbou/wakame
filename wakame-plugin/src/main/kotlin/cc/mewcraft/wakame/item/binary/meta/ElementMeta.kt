package cc.mewcraft.wakame.item.binary.meta

import cc.mewcraft.wakame.display.LoreLine
import cc.mewcraft.wakame.display.NoopLoreLine
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.ItemMetaConstants
import cc.mewcraft.wakame.registry.ElementRegistry
import cc.mewcraft.wakame.util.getByteArrayOrNull
import it.unimi.dsi.fastutil.objects.ObjectArraySet
import me.lucko.helper.nbt.ShadowTagType
import net.kyori.adventure.key.Key

/**
 * 物品的元素。
 *
 * 如果该物品上有X元素的属性或技能，那么该集合一定会包含X元素。
 */
@JvmInline
value class BElementMeta(
    private val accessor: ItemMetaAccessor,
) : BinaryItemMeta<Set<Element>> {
    override val key: Key
        get() = ItemMetaConstants.createKey { ELEMENT }
    override val exists: Boolean
        get() = accessor.rootOrNull?.contains(ItemMetaConstants.ELEMENT, ShadowTagType.BYTE_ARRAY) ?: false

    override fun getOrNull(): Set<Element>? {
        return accessor.rootOrNull
            ?.getByteArrayOrNull(key.value())
            ?.mapTo(ObjectArraySet(2)) {
                ElementRegistry.getBy(it)
            }
    }

    override fun remove() {
        accessor.rootOrNull?.remove(key.value())
    }

    override fun set(value: Set<Element>) {
        require(value.isNotEmpty()) { "Set<Element> must be not empty" }
        val byteArray = value.map { it.binaryId }.toByteArray()
        accessor.rootOrCreate.putByteArray(key.value(), byteArray)
    }

    fun set(value: Collection<Element>) {
        set(value.toHashSet())
    }

    override fun provideDisplayLore(): LoreLine {
        val key = ItemMetaSupport.getLineKey(this) ?: return NoopLoreLine
        val lines = tooltips.render(get(), Element::displayName)
        return ItemMetaLoreLine(key, lines)
    }

    private companion object : ItemMetaConfig(
        ItemMetaConstants.ELEMENT
    ) {
        val tooltips: MergedTooltips = MergedTooltips()
    }
}

fun BElementMeta?.getOrEmpty(): Set<Element> {
    return this?.getOrNull() ?: emptySet()
}