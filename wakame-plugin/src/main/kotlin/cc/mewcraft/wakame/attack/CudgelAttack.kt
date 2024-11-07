package cc.mewcraft.wakame.attack

import cc.mewcraft.wakame.attribute.Attributes
import cc.mewcraft.wakame.damage.*
import cc.mewcraft.wakame.event.NekoEntityDamageEvent
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.applyAttackCooldown
import cc.mewcraft.wakame.player.interact.WrappedPlayerInteractEvent
import cc.mewcraft.wakame.user.toUser
import com.destroystokyo.paper.ParticleBuilder
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
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
class CudgelAttack : AttackType {
    companion object {
        const val NAME = "cudgel"
        const val MAX_RADIUS = 32.0
    }

    override fun generateDamageMetadata(player: Player, nekoStack: NekoStack): DamageMetadata? {
        val user = player.toUser()
        if (user.attackSpeed.isActive(nekoStack.id)) {
            return null
        }

        val attributeMap = user.attributeMap
        val directDamageMetadata = PlayerDamageMetadata(
            user = user,
            damageTags = DamageTags(DamageTag.DIRECT, DamageTag.MELEE, DamageTag.CUDGEL),
            damageBundle = damageBundle(attributeMap) {
                every {
                    standard()
                }
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

        applyCudgelAttack(player, damagee)

        // 应用攻击冷却
        nekoStack.applyAttackCooldown(player)
        // 扣除耐久
        player.damageItemStack(EquipmentSlot.HAND, 1)
    }

    override fun handleInteract(player: Player, nekoStack: NekoStack, action: Action, wrappedEvent: WrappedPlayerInteractEvent) {
        if (!action.isLeftClick) return
        if (player.toUser().attackSpeed.isActive(nekoStack.id)) return

        applyCudgelAttack(player)

        // 应用攻击冷却
        nekoStack.applyAttackCooldown(player)
        // 扣除耐久
        player.damageItemStack(EquipmentSlot.HAND, 1)
    }

    private fun applyCudgelAttack(player: Player, vararg excludeEntities: LivingEntity) {
        val world = player.world
        val user = player.toUser()
        val attributeMap = user.attributeMap
        val radius = attributeMap.getValue(Attributes.ENTITY_INTERACTION_RANGE).coerceIn(0.0, MAX_RADIUS)
        val damageTags = DamageTags(DamageTag.MELEE, DamageTag.CUDGEL)
        // 范围伤害目标的判定为:
        // 以玩家的脚部坐标为起点,
        // 向 y 轴正方向(上方) 1 格
        // 向 x 轴, z 轴正负方向各 radius 格的长方体所碰撞到的生物
        player.location.add(.0, .5, .0).getNearbyLivingEntities(radius, .5) {
            it != player && !excludeEntities.contains(it)
        }.forEach {
            val damageMetadata = PlayerDamageMetadata(
                user = user,
                damageTags = damageTags,
                damageBundle = damageBundle(attributeMap) {
                    every {
                        standard()
                    }
                }
            )
            it.hurt(damageMetadata, player, true)
        }

        ParticleBuilder(Particle.SWEEP_ATTACK)
            .location(player.location.add(.0, 1.0, .0))
            .receivers(64)
            .spawn()
        world.playSound(player.location, Sound.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.PLAYERS, 1F, 1F)

    }
}