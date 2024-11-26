package cc.mewcraft.wakame.attack

import cc.mewcraft.wakame.attribute.Attributes
import cc.mewcraft.wakame.damage.DamageMetadata
import cc.mewcraft.wakame.damage.DamageTag
import cc.mewcraft.wakame.damage.DamageTags
import cc.mewcraft.wakame.damage.PlayerDamageMetadata
import cc.mewcraft.wakame.damage.damageBundle
import cc.mewcraft.wakame.damage.hurt
import cc.mewcraft.wakame.event.NekoEntityDamageEvent
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.applyAttackCooldown
import cc.mewcraft.wakame.item.damageItemStackByMark
import cc.mewcraft.wakame.player.itemdamage.ItemDamageEventMarker
import cc.mewcraft.wakame.user.toUser
import com.destroystokyo.paper.ParticleBuilder
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerItemDamageEvent
import org.bukkit.inventory.EquipmentSlot

/**
 * 自定义锤攻击.
 * 对直接攻击到的实体造成面板伤害.
 * 对 锤击范围 内的实体造成 锤击威力*面板 的伤害.
 * 锤击范围由属性 [cc.mewcraft.wakame.attribute.Attributes.HAMMER_DAMAGE_RANGE] 决定.
 * 锤击威力由属性 [cc.mewcraft.wakame.attribute.Attributes.HAMMER_DAMAGE_RATIO] 决定.
 *
 * ## Node structure
 * ```yaml
 * <node>:
 *   type: hammer
 * ```
 */
class HammerAttack(
    private val cancelVanillaDamage: Boolean
) : AttackType {
    companion object {
        const val NAME = "hammer"
        const val MAX_RADIUS = 32.0
    }

    override fun handleDamage(player: Player, nekoStack: NekoStack, event: PlayerItemDamageEvent) {
        if (cancelVanillaDamage && ItemDamageEventMarker.isAlreadyDamaged(player)) {
            event.isCancelled = true
        }
    }

    override fun handleAttackEntity(player: Player, nekoStack: NekoStack, damagee: LivingEntity, event: NekoEntityDamageEvent) {
        if (!event.damageMetadata.damageTags.contains(DamageTag.DIRECT)) {
            return
        }

        val user = player.toUser()
        // 只要这个物品的内部冷却处于激活状态就不处理
        // 内部冷却不一定是攻速组件造成的
        if (user.attackSpeed.isActive(nekoStack.id)) {
            return
        }

        val attributeMap = user.attributeMap
        val hitLocation = damagee.location
        val radius = attributeMap.getValue(Attributes.HAMMER_DAMAGE_RANGE).coerceIn(0.0, MAX_RADIUS)
        val ratio = attributeMap.getValue(Attributes.HAMMER_DAMAGE_RATIO)
        val damageTags = DamageTags(DamageTag.MELEE, DamageTag.HAMMER)
        // 范围伤害目标的判定为:
        // 以直接受击实体的脚部坐标为起点,
        // 向 y 轴正方向(上方) 1 格
        // 向 x 轴, z 轴正负方向各 radius 格的长方体所碰撞到的生物
        hitLocation.clone().add(.0, .5, .0).getNearbyLivingEntities(radius, .5) {
            it != player && it != damagee
        }.forEach { victim ->
            // 锤子范围命中的生物的伤害元数据
            val extraDamageMetadata = PlayerDamageMetadata(
                user = user,
                damageTags = damageTags,
                damageBundle = damageBundle(attributeMap) {
                    every {
                        standard()
                        rate {
                            ratio * standard()
                        }
                    }
                }
            )
            victim.hurt(extraDamageMetadata, player, true)
        }

        // 特效
        val world = player.world
        val particleBuilder = ParticleBuilder(Particle.BLOCK)
            .count(3)
            .offset(0.3, 0.1, 0.3)
            .extra(0.15)
        // 计算正方形的左下角和右上角的坐标
        val bottomLeftX = hitLocation.x - radius
        val bottomLeftZ = hitLocation.z - radius
        val topRightX = hitLocation.x + radius
        val topRightZ = hitLocation.z + radius
        var x = bottomLeftX
        while (x <= topRightX) {
            var z = bottomLeftZ
            while (z <= topRightZ) {
                val currentLocation = Location(world, x, hitLocation.y, z)
                val blockData = Location(world, x, hitLocation.y - 1, z).block.blockData
                particleBuilder
                    .location(currentLocation)
                    .data(blockData)
                    .receivers(64)
                    .spawn()
                z += 0.5
            }
            x += 0.5
        }
        world.playSound(player.location, Sound.ITEM_MACE_SMASH_GROUND, SoundCategory.PLAYERS, 1F, 1F)

        // 应用攻击冷却
        nekoStack.applyAttackCooldown(player)
        // 扣除耐久
        player.damageItemStackByMark(EquipmentSlot.HAND, 1)
    }

    override fun generateDamageMetadata(player: Player, nekoStack: NekoStack): DamageMetadata? {
        val user = player.toUser()
        if (user.attackSpeed.isActive(nekoStack.id)) {
            return null
        }

        val attributeMap = user.attributeMap
        // 锤子直接命中的生物的伤害元数据
        val directDamageMetadata = PlayerDamageMetadata(
            user = user,
            damageTags = DamageTags(DamageTag.DIRECT, DamageTag.MELEE, DamageTag.HAMMER),
            damageBundle = damageBundle(attributeMap) {
                every {
                    standard()
                }
            }
        )

        return directDamageMetadata
    }
}