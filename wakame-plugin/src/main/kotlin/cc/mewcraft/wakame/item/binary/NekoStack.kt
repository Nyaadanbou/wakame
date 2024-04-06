package cc.mewcraft.wakame.item.binary

import cc.mewcraft.wakame.item.EffectiveSlot
import cc.mewcraft.wakame.item.ItemBehaviorAccessor
import cc.mewcraft.wakame.item.binary.cell.ItemCellAccessor
import cc.mewcraft.wakame.item.binary.meta.BinaryItemMeta
import cc.mewcraft.wakame.item.binary.meta.ItemMetaAccessor
import cc.mewcraft.wakame.item.binary.meta.getAccessor
import cc.mewcraft.wakame.item.binary.stats.ItemStatisticsAccessor
import cc.mewcraft.wakame.item.schema.NekoItem
import cc.mewcraft.wakame.item.schema.behavior.ItemBehavior
import net.kyori.adventure.key.Key
import org.bukkit.inventory.ItemStack
import java.util.UUID
import kotlin.reflect.KClass
import kotlin.reflect.full.isSuperclassOf

/**
 * A wrapper of [ItemStack] which is created from a [NekoItem].
 *
 * To get an instance of [NekoStack], use [NekoStackFactory].
 *
 * This class provides several properties to work with the underlying item
 * stack, mainly manipulating the non-vanilla NBT tags, such as:
 * - checking whether the item is a neko item
 * - look up the identifier of the neko item
 */
interface NekoStack : NekoStackSetter, ItemBehaviorAccessor {
    /**
     * The wrapped [ItemStack].
     *
     * The item stack may or may not be backed by a NMS object.
     *
     * ## When it is backed by a NMS object
     *
     * Any changes on `this` will reflect on the underlying game world.
     *
     * ## When it is backed by a strictly-Bukkit object
     *
     * Any changes on `this` will **not** reflect on the underlying game
     * world (if you've already added this item to the world). In such case,
     * `this` is primarily used to add a new [ItemStack] to the underlying
     * game world, such as giving it to players and dropping it on the ground.
     *
     * @see isNmsBacked
     */
    val handle: ItemStack

    /**
     * Checks if `this` NekoStack is backed by an NMS object.
     *
     * If this returns `true`, that means the [handle] is backed by an NMS object.
     *
     * If this returns `false`, that means the [handle] is a strictly-Bukkit [ItemStack].
     *
     * It should be noted that the server implementation always makes a NMS copy
     * out of the strictly-Bukkit [ItemStack] when the item is being added to
     * the underlying world states.
     *
     * However, this may not hold if the Paper
     * team finish up the ItemStack overhaul:
     * [Interface ItemStacks](https://github.com/orgs/PaperMC/projects/6#).
     * At that time, this property will probably no longer be needed.
     *
     * Please keep an eye on this kdoc. I will add notes to here as soon as
     * anything has changed.
     *
     * @see handle
     */
    val isNmsBacked: Boolean

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
     * The corresponding [NekoItem] schema.
     *
     * @throws NullPointerException if this is not a legal neko item
     */
    val schema: NekoItem

    /**
     * The random seed from which this item is generated.
     *
     * @throws NullPointerException if this is not a legal neko item
     */
    val seed: Long

    /**
     * The [namespaced ID][Key] of this item.
     *
     * @throws NullPointerException if this is not a legal neko item
     */
    val key: Key

    /**
     * The variant of this item.
     *
     * @throws NullPointerException if this is not a legal neko item
     */
    val variant: Int

    /**
     * The UUID of this item.
     *
     * @throws NullPointerException if this is not a legal neko item
     */
    val uuid: UUID

    /**
     * The inventory slot where this item becomes effective.
     *
     * @throws NullPointerException if this is not a legal neko item
     */
    val effectiveSlot: EffectiveSlot

    /**
     * The [ItemCellAccessor] of this item.
     *
     * Used to manipulate the **cells** of this item.
     *
     * @throws NullPointerException if this is not a legal neko item
     */
    val cell: ItemCellAccessor

    /**
     * The [ItemMetaAccessor] of this item.
     *
     * Used to manipulate the **meta** of this item.
     *
     * @throws NullPointerException if this is not a legal neko item
     */
    val meta: ItemMetaAccessor

    /**
     * The [ItemStatisticsAccessor] of this item.
     *
     * Used to manipulate the **statistics** of this item.
     *
     * @throws NullPointerException if this is not a legal neko item
     */
    val statistics: ItemStatisticsAccessor

    override fun <T : ItemBehavior> hasBehavior(behaviorClass: KClass<T>): Boolean {
        return schema.behaviors.any { behaviorClass.isSuperclassOf(it::class) }
    }

    override fun <T : ItemBehavior> getBehaviorOrNull(behaviorClass: KClass<T>): T? {
        @Suppress("UNCHECKED_CAST")
        return schema.behaviors.firstOrNull { behaviorClass.isSuperclassOf(it::class) } as T?
    }

    override fun <T : ItemBehavior> getBehavior(behaviorClass: KClass<T>): T {
        return getBehaviorOrNull(behaviorClass) ?: throw IllegalStateException("Item $key does not have a behavior of type ${behaviorClass.simpleName}")
    }
}

/**
 * Gets the data accessor of specific item meta.
 *
 * @see ItemMetaAccessor.getAccessor
 */
inline fun <reified M : BinaryItemMeta<*>> NekoStack.meta(): M {
    return this.meta.getAccessor<M>()
}
