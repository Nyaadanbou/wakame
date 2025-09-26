package cc.mewcraft.wakame.item.behavior.impl.weapon

import cc.mewcraft.wakame.damage.KoishDamageSources
import cc.mewcraft.wakame.damage.PlayerDamageMetadata
import cc.mewcraft.wakame.damage.hurt
import cc.mewcraft.wakame.entity.player.attributeContainer
import cc.mewcraft.wakame.item.behavior.AttackContext
import cc.mewcraft.wakame.item.behavior.InteractionResult
import cc.mewcraft.wakame.item.extension.addCooldown
import cc.mewcraft.wakame.item.extension.damageItem
import cc.mewcraft.wakame.item.extension.isOnCooldown
import cc.mewcraft.wakame.item.getProp
import cc.mewcraft.wakame.item.property.ItemPropertyTypes
import org.bukkit.FluidCollisionMode
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.EquipmentSlot

/**
 * 一般近战武器的物品行为.
 * 特指左键点击生物以进行攻击的"近战武器", 如斧等.
 * 此类物品只有攻击到生物才会进入冷却.
 */
object Melee : Weapon {
    override fun handleSimpleAttack(context: AttackContext): InteractionResult {
        val itemstack = context.itemstack
        val player = context.player
        if (itemstack.isOnCooldown(player)) return InteractionResult.FAIL
        val melee = itemstack.getProp(ItemPropertyTypes.MELEE) ?: return InteractionResult.FAIL

        val world = player.world
        val attrContainer = player.attributeContainer
        val maxDistance = melee.attackRange

        val rayTraceResult = world.rayTrace(
            player.eyeLocation,
            player.eyeLocation.direction,
            maxDistance,
            FluidCollisionMode.NEVER,
            true, // 是否忽略草、告示牌、流体等有碰撞判定但是可穿过的方块
            0.0
        ) {
            it is LivingEntity && it != player
        }
        // 未命中实体
        if (rayTraceResult == null || rayTraceResult.hitBlock != null) {
            return InteractionResult.FAIL
        }
        val hitEntity = rayTraceResult.hitEntity
        if (hitEntity != null && hitEntity is LivingEntity) {
            val damageMetadata = PlayerDamageMetadata(attrContainer) {
                every {
                    standard()
                }
            }
            val damageSource = KoishDamageSources.playerAttack(player)
            // 造成伤害
            val flag = hitEntity.hurt(damageMetadata, damageSource, true)
            // 如果成功造成了伤害
            if (flag) {
                // 设置耐久
                player.damageItem(EquipmentSlot.HAND, melee.itemDamagePerAttack)
            }
            // 设置冷却
            // 命中实体才进入冷却
            itemstack.addCooldown(player, melee.attackCooldown)
        }
        return InteractionResult.SUCCESS
    }
}