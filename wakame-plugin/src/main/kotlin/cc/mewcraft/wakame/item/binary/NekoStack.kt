package cc.mewcraft.wakame.item.binary

import cc.mewcraft.wakame.item.ItemBehaviorAccessor
import cc.mewcraft.wakame.item.ItemSlot
import cc.mewcraft.wakame.item.binary.cell.ItemCellAccessor
import cc.mewcraft.wakame.item.binary.meta.BinaryItemMeta
import cc.mewcraft.wakame.item.binary.meta.ItemMetaAccessor
import cc.mewcraft.wakame.item.binary.meta.getAccessor
import cc.mewcraft.wakame.item.binary.stats.ItemStatisticsAccessor
import cc.mewcraft.wakame.item.schema.NekoItem
import me.lucko.helper.shadows.nbt.CompoundShadowTag
import net.kyori.adventure.key.Key
import java.util.UUID

/**
 * A wrapper of an ItemStack, which provides dedicated properties and
 * functions to manipulate wakame data on the ItemStack.
 *
 * This is a top-level interface. Except some generic use cases, you
 * probably will not directly work with this interface. Instead, you
 * will likely use the subclasses. Use your IDE to navigate them.
 */
interface NekoStack : ItemBehaviorAccessor {

    /**
     * Gets the "wakame" [Compound][CompoundShadowTag] of this item.
     *
     * This does not include any other tags which are not part of the
     * wakame item NBT specifications.
     */
    val tags: CompoundShadowTag

    /**
     * The corresponding [NekoItem] schema.
     */
    val schema: NekoItem

    /**
     * The [namespaced identifier][Key] of this item.
     */
    val key: Key

    /**
     * The namespace of this item.
     */
    val namespace: String

    /**
     * The path of this item.
     */
    val path: String

    /**
     * The variant of this item.
     */
    var variant: Int

    /**
     * The UUID of this item.
     */
    val uuid: UUID

    /**
     * The inventory slot where this item becomes effective.
     */
    val slot: ItemSlot

    /**
     * The [ItemCellAccessor] of this item.
     *
     * Used to manipulate the **cells** of this item.
     */
    val cell: ItemCellAccessor

    /**
     * The [ItemMetaAccessor] of this item.
     *
     * Used to manipulate the **meta** of this item.
     */
    val meta: ItemMetaAccessor

    /**
     * The [ItemStatisticsAccessor] of this item.
     *
     * Used to manipulate the **statistics** of this item.
     */
    val statistics: ItemStatisticsAccessor

    /**
     * Removes all the custom tags from the item.
     *
     * **Only to be used in certain special cases!**
     */
    fun erase()

}

/**
 * Gets the data accessor of specific item meta.
 *
 * @see ItemMetaAccessor.getAccessor
 */
inline fun <reified M : BinaryItemMeta<*>> NekoStack.getMetaAccessor(): M {
    return this.meta.getAccessor<M>()
}
