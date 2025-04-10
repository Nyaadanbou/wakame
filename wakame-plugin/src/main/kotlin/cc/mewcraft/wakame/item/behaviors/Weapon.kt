package cc.mewcraft.wakame.item.behaviors

import cc.mewcraft.wakame.event.bukkit.NekoPostprocessDamageEvent
import cc.mewcraft.wakame.event.bukkit.PlayerItemLeftClickEvent
import cc.mewcraft.wakame.event.bukkit.PlayerItemRightClickEvent
import cc.mewcraft.wakame.event.bukkit.WrappedPlayerInteractEvent
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.behavior.ItemBehavior
import cc.mewcraft.wakame.item.behavior.ItemBehaviorType
import cc.mewcraft.wakame.item.template.ItemTemplateTypes
import io.papermc.paper.event.player.PlayerStopUsingItemEvent
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerItemDamageEvent
import org.bukkit.inventory.ItemStack

/**
 * 作为武器的物品的逻辑.
 */
interface Weapon : ItemBehavior {
    private object Default : Weapon {
        override fun handleLeftClick(player: Player, itemStack: ItemStack, koishStack: NekoStack, event: PlayerItemLeftClickEvent) {
            val weapon = koishStack.templates.get(ItemTemplateTypes.WEAPON) ?: return
            weapon.weaponType.handleLeftClick(player, koishStack, event)
        }

        override fun handleRightClick(player: Player, itemStack: ItemStack, koishStack: NekoStack, clickHand: PlayerItemRightClickEvent.Hand, event: PlayerItemRightClickEvent) {
            val weapon = koishStack.templates.get(ItemTemplateTypes.WEAPON) ?: return
            weapon.weaponType.handleRightClick(player, koishStack, clickHand, event)
        }

        override fun handleAttackEntity(player: Player, itemStack: ItemStack, koishStack: NekoStack, damagee: Entity, event: NekoPostprocessDamageEvent) {
            if (event.isCancelled) return
            val weapon = koishStack.templates.get(ItemTemplateTypes.WEAPON) ?: return
            if (damagee !is LivingEntity) return
            weapon.weaponType.handleAttackEntity(player, koishStack, damagee, event)
        }

        //override fun handlePlayerDamage(player: Player, itemStack: ItemStack, koishStack: NekoStack, damageSource: DamageSource, event: NekoPostprocessDamageEvent) {
        //    if (event.isCancelled) return
        //    val weapon = koishStack.templates.get(ItemTemplateTypes.WEAPON) ?: return
        //    weapon.weaponType.handlePlayerDamage(player, koishStack, damageSource, event)
        //}

        override fun handleInteract(player: Player, itemStack: ItemStack, koishStack: NekoStack, action: Action, wrappedEvent: WrappedPlayerInteractEvent) {
            if (wrappedEvent.event.useItemInHand() == Event.Result.DENY) return

            val weapon = koishStack.templates.get(ItemTemplateTypes.WEAPON) ?: return
            weapon.weaponType.handleInteract(player, koishStack, action, wrappedEvent)
        }

        override fun handleRelease(player: Player, itemStack: ItemStack, koishStack: NekoStack, event: PlayerStopUsingItemEvent) {
            val weapon = koishStack.templates.get(ItemTemplateTypes.WEAPON) ?: return
            weapon.weaponType.handleRelease(player, koishStack, event)
        }

        override fun handleDamage(player: Player, itemStack: ItemStack, koishStack: NekoStack, event: PlayerItemDamageEvent) {
            if (event.isCancelled) return
            val weapon = koishStack.templates.get(ItemTemplateTypes.WEAPON) ?: return
            weapon.weaponType.handleDamage(player, koishStack, event)
        }
    }

    companion object Type : ItemBehaviorType<Weapon> {
        override fun create(): Weapon {
            return Default
        }
    }
}
