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
import cc.mewcraft.wakame.player.itemdamage.ItemDamageEventMarker
import cc.mewcraft.wakame.user.attackSpeed
import cc.mewcraft.wakame.util.adventure.BukkitSound
import cc.mewcraft.wakame.util.adventure.SoundSource
import cc.mewcraft.wakame.util.adventure.playSound
import com.destroystokyo.paper.ParticleBuilder
import org.bukkit.Location
import org.bukkit.Particle
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
    private val cancelVanillaDamage: Boolean,
) : AttackType {
    companion object {
        const val NAME = "hammer"
        const val MAX_RADIUS = 32.0
    }

    override fun generateDamageMetadata(itemstack: NekoStack, event: NekoPreprocessDamageEvent): DamageMetadata? {
        val player = event.damager
        if (player.attackSpeed.isActive(itemstack.id)) return null

        // 锤子直接命中的生物的伤害元数据
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
        if (player.attackSpeed.isActive(itemstack.id)) {
            // 只要这个物品的内部冷却处于激活状态就不处理
            // 内部冷却不一定是攻速组件造成的
            return
        }

        val damagee = event.damagee!!
        val hitLocation = damagee.location
        val playerAttributes = event.damagerAttributes

        // 范围伤害目标的判定为:
        // 以直接受击实体的脚部坐标为起点,
        // 向 y 轴正方向(上方) 1 格
        // 向 x 轴, z 轴正负方向各 radius 格的长方体所碰撞到的生物
        val attackRadius = playerAttributes.getValue(Attributes.HAMMER_DAMAGE_RANGE).coerceIn(0.0, MAX_RADIUS)
        val attackRatio = playerAttributes.getValue(Attributes.HAMMER_DAMAGE_RATIO)
        hitLocation.clone().add(.0, .5, .0).getNearbyLivingEntities(attackRadius, .5) {
            it != player && it != damagee
        }.forEach { xdamagee ->
            // 锤子范围命中的生物的伤害元数据
            val extraDamageMetadata = PlayerDamageMetadata(
                attributes = playerAttributes,
                damageBundle = damageBundle(playerAttributes) {
                    every {
                        standard()
                        rate {
                            attackRatio * standard()
                        }
                    }
                }
            )
            xdamagee.hurt(extraDamageMetadata, player, true)
        }

        // 特效
        val world = player.world
        val particleBuilder = ParticleBuilder(Particle.BLOCK)
            .count(3)
            .offset(0.3, 0.1, 0.3)
            .extra(0.15)
        // 计算正方形的左下角和右上角的坐标
        val bottomLeftX = hitLocation.x - attackRadius
        val bottomLeftZ = hitLocation.z - attackRadius
        val topRightX = hitLocation.x + attackRadius
        val topRightZ = hitLocation.z + attackRadius
        var x = bottomLeftX
        while (x <= topRightX) {
            var z = bottomLeftZ
            while (z <= topRightZ) {
                val currentLocation = Location(world, x, hitLocation.y, z)
                val blockData = Location(world, x, hitLocation.y - 1, z).block.blockData
                particleBuilder
                    .location(currentLocation)
                    .data(blockData)
                    .receivers(16)
                    .spawn()
                z += 0.5
            }
            x += 0.5
        }

        world.playSound(player) {
            type(BukkitSound.ITEM_MACE_SMASH_GROUND)
            source(SoundSource.PLAYER)
            volume(1f)
            pitch(1f)
        }

        itemstack.applyAttackCooldown(player)
        player.damageItemStack2(EquipmentSlot.HAND, 1)
    }

    override fun handleDamage(player: Player, itemstack: NekoStack, event: PlayerItemDamageEvent) {
        if (cancelVanillaDamage && ItemDamageEventMarker.isAlreadyDamaged(player)) {
            event.isCancelled = true
        }
    }
}