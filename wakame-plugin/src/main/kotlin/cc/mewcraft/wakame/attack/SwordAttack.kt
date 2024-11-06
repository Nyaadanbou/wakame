package cc.mewcraft.wakame.attack

import cc.mewcraft.wakame.damage.*
import cc.mewcraft.wakame.event.NekoEntityDamageEvent
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.applyAttackCooldown
import cc.mewcraft.wakame.user.toUser
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot

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

    override fun generateDamageMetadata(player: Player, nekoStack: NekoStack): DamageMetadata? {
        val user = player.toUser()
        if (user.attackSpeed.isActive(nekoStack.id)) {
            return null
        }

        return PlayerDamageMetadata(
            user = user,
            damageTags = DamageTags(DamageTag.DIRECT, DamageTag.MELEE, DamageTag.SWORD),
            damageBundle = damageBundle(user.attributeMap) { every { standard() } }
        )
    }

    override fun handleAttackEntity(player: Player, nekoStack: NekoStack, damagee: LivingEntity, event: NekoEntityDamageEvent) {
        if (!event.damageMetadata.damageTags.contains(DamageTag.DIRECT)) return

        val user = player.toUser()
        if (user.attackSpeed.isActive(nekoStack.id)) {
            return
        }

        // 应用攻击冷却
        nekoStack.applyAttackCooldown(player)
        // 扣除耐久
        player.damageItemStack(EquipmentSlot.HAND, 1)
    }
}