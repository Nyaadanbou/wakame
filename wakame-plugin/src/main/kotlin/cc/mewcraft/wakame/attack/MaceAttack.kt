package cc.mewcraft.wakame.attack

import cc.mewcraft.wakame.damage.*
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.user.toUser
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageEvent

/**
 * 原版重锤攻击.
 *
 * ## Node structure
 * ```yaml
 * <node>:
 *   type: mace
 * ```
 */
class MaceAttack : AttackType {
    companion object {
        const val NAME = "mace"
    }

    override fun handleDirectMeleeAttackEntity(player: Player, nekoStack: NekoStack, event: EntityDamageEvent): DamageMetadata? {
        return PlayerDamageMetadata(
            damager = player,
            damageTags = DamageTags(DamageTag.MELEE, DamageTag.MACE),
            damageBundle = damageBundle(player.toUser().attributeMap) { every { standard() } }
        )
    }
}