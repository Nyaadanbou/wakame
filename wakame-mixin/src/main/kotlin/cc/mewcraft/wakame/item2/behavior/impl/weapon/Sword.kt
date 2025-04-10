package cc.mewcraft.wakame.item2.behavior.impl.weapon

import cc.mewcraft.wakame.damage.DamageMetadata
import cc.mewcraft.wakame.damage.PlayerDamageMetadata
import cc.mewcraft.wakame.damage.hurt
import cc.mewcraft.wakame.entity.player.attributeContainer
import cc.mewcraft.wakame.event.bukkit.PlayerItemLeftClickEvent
import cc.mewcraft.wakame.event.bukkit.WrappedPlayerInteractEvent
import cc.mewcraft.wakame.item2.ItemDamageEventMarker
import cc.mewcraft.wakame.item2.config.property.ItemPropertyTypes
import cc.mewcraft.wakame.item2.extension.addCooldown
import cc.mewcraft.wakame.item2.extension.isOnCooldown
import cc.mewcraft.wakame.item2.getProperty
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerItemDamageEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

object Sword : Weapon {

    override fun generateDamageMetadata(player: Player, itemstack: ItemStack): DamageMetadata? {
        return null
    }

    override fun handleLeftClick(player: Player, itemstack: ItemStack, event: PlayerItemLeftClickEvent) {
        if (itemstack.isOnCooldown(player)) return
        val swordConfig = itemstack.getProperty(ItemPropertyTypes.SWORD) ?: return
        val attackSpeed = itemstack.getProperty(ItemPropertyTypes.ATTACK_SPEED)?.unwrap() ?: return

        // TODO #349: 触发 pre damage event
        val attrContainer = player.attributeContainer
        val damageMetadata = PlayerDamageMetadata(attrContainer) {
            every {
                standard()
            }
        }
        val hitEntities = WeaponUtils.getHitEntities(player, 4.0, 1.2f, 0.1f, 1f)
        hitEntities.forEach { entity -> entity.hurt(damageMetadata, player, true) }

        itemstack.addCooldown(player, attackSpeed)
        // TODO #349: 使物品掉耐久
    }

    override fun handleInteract(player: Player, itemstack: ItemStack, action: Action, wrappedEvent: WrappedPlayerInteractEvent) {
        val event = wrappedEvent.event
        if (event.hand != EquipmentSlot.HAND) {
            event.setUseItemInHand(Event.Result.DENY)
        }
        wrappedEvent.actionPerformed = true
    }

    override fun handleDamage(player: Player, itemstack: ItemStack, event: PlayerItemDamageEvent) {
        if (event.isCancelled) return
        val swordConfig = itemstack.getProperty(ItemPropertyTypes.SWORD) ?: return
        if (swordConfig.cancelVanillaDamage && ItemDamageEventMarker.isAlreadyDamaged(player)) {
            event.isCancelled = true
        }
    }

}