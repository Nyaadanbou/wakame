package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.crate.Crate
import cc.mewcraft.wakame.item.binary.PlayNekoStack
import cc.mewcraft.wakame.item.template.GenerationContext
import cc.mewcraft.wakame.user.User

/**
 * Realizes [NekoItem] into an item which then can be added to the game world.
 */
interface NekoItemRealizer {

    /**
     * Realizes an item template from a custom trigger.
     *
     * @param item the item template
     * @param context the context on which the realization is based
     * @return a one-off NekoStack
     */
    fun realize(item: NekoItem, context: GenerationContext): PlayNekoStack

    /**
     * Realizes an item template from a player.
     *
     * @param item the item template
     * @param user the player on which the realization is based
     * @return a one-off NekoStack
     */
    fun realize(item: NekoItem, user: User<*>): PlayNekoStack

    /**
     * Realizes an item template from a crate.
     *
     * @param item the item template
     * @param crate the crate on which the realization is based
     * @return a one-off NekoStack
     */
    fun realize(item: NekoItem, crate: Crate): PlayNekoStack

}