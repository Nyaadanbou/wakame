package cc.mewcraft.wakame.item.binary

import cc.mewcraft.wakame.annotation.InternalApi
import cc.mewcraft.wakame.item.binary.cell.CellAccessor
import cc.mewcraft.wakame.item.binary.curse.BinaryCurseContext
import cc.mewcraft.wakame.item.binary.meta.ItemMetaAccessor
import cc.mewcraft.wakame.item.binary.stats.ItemStatsAccessor
import cc.mewcraft.wakame.item.scheme.WakaItem
import me.lucko.helper.shadows.nbt.CompoundShadowTag
import net.kyori.adventure.key.Key
import org.bukkit.inventory.ItemStack
import java.util.UUID

/**
 * A wrapper of [ItemStack] which is created from a [WakaItem].
 *
 * To get an instance of [WakaItemStack], use [WakaItemStackFactory].
 *
 * This class provides several properties to work with the underlying item
 * stack, mainly manipulating the non-vanilla NBT tags of it, such as:
 * - checking whether the item is a wakame item
 * - getting the identifier of the wakame item
 * - retrieving the NBT tags of the bukkit item
 */
interface WakaItemStack : WakaItemStackSetter, BinaryCurseContext {
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
     * Records whether `this` is a one-off [WakaItemStack] instance.
     *
     * A one-off [WakaItemStack] instance is effectively backed by a
     * strictly-Bukkit [ItemStack]. That is, the [handle] is a strictly-Bukkit
     * [ItemStack].
     *
     * It should be noted, once the [handle] of a one-off [WakaItemStack] has
     * been added to the underlying game world, any changes to the one-off
     * [WakaItemStack] **will not** reflect to that one in the underlying game
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
     * The "wakame" [CompoundTag][CompoundShadowTag] of this item.
     *
     * This **does not** include any other tags which are **not** part of the
     * wakame item NBT specifications, such as display name, enchantment and
     * durability, which are already accessible via Paper API. To get access to
     * these tags, just use the wrapped [handle].
     */
    @InternalApi
    val tags: CompoundShadowTag // 外部不应该读取该变量

    /**
     * Returns `true` if this item is a wakame item.
     *
     * In this case [isNotWakame] returns `false`.
     */
    val isWakame: Boolean

    /**
     * Returns `true` if this item is not a wakame item.
     *
     * In this case [isWakame] returns `false`.
     */
    val isNotWakame: Boolean

    /**
     * The corresponding [WakaItem] scheme.
     *
     * @throws NullPointerException if this is not a legal wakame item
     */
    val scheme: WakaItem

    /**
     * The namespace of this item.
     *
     * @throws NullPointerException if this is not a legal wakame item
     */
    val namespace: String

    /**
     * The ID of this item.
     *
     * @throws NullPointerException if this is not a legal wakame item
     */
    val id: String

    /**
     * The [namespaced ID][Key] of this item.
     *
     * @throws NullPointerException if this is not a legal wakame item
     */
    val key: Key

    /**
     * The UUID of this item.
     *
     * @throws NullPointerException if this is not a legal wakame item
     */
    val uuid: UUID

    /**
     * The CellMap of this item.
     *
     * Used to manipulate the **cells** of this item.
     *
     * @throws NullPointerException if this is not a legal wakame item
     */
    val cellAccessor: CellAccessor

    /**
     * The ItemMetaMap of this item.
     *
     * Used to manipulate the **meta** of this item.
     *
     * @throws NullPointerException if this is not a legal wakame item
     */
    val metaAccessor: ItemMetaAccessor

    /**
     * The ItemStatsMap of this item.
     *
     * Used to manipulate the **statistics** of this item.
     *
     * @throws NullPointerException if this is not a legal wakame item
     */
    val statsAccessor: ItemStatsAccessor

    override fun hashCode(): Int
    override fun equals(other: Any?): Boolean
}