package cc.mewcraft.wakame.attack

import cc.mewcraft.wakame.attribute.Attributes
import cc.mewcraft.wakame.damage.DamageMetadata
import cc.mewcraft.wakame.damage.PlayerDamageMetadata
import cc.mewcraft.wakame.damage.damageBundle
import cc.mewcraft.wakame.damage.hurt
import cc.mewcraft.wakame.event.bukkit.NekoPreprocessDamageEvent
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
import org.bukkit.Particle
import org.bukkit.damage.DamageSource
import org.bukkit.damage.DamageType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerItemDamageEvent
import org.bukkit.inventory.EquipmentSlot

/**
 * 自定义棍攻击.
 * 左键后对以玩家为中心的长方体区域内的所有生物进行攻击.
 * 长方体区域 x 轴 和 z 轴边长由属性 [cc.mewcraft.wakame.attribute.Attributes.ENTITY_INTERACTION_RANGE] 决定.
 *
 * ## Node structure
 * ```yaml
 * <node>:
 *   type: cudgel
 * ```
 */
class CudgelAttack(
    private val cancelVanillaDamage: Boolean,
) : AttackType {
    companion object {
        const val NAME = "cudgel"
        const val MAX_RADIUS = 32.0
    }

    override fun generateDamageMetadata(itemstack: NekoStack, event: NekoPreprocessDamageEvent): DamageMetadata? {
        val player = event.damager
        if (player.attackSpeed.isActive(itemstack.id)) return null
        val playerAttributes = event.damagerAttributes
        val directDamageMetadata = PlayerDamageMetadata(
            attributes = playerAttributes,
            damageBundle = damageBundle(playerAttributes) {
                every {
                    standard()
                }
            }
        )

        return directDamageMetadata
    }

    override fun handleAttackEntity(itemstack: NekoStack, event: NekoPreprocessDamageEvent) {
        val player = event.damager
        val damagee = event.damagee!!
        if (player.attackSpeed.isActive(itemstack.id)) return

        applyCudgelAttack(player, damagee)

        itemstack.applyAttackCooldown(player)
        player.damageItemStack2(EquipmentSlot.HAND, 1)
    }

    override fun handleInteract(player: Player, itemstack: NekoStack, action: Action, wrappedEvent: WrappedPlayerInteractEvent) {
        if (!action.isLeftClick) return
        if (player.attackSpeed.isActive(itemstack.id)) return

        applyCudgelAttack(player)

        itemstack.applyAttackCooldown(player)
        player.damageItemStack2(EquipmentSlot.HAND, 1)
    }

    override fun handleDamage(player: Player, itemstack: NekoStack, event: PlayerItemDamageEvent) {
        if (cancelVanillaDamage && ItemDamageEventMarker.isAlreadyDamaged(player)) {
            event.isCancelled = true
        }
    }

    private fun applyCudgelAttack(player: Player, vararg excludeEntities: LivingEntity) {
        // FIXME #366: 触发两次事件真的好吗? 有没有办法只触发一次事件?
        val preprocessEvent = NekoPreprocessDamageEvent(player, null, null).apply { callEvent() }
        val playerAttributes = preprocessEvent.damagerAttributes

        // 范围伤害目标的判定为:
        // 以玩家的脚部坐标为起点,
        // 向 y 轴正方向 (上方) 1 格
        // 向 x 轴, z 轴正负方向各 radius 格的长方体所碰撞到的生物
        val attackRadius = playerAttributes.getValue(Attributes.ENTITY_INTERACTION_RANGE).coerceIn(0.0, MAX_RADIUS)
        player.location.add(.0, .5, .0).getNearbyLivingEntities(attackRadius, .5) {
            it != player && !excludeEntities.contains(it)
        }.forEach { xdamagee ->
            val preprocessEvent = NekoPreprocessDamageEvent(
                player,
                xdamagee,
                DamageSource.builder(DamageType.GENERIC) // TODO 使用数据包添加的 damage_type
                    .withCausingEntity(player)
                    .withDirectEntity(player)
                    .withDamageLocation(xdamagee.location)
                    .build()
            ).apply { callEvent() }
            val playerAttributes2 = preprocessEvent.damagerAttributes
            val damageMetadata = PlayerDamageMetadata(
                attributes = playerAttributes2,
                damageBundle = damageBundle(playerAttributes) {
                    every {
                        standard()
                    }
                }
            )
            xdamagee.hurt(damageMetadata, player, true)
        }

        ParticleBuilder(Particle.SWEEP_ATTACK)
            .location(player.location.add(.0, 1.0, .0))
            .receivers(16)
            .spawn()
        player.playSound(player) {
            type(BukkitSound.ENTITY_PLAYER_ATTACK_SWEEP)
            source(SoundSource.PLAYER)
            pitch(1f)
            volume(1f)
        }
    }
}