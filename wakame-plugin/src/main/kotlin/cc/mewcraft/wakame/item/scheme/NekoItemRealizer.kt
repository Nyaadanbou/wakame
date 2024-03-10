package cc.mewcraft.wakame.item.scheme

import cc.mewcraft.wakame.crate.BinaryCrate
import cc.mewcraft.wakame.item.binary.NekoItemStack
import org.bukkit.entity.Player

// TODO create a mock for it

/**
 * Realizes [NekoItem] into a usable item which then can be added to the game world.
 */
interface NekoItemRealizer {

    /**
     * Realizes an item template against a player.
     *
     * @param nekoItem the item template
     * @param player the player which the realization is against
     * @return a usable item
     */
    fun realize(nekoItem: NekoItem, player: Player): NekoItemStack

    /**
     * Realizes an item template against a crate.
     *
     * @param nekoItem the item template
     * @param crate the crate which the realization is against
     * @return a usable item
     */
    fun realize(nekoItem: NekoItem, crate: BinaryCrate): NekoItemStack

}