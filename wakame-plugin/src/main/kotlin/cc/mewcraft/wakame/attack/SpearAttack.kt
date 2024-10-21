package cc.mewcraft.wakame.attack

import cc.mewcraft.wakame.attribute.Attributes
import cc.mewcraft.wakame.damage.*
import cc.mewcraft.wakame.event.NekoEntityDamageEvent
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.user.toUser
import org.bukkit.FluidCollisionMode
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import kotlin.math.absoluteValue
import kotlin.random.Random

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
    val size: Double
) : AttackType {
    companion object {
        const val NAME = "spear"
    }

    override fun handleAttackEntity(player: Player, nekoStack: NekoStack, damagee: Entity, event: NekoEntityDamageEvent) {
        if (event.damageMetadata is VanillaDamageMetadata) {
            event.isCancelled = true
            applyAttack(player)
        }
    }

    override fun handleDirectMeleeAttackEntity(player: Player, nekoStack: NekoStack, event: EntityDamageByEntityEvent): DamageMetadata? {

    }

    override fun handleInteract(player: Player, nekoStack: NekoStack, action: Action, event: PlayerInteractEvent) {
        if (!action.isLeftClick) return
        applyAttack(player)
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

        val chance = attributeMap.getValue(Attributes.CRITICAL_STRIKE_CHANCE)
        val criticalPower = if (chance < 0) {
            attributeMap.getValue(Attributes.NEGATIVE_CRITICAL_STRIKE_POWER)
        } else {
            attributeMap.getValue(Attributes.CRITICAL_STRIKE_POWER)
        }
        val damageTags = DamageTags(DamageTag.MELEE, DamageTag.SPEAR)
        hitEntities.forEach {
            val criticalStrikeState: CriticalStrikeState = if (chance < 0) {
                if (Random.nextDouble() < chance.absoluteValue) {
                    CriticalStrikeState.NEGATIVE
                } else {
                    CriticalStrikeState.NONE
                }
            } else {
                if (Random.nextDouble() < chance) {
                    CriticalStrikeState.POSITIVE
                } else {
                    CriticalStrikeState.NONE
                }
            }
            val customDamageMetadata = CustomDamageMetadata(
                criticalPower = criticalPower,
                criticalStrikeState = criticalStrikeState,
                knockback = true,
                damageBundle = damageBundle(attributeMap) { every { standard() } },
                damageTags = damageTags
            )
            it.hurt(customDamageMetadata, damager)
        }
    }
}