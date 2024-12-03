package cc.mewcraft.wakame.attack

import cc.mewcraft.wakame.attribute.Attributes
import cc.mewcraft.wakame.damage.DamageMetadata
import cc.mewcraft.wakame.damage.DamageTag
import cc.mewcraft.wakame.damage.DamageTags
import cc.mewcraft.wakame.damage.PlayerDamageMetadata
import cc.mewcraft.wakame.damage.damageBundle
import cc.mewcraft.wakame.damage.hurt
import cc.mewcraft.wakame.event.NekoEntityDamageEvent
import cc.mewcraft.wakame.extensions.minus
import cc.mewcraft.wakame.extensions.mul
import cc.mewcraft.wakame.extensions.plus
import cc.mewcraft.wakame.extensions.toLocation
import cc.mewcraft.wakame.extensions.toVector3f
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.applyAttackCooldown
import cc.mewcraft.wakame.item.damageItemStack2
import cc.mewcraft.wakame.player.interact.WrappedPlayerInteractEvent
import cc.mewcraft.wakame.player.itemdamage.ItemDamageEventMarker
import cc.mewcraft.wakame.user.toUser
import com.destroystokyo.paper.ParticleBuilder
import org.bukkit.FluidCollisionMode
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.SoundCategory
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
        val user = player.toUser()
        if (user.attackSpeed.isActive(nekoStack.id)) {
            return null
        }

        val attributeMap = user.attributeMap
        val directDamageMetadata = PlayerDamageMetadata(
            user = user,
            damageTags = DamageTags(DamageTag.DIRECT, DamageTag.MELEE, DamageTag.SPEAR),
            damageBundle = damageBundle(attributeMap) {
                every { standard() }
            }
        )

        return directDamageMetadata
    }

    override fun handleAttackEntity(player: Player, nekoStack: NekoStack, damagee: LivingEntity, event: NekoEntityDamageEvent) {
        if (!event.damageMetadata.damageTags.contains(DamageTag.DIRECT)) {
            return
        }

        val user = player.toUser()
        if (user.attackSpeed.isActive(nekoStack.id)) {
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
        if (player.toUser().attackSpeed.isActive(nekoStack.id)) return

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
        val user = player.toUser()
        val attributeMap = user.attributeMap
        val maxDistance = attributeMap.getValue(Attributes.ENTITY_INTERACTION_RANGE)

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
                user = user,
                damageBundle = damageBundle(attributeMap) { every { standard() } },
                damageTags = DamageTags(DamageTag.MELEE, DamageTag.SPEAR)
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
        for (i in 0..num) {
            val t = i.toFloat() / num
            particleBuilder.location(
                (start + ((end - start) mul t)).toLocation(world)
            ).receivers(64).spawn()
        }
    }
}