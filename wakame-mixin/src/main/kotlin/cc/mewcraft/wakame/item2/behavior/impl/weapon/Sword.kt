package cc.mewcraft.wakame.item2.behavior.impl.weapon

import cc.mewcraft.wakame.damage.DamageMetadata
import cc.mewcraft.wakame.damage.PlayerDamageMetadata
import cc.mewcraft.wakame.damage.hurt
import cc.mewcraft.wakame.entity.player.attributeContainer
import cc.mewcraft.wakame.event.bukkit.PlayerItemLeftClickEvent
import cc.mewcraft.wakame.event.bukkit.WrappedPlayerInteractEvent
import cc.mewcraft.wakame.item2.config.property.ItemPropertyTypes
import cc.mewcraft.wakame.item2.extension.addCooldown
import cc.mewcraft.wakame.item2.extension.damageItem
import cc.mewcraft.wakame.item2.extension.isOnCooldown
import cc.mewcraft.wakame.item2.getProperty
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.block.Action
import org.bukkit.inventory.EquipmentSlot
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
        // 造成伤害
        val attrContainer = player.attributeContainer
        val damageMetadata = PlayerDamageMetadata(attrContainer) {
            every {
                standard()
            }
        }
        val hitEntities = WeaponUtils.getHitEntities(player, 4.0, 1.2f, 0.1f, 1f) // 参考了太刀的实现
        if (hitEntities.isNotEmpty()) {
            // 造成伤害
            hitEntities.forEach { entity -> entity.hurt(damageMetadata, player, true) }
            // 设置耐久
            player.damageItem(event.hand, 1)
        }
        // 设置冷却
        val attackSpeed = itemstack.getProperty(ItemPropertyTypes.ATTACK_SPEED)
        itemstack.addCooldown(player, attackSpeed)
    }

    override fun handleInteract(player: Player, itemstack: ItemStack, action: Action, wrappedEvent: WrappedPlayerInteractEvent) {
        val event = wrappedEvent.event
        if (event.hand != EquipmentSlot.HAND) {
            event.setUseItemInHand(Event.Result.DENY) // 只允许主手使用剑进行交互
        }
        wrappedEvent.actionPerformed = true
    }
}