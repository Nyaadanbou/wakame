package cc.mewcraft.wakame.attack

import cc.mewcraft.wakame.damage.*
import cc.mewcraft.wakame.entity.attribute.Attributes
import cc.mewcraft.wakame.entity.player.attributeContainer
import cc.mewcraft.wakame.event.bukkit.NekoEntityDamageEvent
import cc.mewcraft.wakame.event.bukkit.WrappedPlayerInteractEvent
import cc.mewcraft.wakame.extensions.*
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.extension.applyAttackCooldown
import cc.mewcraft.wakame.item.extension.damageItemStack2
import cc.mewcraft.wakame.item2.ItemDamageEventMarker
import cc.mewcraft.wakame.user.attackSpeed
import com.destroystokyo.paper.ParticleBuilder
import org.bukkit.*
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerItemDamageEvent
import org.bukkit.inventory.EquipmentSlot
import org.joml.Vector3f

/**
 * 自定义矛攻击.
 * 左键后对玩家视线所指方向的所有生物进行攻击.
 * 攻击的距离上限由属性 [Attributes.ENTITY_INTERACTION_RANGE] 决定.
 *
 * ## Node structure
 * ```yaml
 * <node>:
 *   type: spear
 *   size: <double>
 * ```
 */
data class SpearAttack(
    private val cancelVanillaDamage: Boolean,
    private val size: Double,
) : AttackType {
    companion object {
        const val NAME = "spear"
        const val MAX_HIT_AMOUNT = 100
    }

    override fun handleDamage(player: Player, nekoStack: NekoStack, event: PlayerItemDamageEvent) {
        if (cancelVanillaDamage && ItemDamageEventMarker.isAlreadyDamaged(player)) {
            event.isCancelled = true
        }
    }

    override fun generateDamageMetadata(player: Player, nekoStack: NekoStack): DamageMetadata? {
        if (player.attackSpeed.isActive(nekoStack.id)) {
            return null
        }

        val attributes = player.attributeContainer
        val damageMeta = PlayerDamageMetadata(
            attributes = attributes,
            damageBundle = damageBundle(attributes) {
                every {
                    standard()
                }
            }
        )

        return damageMeta
    }

    override fun handleAttackEntity(player: Player, nekoStack: NekoStack, damagee: LivingEntity, event: NekoEntityDamageEvent) {
        if (!event.damageMetadata.damageTags.contains(DamageTag.DIRECT)) {
            return
        }

        if (player.attackSpeed.isActive(nekoStack.id)) {
            return
        }

        applySpearAttack(player, damagee)

        // 应用攻击冷却
        nekoStack.applyAttackCooldown(player)
        // 扣除耐久
        player.damageItemStack2(EquipmentSlot.HAND, 1)
    }

    override fun handleInteract(player: Player, nekoStack: NekoStack, action: Action, wrappedEvent: WrappedPlayerInteractEvent) {
        if (!action.isLeftClick) return
        if (player.attackSpeed.isActive(nekoStack.id)) return

        applySpearAttack(player)

        // 应用攻击冷却
        nekoStack.applyAttackCooldown(player)
        // 扣除耐久
        player.damageItemStack2(EquipmentSlot.HAND, 1)

        wrappedEvent.actionPerformed = true
    }

    private fun applySpearAttack(player: Player, vararg excludeEntities: LivingEntity) {
        val world = player.world
        val hitEntities = mutableListOf<LivingEntity>()
        val attributes = player.attributeContainer
        val maxDistance = attributes.getValue(Attributes.ENTITY_INTERACTION_RANGE)

        var end: Vector3f? = null
        for (i in 0 until MAX_HIT_AMOUNT) {
            val rayTraceResult = world.rayTrace(
                player.eyeLocation,
                player.eyeLocation.direction,
                maxDistance,
                FluidCollisionMode.NEVER,
                true,
                size
            ) {
                it is LivingEntity && it != player && !hitEntities.contains(it) && !excludeEntities.contains(it)
            }
            if (rayTraceResult == null) {
                break
            }
            if (rayTraceResult.hitBlock != null) {
                end = rayTraceResult.hitPosition.toVector3f()
                break
            }
            val hitEntity = rayTraceResult.hitEntity
            if (hitEntity != null) {
                if (hitEntity is LivingEntity) {
                    hitEntities.add(hitEntity)
                }
            }
        }

        hitEntities.forEach { victim ->
            val playerDamageMetadata = PlayerDamageMetadata(
                attributes = attributes,
                damageBundle = damageBundle(attributes) {
                    every {
                        standard()
                    }
                },
            )
            victim.hurt(playerDamageMetadata, player, true)
        }

        // 特效
        if (end == null) {
            end = player.eyeLocation.toVector3f() + (player.eyeLocation.direction.toVector3f().normalize(maxDistance.toFloat()))
        }
        particleLine(
            world,
            player.eyeLocation.toVector3f(),
            end,
            ParticleBuilder(Particle.CRIT).extra(0.0)
        )
        ParticleBuilder(Particle.SWEEP_ATTACK)
            .location(end.toLocation(world))
            .receivers(64)
            .spawn()
        world.playSound(player.location, Sound.ENTITY_ARROW_SHOOT, SoundCategory.PLAYERS, 1F, 1F)
    }

    private fun particleLine(world: World, start: Vector3f, end: Vector3f, particleBuilder: ParticleBuilder) {
        val distance = end.distance(start)
        val num = (distance / 0.2).toInt()
        for (i in 1..num) {
            val t = (i - 1).toFloat() / num
            particleBuilder.location(
                (start + ((end - start) mul t)).toLocation(world)
            ).receivers(64).spawn()
        }
    }
}