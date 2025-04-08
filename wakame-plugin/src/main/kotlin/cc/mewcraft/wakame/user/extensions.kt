@file:JvmName("UserUtils")

package cc.mewcraft.wakame.user

import org.bukkit.entity.Player


/**
 * Adapts the [Player] into [NekoPlayer][User].
 */
@Deprecated("User<*> will be completely replaced by extension functions")
fun Player.toUser(): User<Player> {
    return PlayerAdapters.get<Player>().adapt(this)
}