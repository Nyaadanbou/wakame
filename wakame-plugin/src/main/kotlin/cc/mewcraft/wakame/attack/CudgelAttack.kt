package cc.mewcraft.wakame.attack

import cc.mewcraft.wakame.damage.*
import cc.mewcraft.wakame.entity.attribute.Attributes
import cc.mewcraft.wakame.entity.player.attributeContainer
import cc.mewcraft.wakame.entity.player.itemCooldownContainer
import cc.mewcraft.wakame.event.bukkit.PostprocessDamageEvent
import cc.mewcraft.wakame.event.bukkit.WrappedPlayerInteractEvent
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.extension.applyAttackCooldown
import cc.mewcraft.wakame.item.extension.damageItemStack2
import cc.mewcraft.wakame.item2.ItemDamageEventMarker
import com.destroystokyo.paper.ParticleBuilder
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerItemDamageEvent
import org.bukkit.inventory.EquipmentSlot

/**
 * 自定义棍攻击.
 * 左键后对以玩家为中心的长方体区域内的所有生物进行攻击.
 * 长方体区域 x 轴 和 z 轴边长由属性 [Attributes.ENTITY_INTERACTION_RANGE] 决定.
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

    override fun handleDamage(player: Player, nekoStack: NekoStack, event: PlayerItemDamageEvent) {
        if (cancelVanillaDamage && ItemDamageEventMarker.isAlreadyDamaged(player)) {
            event.isCancelled = true
        }
    }

    override fun generateDamageMetadata(player: Player, nekoStack: NekoStack): DamageMetadata? {
        if (player.itemCooldownContainer.isActive(nekoStack.id)) {
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

    override fun handleAttackEntity(player: Player, nekoStack: NekoStack, damagee: LivingEntity, event: PostprocessDamageEvent) {
        if (!event.damageMetadata.damageTags.contains(DamageTag.DIRECT)) {
            return
        }

        if (player.itemCooldownContainer.isActive(nekoStack.id)) {
            return
        }

        applyCudgelAttack(player, damagee)

        // 应用攻击冷却
        nekoStack.applyAttackCooldown(player)
        // 扣除耐久
        player.damageItemStack2(EquipmentSlot.HAND, 1)
    }

    override fun handleInteract(player: Player, nekoStack: NekoStack, action: Action, wrappedEvent: WrappedPlayerInteractEvent) {
        if (!action.isLeftClick) return
        if (player.itemCooldownContainer.isActive(nekoStack.id)) return

        applyCudgelAttack(player)

        // 应用攻击冷却
        nekoStack.applyAttackCooldown(player)
        // 扣除耐久
        player.damageItemStack2(EquipmentSlot.HAND, 1)
    }

    private fun applyCudgelAttack(player: Player, vararg excludeEntities: LivingEntity) {
        val world = player.world
        val attributes = player.attributeContainer
        val radius = attributes.getValue(Attributes.ENTITY_INTERACTION_RANGE).coerceIn(0.0, MAX_RADIUS)
        // 范围伤害目标的判定为:
        // 以玩家的脚部坐标为起点,
        // 向 y 轴正方向(上方) 1 格
        // 向 x 轴, z 轴正负方向各 radius 格的长方体所碰撞到的生物
        player.location.add(.0, .5, .0).getNearbyLivingEntities(radius, .5) {
            it != player && !excludeEntities.contains(it)
        }.forEach { victim ->
            val damageMeta = PlayerDamageMetadata(
                attributes = attributes,
                damageBundle = damageBundle(attributes) {
                    every {
                        standard()
                    }
                }
            )
            victim.hurt(damageMeta, player, true)
        }

        ParticleBuilder(Particle.SWEEP_ATTACK)
            .location(player.location.add(.0, 1.0, .0))
            .receivers(64)
            .spawn()
        world.playSound(player.location, Sound.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.PLAYERS, 1F, 1F)

    }
}