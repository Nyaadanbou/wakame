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
import org.bukkit.inventory.ItemStack
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
     * The wrapped [ItemStack].
     *
     * The item stack may or may not be backed by a NMS object.
     *
     * ## When it is backed by a NMS object
     *
     * Any changes on `this` will reflect on the underlying game state, which
     * means: you may freely modify `this` and it will make sure that your
     * modifications will be directly and instantly applied to the world state.
     *
     * ## When it is backed by a strictly-Bukkit object
     *
     * Any changes on `this` will **NOT** apply to the underlying world state,
     * which means: you should only use `this` to add a new [ItemStack] to the
     * world state, such as giving it to players and dropping it on the ground.
     * In other words, if you have already added `this` to the world state, **DO
     * NOT** modify `this` and then expect that your changes will apply to the
     * world state.
     */
    val itemStack: ItemStack

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
