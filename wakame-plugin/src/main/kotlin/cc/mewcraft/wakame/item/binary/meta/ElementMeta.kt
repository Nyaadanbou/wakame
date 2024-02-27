package cc.mewcraft.wakame.item.binary.meta

import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.ItemMetaKeys
import cc.mewcraft.wakame.registry.ElementRegistry
import cc.mewcraft.wakame.registry.getByOrThrow
import cc.mewcraft.wakame.util.getByteArrayOrNull
import it.unimi.dsi.fastutil.objects.ObjectArraySet
import me.lucko.helper.nbt.ShadowTagType
import me.lucko.helper.shadows.nbt.CompoundShadowTag
import net.kyori.adventure.key.Key

/**
 * 物品的元素。
 *
 * 如果该物品上有X元素的属性或技能，那么该集合一定会包含X元素。
 */
internal class ElementMeta(
    private val holder: ItemMetaHolderImpl,
) : ItemMeta<Set<Element>> {
    override val key: Key = ItemMetaKeys.ELEMENT
    override val companion: ItemMetaCompanion = Companion

    override fun getOrNull(): Set<Element>? {
        return holder.rootOrNull
            ?.getByteArrayOrNull(key.value())
            ?.mapTo(ObjectArraySet(2)) {
                ElementRegistry.getByOrThrow(it)
            }
    }

    override fun remove() {
        holder.rootOrNull?.remove(key.value())
    }

    override fun set(value: Set<Element>) {
        require(value.isNotEmpty()) { "Set<Element> must be not empty" }
        val byteArray = value.map { it.binary }.toByteArray()
        holder.rootOrCreate.putByteArray(key.value(), byteArray)
    }

    fun set(value: Collection<Element>) {
        set(value.toHashSet())
    }

    companion object : ItemMetaCompanion {
        override fun contains(compound: CompoundShadowTag): Boolean {
            return compound.contains(ItemMetaKeys.ELEMENT.value(), ShadowTagType.BYTE_ARRAY)
        }
    }
}