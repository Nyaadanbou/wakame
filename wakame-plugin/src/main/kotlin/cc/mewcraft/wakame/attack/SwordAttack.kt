package cc.mewcraft.wakame.attack

import cc.mewcraft.wakame.damage.*
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.applyAttackCooldown
import cc.mewcraft.wakame.user.toUser
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageEvent

/**
 * 原版剑横扫攻击.
 *
 * ## Node structure
 * ```yaml
 * <node>:
 *   type: sword
 * ```
 */
class SwordAttack : AttackType {
    companion object {
        const val NAME = "sword"
    }

    override fun handleDirectMeleeAttackEntity(player: Player, nekoStack: NekoStack, event: EntityDamageEvent): DamageMetadata? {
        val user = player.toUser()
        if (user.attackSpeed.isActive(nekoStack.id)) {
            return null
        } else {
            nekoStack.applyAttackCooldown(player)
        }

        return PlayerDamageMetadata(
            damager = player,
            damageTags = DamageTags(DamageTag.MELEE, DamageTag.SWORD),
            damageBundle = damageBundle(player.toUser().attributeMap) { every { standard() } }
        )
    }
}