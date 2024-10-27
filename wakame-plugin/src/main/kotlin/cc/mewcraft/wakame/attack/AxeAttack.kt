package cc.mewcraft.wakame.attack

import cc.mewcraft.wakame.damage.*
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.applyAttackCooldown
import cc.mewcraft.wakame.user.toUser
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageEvent

/**
 * 原版斧单体攻击.
 *
 * ## Node structure
 * ```yaml
 * <node>:
 *   type: axe
 * ```
 */
class AxeAttack : AttackType {
    companion object {
        const val NAME = "axe"
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
            damageTags = DamageTags(DamageTag.MELEE, DamageTag.AXE),
            damageBundle = damageBundle(player.toUser().attributeMap) { every { standard() } }
        )
    }
}