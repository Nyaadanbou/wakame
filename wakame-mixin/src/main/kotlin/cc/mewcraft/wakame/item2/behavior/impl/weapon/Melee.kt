package cc.mewcraft.wakame.item2.behavior.impl.weapon

import cc.mewcraft.wakame.damage.PlayerDamageMetadata
import cc.mewcraft.wakame.damage.hurt
import cc.mewcraft.wakame.entity.player.attributeContainer
import cc.mewcraft.wakame.event.bukkit.PlayerItemLeftClickEvent
import cc.mewcraft.wakame.item2.config.property.ItemPropertyTypes
import cc.mewcraft.wakame.item2.extension.addCooldown
import cc.mewcraft.wakame.item2.extension.damageItem
import cc.mewcraft.wakame.item2.extension.isOnCooldown
import cc.mewcraft.wakame.item2.getProperty
import org.bukkit.FluidCollisionMode
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * 一般近战武器的物品行为.
 * 特指左键点击生物以进行攻击的"近战武器", 如斧等.
 * 此类物品只有攻击到生物才会进入冷却.
 */
object Melee : Weapon {
    override fun handleLeftClick(player: Player, itemstack: ItemStack, event: PlayerItemLeftClickEvent) {
        if (itemstack.isOnCooldown(player)) return
        val melee = itemstack.getProperty(ItemPropertyTypes.MELEE) ?: return

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
            return
        }
        val hitEntity = rayTraceResult.hitEntity
        if (hitEntity != null && hitEntity is LivingEntity) {
            val damageMetadata = PlayerDamageMetadata(attrContainer) {
                every {
                    standard()
                }
            }
            // 造成伤害
            hitEntity.hurt(damageMetadata, player, true)

            // 设置耐久
            player.damageItem(event.hand, melee.itemDamagePerAttack)
            // 设置冷却
            // 命中实体才进入冷却
            itemstack.addCooldown(player, melee.attackCooldown)
        }
    }
}