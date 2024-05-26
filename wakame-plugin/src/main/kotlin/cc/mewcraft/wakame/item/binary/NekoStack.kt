package cc.mewcraft.wakame.item.binary

import cc.mewcraft.wakame.item.EffectiveSlot
import cc.mewcraft.wakame.item.ItemBehaviorAccessor
import cc.mewcraft.wakame.item.binary.cell.ItemCellAccessor
import cc.mewcraft.wakame.item.binary.meta.BinaryItemMeta
import cc.mewcraft.wakame.item.binary.meta.ItemMetaAccessor
import cc.mewcraft.wakame.item.binary.meta.getAccessor
import cc.mewcraft.wakame.item.binary.show.CustomDataAccessor
import cc.mewcraft.wakame.item.binary.stats.ItemStatisticsAccessor
import cc.mewcraft.wakame.item.schema.NekoItem
import net.kyori.adventure.key.Key
import org.bukkit.inventory.ItemStack
import java.util.UUID
import com.github.retrooper.packetevents.protocol.item.ItemStack as PacketStack

/**
 * A wrapper of [ItemStack], which adds additional properties and
 * functions to work with the wakame data on the [itemStack], such as:
 *
 * - checking whether the item is of a neko item or not
 * - looking up the unique id of the neko item (if it is)
 *
 * Except some generic use cases, you will probably not directly work
 * with this interface. Instead, you will likely work with its subclasses:
 * [PlayNekoStack] and [ShowNekoStack].
 *
 * To get an instance of [NekoStack], use a factory:
 * [PlayNekoStackFactory] and [ShowNekoStackFactory].
 */
sealed interface NekoStack<I> : PlayNekoStackLike, ShowNekoStackLike, ItemBehaviorAccessor {
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
     * world state. Why is that? See [isNmsBacked].
     *
     * @see isNmsBacked
     */
    val itemStack: I

    /**
     * Checks if `this` NekoStack is backed by an NMS object.
     *
     * - Returning `true` means the [itemStack] is backed by an NMS object.
     * - Returning `false` means the [itemStack] is a strictly-Bukkit [ItemStack].
     *
     * So what's so important about it? It should be noted that the server
     * implementation always makes a **NMS copy** out of a strictly-Bukkit
     * [ItemStack] when the item is being added to the underlying world state.
     *
     * This will lead to the case in which any changes to the strictly-Bukkit
     * ItemStack will not apply to that corresponding NMS ItemStack in the world
     * state, which pretty makes sense as they are different objects!
     *
     * However, this may not hold if the Paper
     * team finish up the ItemStack overhaul:
     * [Interface ItemStacks](https://github.com/orgs/PaperMC/projects/6#).
     * At that time, this property will probably no longer be needed.
     *
     * Please keep an eye on this kdoc. I will add notes to here as soon as
     * anything has changed.
     *
     * @see itemStack
     */
    val isNmsBacked: Boolean

    /**
     * Returns `true` if this item is a [NekoItem] realization.
     *
     * When this returns `true`, you can then access the wakame data on the
     * [itemStack] without throwing NPE exceptions.
     */
    val isNeko: Boolean

    /**
     * Returns `true` if this item is a legal [PlayNekoStack].
     *
     * @throws NullPointerException if this is not a [NekoItem] realization
     * @see PlayNekoStack
     */
    val isPlay: Boolean

    /**
     * Returns `true` if this item is a legal [ShowNekoStack].
     *
     * @throws NullPointerException if this is not a [NekoItem] realization
     * @see ShowNekoStack
     */
    val isShow: Boolean

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
    val effectiveSlot: EffectiveSlot

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
 * Represents a [NekoStack] which will ultimately be used by players.
 *
 * For example, a [PlayNekoStack] may:
 * - be given to players
 * - be kept by players
 */
interface PlayNekoStack : NekoStack<ItemStack> {
    // TDB 暂时和 NekoStack 一样
}

/**
 * Something that can be represented as a [PlayNekoStack].
 */
interface PlayNekoStackLike {
    /**
     * Gets a [PlayNekoStack] representation.
     *
     * @throws NullPointerException if this is not a [NekoItem] realization
     */
    val play: PlayNekoStack
}

/**
 * Represents a [NekoStack] which is **solely** used to build showcases.
 *
 * A [ShowNekoStack] differs from [PlayNekoStack] in that it provides interfaces
 * to conveniently modify the name and lore of the [itemStack].
 *
 * For example, a [ShowNekoStack] may:
 * - be stored in a display item
 * - be placed in a virtual inventory
 */
interface ShowNekoStack : NekoStack<ItemStack> {
    /**
     * The [CustomDataAccessor] of this item.
     *
     * Used to manipulate the **custom data** of this item.
     *
     * @throws NullPointerException if this is not a [NekoItem] realization
     */
    val customData: CustomDataAccessor
}

/**
 * Something that can be represented as a [ShowNekoStack].
 */
interface ShowNekoStackLike {
    /**
     * Gets a [ShowNekoStack] representation.
     *
     * @throws NullPointerException if this is not a [NekoItem] realization
     */
    val show: ShowNekoStack
}

interface PacketNekoStack : NekoStack<PacketStack>

/**
 * Gets the data accessor of specific item meta.
 *
 * @see ItemMetaAccessor.getAccessor
 */
inline fun <reified M : BinaryItemMeta<*>> NekoStack<*>.getMetaAccessor(): M {
    return this.meta.getAccessor<M>()
}
