package cc.mewcraft.wakame.item.binary

import cc.mewcraft.wakame.item.ItemBehaviorAccessor
import cc.mewcraft.wakame.item.ItemSlot
import cc.mewcraft.wakame.item.binary.cell.ItemCellAccessor
import cc.mewcraft.wakame.item.binary.meta.BinaryItemMeta
import cc.mewcraft.wakame.item.binary.meta.ItemMetaAccessor
import cc.mewcraft.wakame.item.binary.meta.getAccessor
import cc.mewcraft.wakame.item.binary.stats.ItemStatisticsAccessor
import cc.mewcraft.wakame.item.schema.NekoItem
import cc.mewcraft.wakame.util.CompoundShadowTag
import me.lucko.helper.shadows.nbt.CompoundShadowTag
import net.kyori.adventure.key.Key
import java.util.UUID

/**
 * A wrapper of an ItemStack, which provides dedicated properties and
 * functions to manipulate wakame data on the ItemStack.
 *
 * Except some generic use cases, you probably will not directly work
 * with this interface. Instead, you will likely use the subclasses.
 * Use your IDE to navigate them.
 *
 * To get an instance of [NekoStack], use a corresponding factory.
 */
interface NekoStack : ItemBehaviorAccessor {

    /**
     * Gets the "wakame" [Compound][CompoundShadowTag] of this item.
     *
     * This **does not** include any other tags which are **not** part of the
     * wakame item NBT specifications, such as display name, lore, enchantment and
     * durability, which are already accessible via the Bukkit API. To get access to
     * those tags, just use the wrapped ItemStack.
     */
    val tags: CompoundShadowTag

    /**
     * The corresponding [NekoItem] schema.
     *
     * @throws NullPointerException if this is not a [NekoItem] realization
     */
    val schema: NekoItem

    /**
     * The [namespaced ID][Key] of this item.
     *
     * @throws NullPointerException if this is not a [NekoItem] realization
     */
    var key: Key

    /**
     * The namespace of this item.
     *
     * @throws NullPointerException if this is not a [NekoItem] realization
     */
    var namespace: String

    /**
     * The path of this item.
     *
     * @throws NullPointerException if this is not a [NekoItem] realization
     */
    var path: String

    /**
     * The variant of this item.
     *
     * @throws NullPointerException if this is not a [NekoItem] realization
     */
    var variant: Int

    /**
     * The UUID of this item.
     *
     * @throws NullPointerException if this is not a [NekoItem] realization
     */
    val uuid: UUID

    /**
     * The inventory slot where this item becomes effective.
     *
     * @throws NullPointerException if this is not a [NekoItem] realization
     */
    val slot: ItemSlot

    /**
     * The [ItemCellAccessor] of this item.
     *
     * Used to manipulate the **cells** of this item.
     *
     * @throws NullPointerException if this is not a [NekoItem] realization
     */
    val cell: ItemCellAccessor

    /**
     * The [ItemMetaAccessor] of this item.
     *
     * Used to manipulate the **meta** of this item.
     *
     * @throws NullPointerException if this is not a [NekoItem] realization
     */
    val meta: ItemMetaAccessor

    /**
     * The [ItemStatisticsAccessor] of this item.
     *
     * Used to manipulate the **statistics** of this item.
     *
     * @throws NullPointerException if this is not a [NekoItem] realization
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
