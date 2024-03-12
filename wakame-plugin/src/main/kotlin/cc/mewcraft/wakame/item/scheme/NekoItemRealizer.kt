package cc.mewcraft.wakame.item.scheme

import cc.mewcraft.wakame.crate.Crate
import cc.mewcraft.wakame.item.binary.NekoItemStack
import cc.mewcraft.wakame.player.Player

// TODO create a mock for it

/**
 * Realizes [NekoItem] into an item which then can be added to the game world.
 */
interface NekoItemRealizer {

    /**
     * Realizes an item template against a player.
     *
     * @param nekoItem the item template
     * @param player the player on which the realization is based
     * @return a once-off NekoStack
     */
    fun realize(nekoItem: NekoItem, player: Player): NekoItemStack

    /**
     * Realizes an item template against a crate.
     *
     * @param nekoItem the item template
     * @param crate the crate on which the realization is based
     * @return a once-off NekoStack
     */
    fun realize(nekoItem: NekoItem, crate: Crate): NekoItemStack

}