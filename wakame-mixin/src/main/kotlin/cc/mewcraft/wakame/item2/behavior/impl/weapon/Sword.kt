package cc.mewcraft.wakame.item2.behavior.impl.weapon

import cc.mewcraft.wakame.damage.DamageMetadata
import cc.mewcraft.wakame.damage.PlayerDamageMetadata
import cc.mewcraft.wakame.damage.hurt
import cc.mewcraft.wakame.entity.player.attributeContainer
import cc.mewcraft.wakame.event.bukkit.PlayerItemLeftClickEvent
import cc.mewcraft.wakame.item2.config.property.ItemPropertyTypes
import cc.mewcraft.wakame.item2.extension.addCooldown
import cc.mewcraft.wakame.item2.extension.damageItem
import cc.mewcraft.wakame.item2.extension.isOnCooldown
import cc.mewcraft.wakame.item2.getProperty
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * 剑的物品行为.
 */
object Sword : Weapon {

    override fun generateDamageMetadata(player: Player, itemstack: ItemStack): DamageMetadata? {
        return null
    }

    override fun handleLeftClick(player: Player, itemstack: ItemStack, event: PlayerItemLeftClickEvent) {
        if (itemstack.isOnCooldown(player)) return
        val sword = itemstack.getProperty(ItemPropertyTypes.SWORD) ?: return
        // 造成伤害
        val attrContainer = player.attributeContainer
        val damageMetadata = PlayerDamageMetadata(attrContainer) {
            every {
                standard()
            }
        }
        val hitEntities = WeaponUtils.getHitEntities(player, 5.0, sword.attackHalfWidth, sword.attackHalfHeight, sword.attackHalfDepth)
        if (hitEntities.isNotEmpty()) {
            // 造成伤害
            hitEntities.forEach { entity -> entity.hurt(damageMetadata, player, true) }
            // 设置耐久
            player.damageItem(event.hand, 1)
        }
        // 设置冷却
        itemstack.addCooldown(player, sword.attackCooldown)
    }
}