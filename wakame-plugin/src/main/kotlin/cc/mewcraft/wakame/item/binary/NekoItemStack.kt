package cc.mewcraft.wakame.item.binary

import cc.mewcraft.wakame.annotation.InternalApi
import cc.mewcraft.wakame.item.binary.cell.CellAccessor
import cc.mewcraft.wakame.item.binary.meta.ItemMetaAccessor
import cc.mewcraft.wakame.item.binary.stats.ItemStatsAccessor
import cc.mewcraft.wakame.item.scheme.NekoItem
import net.kyori.adventure.key.Key
import org.bukkit.inventory.ItemStack
import java.util.UUID

/**
 * A wrapper of [ItemStack] which is created from a [NekoItem].
 *
 * To get an instance of [NekoItemStack], use [NekoItemStackFactory].
 *
 * This class provides several properties to work with the underlying item
 * stack, mainly manipulating the non-vanilla NBT tags of it, such as:
 * - checking whether the item is a neko item
 * - getting the identifier of the neko item
 * - retrieving the NBT tags of the bukkit item
 */
interface NekoItemStack : NekoItemStackSetter {
    /**
     * The wrapped [ItemStack].
     *
     * The item stack may or may not be backed by a NMS object.
     *
     * ## When it is backed by a NMS object
     *
     * Any changes on `this` will be reflected on the underlying game world.
     *
     * ## When it is backed by a strictly-Bukkit object
     *
     * Any changes on `this` will **not** be reflected on the underlying game
     * world. In such case, `this` is primarily used to add a new [ItemStack]
     * to the underlying game world, such as giving it to the player.
     *
     * @see isOneOff
     */
    val handle: ItemStack // TODO use `Any` to directly store a NMS object?

    /**
     * Records whether `this` is a one-off [NekoItemStack] instance.
     *
     * A one-off [NekoItemStack] instance is effectively backed by a
     * strictly-Bukkit [ItemStack]. That is, the [handle] is a strictly-Bukkit
     * [ItemStack].
     *
     * It should be noted, once the [handle] of a one-off [NekoItemStack] has
     * been added to the underlying game world, any changes to the one-off
     * [NekoItemStack] **will not** reflect to that one in the underlying game
     * world. This is because the server implementation always makes a NMS copy
     * out of the strictly-Bukkit [ItemStack] when the item is being added to
     * the underlying game world.
     *
     * However, this may not hold if the Paper
     * team finish up the ItemStack overhaul:
     * [Interface ItemStacks](https://github.com/orgs/PaperMC/projects/6#).
     * At that time, this property will probably no longer be needed.
     *
     * Please keep an eye on this kdoc. I will add notes here as soon as
     * anything has changed.
     *
     * @see handle
     */
    val isOneOff: Boolean

    /**
     * Erases all the custom tags from `this`.
     *
     * **Only to be used in certain special cases**.
     */
    @InternalApi
    fun erase()

    /**
     * Returns `true` if this item is a neko item.
     *
     * In this case [isNotNeko] returns `false`.
     */
    val isNeko: Boolean

    /**
     * Returns `true` if this item is not a neko item.
     *
     * In this case [isNeko] returns `false`.
     */
    val isNotNeko: Boolean

    /**
     * The corresponding [NekoItem] scheme.
     *
     * @throws NullPointerException if this is not a legal neko item
     */
    val scheme: NekoItem

    /**
     * The namespace of this item.
     *
     * @throws NullPointerException if this is not a legal neko item
     */
    val namespace: String

    /**
     * The ID of this item.
     *
     * @throws NullPointerException if this is not a legal neko item
     */
    val id: String

    /**
     * The [namespaced ID][Key] of this item.
     *
     * @throws NullPointerException if this is not a legal neko item
     */
    val key: Key

    /**
     * The UUID of this item.
     *
     * @throws NullPointerException if this is not a legal neko item
     */
    val uuid: UUID

    /**
     * The CellMap of this item.
     *
     * Used to manipulate the **cells** of this item.
     *
     * @throws NullPointerException if this is not a legal neko item
     */
    val cells: CellAccessor

    /**
     * The ItemMetaMap of this item.
     *
     * Used to manipulate the **meta** of this item.
     *
     * @throws NullPointerException if this is not a legal neko item
     */
    val metadata: ItemMetaAccessor

    /**
     * The ItemStatsMap of this item.
     *
     * Used to manipulate the **statistics** of this item.
     *
     * @throws NullPointerException if this is not a legal neko item
     */
    val statistics: ItemStatsAccessor

    override fun hashCode(): Int
    override fun equals(other: Any?): Boolean
}