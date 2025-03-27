package cc.mewcraft.wakame.attack

import cc.mewcraft.wakame.attribute.Attributes
import cc.mewcraft.wakame.damage.DamageMetadata
import cc.mewcraft.wakame.damage.PlayerDamageMetadata
import cc.mewcraft.wakame.damage.damageBundle
import cc.mewcraft.wakame.damage.hurt
import cc.mewcraft.wakame.event.bukkit.NekoPreprocessDamageEvent
import cc.mewcraft.wakame.extensions.*
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.extension.applyAttackCooldown
import cc.mewcraft.wakame.item.extension.damageItemStack2
import cc.mewcraft.wakame.player.interact.WrappedPlayerInteractEvent
import cc.mewcraft.wakame.player.itemdamage.ItemDamageEventMarker
import cc.mewcraft.wakame.user.attackSpeed
import cc.mewcraft.wakame.util.adventure.BukkitSound
import cc.mewcraft.wakame.util.adventure.SoundSource
import cc.mewcraft.wakame.util.adventure.playSound
import com.destroystokyo.paper.ParticleBuilder
import org.bukkit.FluidCollisionMode
import org.bukkit.Particle
import org.bukkit.World
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerItemDamageEvent
import org.bukkit.inventory.EquipmentSlot
import org.joml.Vector3f

/**
 * 自定义矛攻击.
 * 左键后对玩家视线所指方向的所有生物进行攻击.
 * 攻击的距离上限由属性 [cc.mewcraft.wakame.attribute.Attributes.ENTITY_INTERACTION_RANGE] 决定.
 *
 * ## Node structure
 * ```yaml
 * <node>:
 *   type: spear
 *   size: <double>
 * ```
 */
class SpearAttack(
    private val cancelVanillaDamage: Boolean,
    private val size: Double,
) : AttackType {
    companion object {
        const val NAME = "spear"
        const val MAX_HIT_AMOUNT = 100
    }

    override fun generateDamageMetadata(itemstack: NekoStack, event: NekoPreprocessDamageEvent): DamageMetadata? {
        val player = event.damager
        if (player.attackSpeed.isActive(itemstack.id)) return null
        val playerAttributes = event.damagerAttributes
        val directDamageMetadata = PlayerDamageMetadata(
            attributes = playerAttributes,
            damageBundle = damageBundle(playerAttributes) {
                every { standard() }
            }
        )

        return directDamageMetadata
    }

    override fun handleAttackEntity(itemstack: NekoStack, event: NekoPreprocessDamageEvent) {
        val player = event.damager
        if (player.attackSpeed.isActive(itemstack.id)) return
        val damagee = event.damagee!!

        applySpearAttack(player, damagee)

        itemstack.applyAttackCooldown(player)
        player.damageItemStack2(EquipmentSlot.HAND, 1)
    }

    override fun handleInteract(player: Player, itemstack: NekoStack, action: Action, wrappedEvent: WrappedPlayerInteractEvent) {
        if (!action.isLeftClick) return
        if (player.attackSpeed.isActive(itemstack.id)) return

        applySpearAttack(player)

        itemstack.applyAttackCooldown(player)
        player.damageItemStack2(EquipmentSlot.HAND, 1)
        wrappedEvent.actionPerformed = true
    }

    override fun handleDamage(player: Player, itemstack: NekoStack, event: PlayerItemDamageEvent) {
        if (cancelVanillaDamage && ItemDamageEventMarker.isAlreadyDamaged(player)) {
            event.isCancelled = true
        }
    }

    private fun applySpearAttack(player: Player, vararg excludeEntities: LivingEntity) {
        val preprocessEvent = NekoPreprocessDamageEvent(player, null, null).apply { callEvent() }
        val playerAttributes = preprocessEvent.damagerAttributes

        var end: Vector3f? = null
        val world = player.world
        val hitEntities = mutableListOf<LivingEntity>()
        val maxDistance = playerAttributes.getValue(Attributes.ENTITY_INTERACTION_RANGE)
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

        hitEntities.forEach { xdamagee ->
            val playerDamageMetadata = PlayerDamageMetadata(
                attributes = playerAttributes,
                damageBundle = damageBundle(playerAttributes) {
                    every { standard() }
                },
            )
            xdamagee.hurt(playerDamageMetadata, player, true)
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
        world.playSound(player) {
            type(BukkitSound.ENTITY_ARROW_SHOOT)
            source(SoundSource.PLAYER)
            pitch(1f)
            volume(1f)
        }
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