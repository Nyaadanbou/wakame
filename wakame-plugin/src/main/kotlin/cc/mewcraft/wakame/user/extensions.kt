@file:JvmName("UserUtils")

package cc.mewcraft.wakame.user

import cc.mewcraft.wakame.ability2.combo.PlayerCombo
import cc.mewcraft.wakame.ecs.bridge.koishify
import cc.mewcraft.wakame.entity.attribute.AttributeMap
import cc.mewcraft.wakame.kizami.KizamiMap
import cc.mewcraft.wakame.player.attackspeed.AttackSpeed
import org.bukkit.entity.Player

val Player.koishLevel: Int
    get() = toUser().level

val Player.kizamiContainer: KizamiMap
    get() = toUser().kizamiMap

val Player.attributeContainer: AttributeMap
    get() = koishify()[AttributeMap]

val Player.combo: PlayerCombo
    get() = toUser().combo

val Player.attackSpeed: AttackSpeed
    get() = toUser().attackSpeed

val Player.isInventoryListenable: Boolean
    get() = toUser().isInventoryListenable

/**
 * Adapts the [Player] into [NekoPlayer][User].
 */
fun Player.toUser(): User<Player> {
    return PlayerAdapters.get<Player>().adapt(this)
}