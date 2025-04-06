@file:JvmName("UserUtils")

package cc.mewcraft.wakame.user

import cc.mewcraft.wakame.ability2.combo.PlayerCombo
import cc.mewcraft.wakame.entity.player.AttackSpeed
import org.bukkit.entity.Player


val Player.combo: PlayerCombo
    get() = toUser().combo

val Player.attackSpeed: AttackSpeed
    get() = toUser().attackSpeed

/**
 * Adapts the [Player] into [NekoPlayer][User].
 */
@Deprecated("User<*> will be completely replaced by extension functions")
fun Player.toUser(): User<Player> {
    return PlayerAdapters.get<Player>().adapt(this)
}