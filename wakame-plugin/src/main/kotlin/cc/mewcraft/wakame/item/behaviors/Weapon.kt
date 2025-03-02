package cc.mewcraft.wakame.item.behaviors

import cc.mewcraft.wakame.event.bukkit.NekoEntityDamageEvent
import cc.mewcraft.wakame.event.bukkit.WrappedPlayerInteractEvent
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.behavior.ItemBehavior
import cc.mewcraft.wakame.item.behavior.ItemBehaviorType
import cc.mewcraft.wakame.item.template.ItemTemplateTypes
import cc.mewcraft.wakame.player.interact.PlayerClickEvent
import com.destroystokyo.paper.event.server.ServerTickStartEvent
import io.papermc.paper.event.player.PlayerStopUsingItemEvent
import org.bukkit.damage.DamageSource
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
        override fun handleActiveTick(player: Player, itemStack: ItemStack, koishStack: NekoStack, event: ServerTickStartEvent) {
            val weapon = koishStack.templates.get(ItemTemplateTypes.WEAPON) ?: return
            weapon.weaponType.handleActiveTick(player, koishStack, event)
        }

        override fun handleClick(player: Player, itemStack: ItemStack, koishStack: NekoStack, clickAction: PlayerClickEvent.Action, clickHand: PlayerClickEvent.Hand, event: PlayerClickEvent) {
            val weapon = koishStack.templates.get(ItemTemplateTypes.WEAPON) ?: return
            weapon.weaponType.handleClick(player, koishStack, clickAction, clickHand, event)
        }

        override fun handleAttackEntity(player: Player, itemStack: ItemStack, koishStack: NekoStack, damagee: Entity, event: NekoEntityDamageEvent) {
            if (event.isCancelled) return
            val weapon = koishStack.templates.get(ItemTemplateTypes.WEAPON) ?: return
            if (damagee !is LivingEntity) return
            weapon.weaponType.handleAttackEntity(player, koishStack, damagee, event)
        }

        override fun handlePlayerDamage(player: Player, itemStack: ItemStack, koishStack: NekoStack, damageSource: DamageSource, event: NekoEntityDamageEvent) {
            if (event.isCancelled) return
            val weapon = koishStack.templates.get(ItemTemplateTypes.WEAPON) ?: return
            weapon.weaponType.handlePlayerDamage(player, koishStack, damageSource, event)
        }

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
