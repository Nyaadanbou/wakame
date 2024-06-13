package cc.mewcraft.wakame.item.binary

import cc.mewcraft.wakame.item.binary.show.CustomDataAccessor
import org.bukkit.inventory.ItemStack

interface BukkitNekoStack : NekoStack {
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
     * Gets a [PlayNekoStack] representation.
     */
    val play: PlayNekoStack

    /**
     * Gets a [ShowNekoStack] representation.
     */
    val show: ShowNekoStack
}

/**
 * Represents a [NekoStack] which will ultimately be used by players.
 */
interface PlayNekoStack : BukkitNekoStack

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
interface ShowNekoStack : BukkitNekoStack {
    /**
     * The [CustomDataAccessor] of this item.
     */
    val customData: CustomDataAccessor
}
