package cc.mewcraft.wakame.attack

import cc.mewcraft.wakame.attribute.Attributes
import cc.mewcraft.wakame.damage.*
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.applyAttackCooldown
import cc.mewcraft.wakame.player.interact.WrappedPlayerInteractEvent
import cc.mewcraft.wakame.user.toUser
import org.bukkit.FluidCollisionMode
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityDamageEvent

/**
 * 自定义矛攻击.
 * 左键后对玩家视线所指方向的所有实体进行攻击.
 * 攻击的距离上限由属性 [cc.mewcraft.wakame.attribute.Attributes.ENTITY_INTERACTION_RANGE] 决定.
 *
 * ## Node structure
 * ```yaml
 * <node>:
 *   type: spear
 *   size: <double>
 * ```
 */
data class SpearAttack(
    val size: Double,
) : AttackType {
    companion object {
        const val NAME = "spear"
    }

    override fun handleDirectMeleeAttackEntity(player: Player, nekoStack: NekoStack, event: EntityDamageEvent): DamageMetadata? {
        val user = player.toUser()
        if (user.attackSpeed.isActive(nekoStack.id)) {
            return null
        }

        applyAttack(player)

        // 攻击冷却
        nekoStack.applyAttackCooldown(player)
        // TODO 扣除耐久

        return null
    }

    override fun handleInteract(player: Player, nekoStack: NekoStack, action: Action, wrappedEvent: WrappedPlayerInteractEvent) {
        if (!action.isLeftClick) return
        val user = player.toUser()
        if (user.attackSpeed.isActive(nekoStack.id)) return

        applyAttack(player)
        wrappedEvent.actionPerformed = true

        // 攻击冷却
        nekoStack.applyAttackCooldown(player)
        // TODO 扣除耐久
    }

    private fun applyAttack(damager: Player) {
        val world = damager.world
        val maxHitAmount = 100
        val hitEntities: MutableList<LivingEntity> = mutableListOf()
        val attributeMap = damager.toUser().attributeMap
        val maxDistance = attributeMap.getValue(Attributes.ENTITY_INTERACTION_RANGE)
        for (i in 0 until maxHitAmount) {
            val rayTraceResult = world.rayTrace(
                damager.eyeLocation,
                damager.eyeLocation.direction,
                maxDistance,
                FluidCollisionMode.NEVER,
                true,
                size
            ) {
                it is LivingEntity && it.uniqueId != damager.uniqueId && !hitEntities.contains(it)
            }
            if (rayTraceResult == null) {
                break
            }
            if (rayTraceResult.hitBlock != null) {
                break
            }
            val hitEntity = rayTraceResult.hitEntity
            if (hitEntity != null) {
                if (hitEntity is LivingEntity) {
                    hitEntities.add(hitEntity)
                }
            }
        }

        val damageTags = DamageTags(DamageTag.MELEE, DamageTag.SPEAR)
        hitEntities.forEach {
            val playerDamageMetadata = PlayerDamageMetadata(
                damager = damager,
                damageBundle = damageBundle(attributeMap) { every { standard() } },
                damageTags = damageTags
            )
            it.hurt(playerDamageMetadata, damager, true)
        }
    }
}