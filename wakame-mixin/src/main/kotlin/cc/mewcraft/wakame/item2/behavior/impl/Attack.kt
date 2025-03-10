package cc.mewcraft.wakame.item2.behavior.impl

import cc.mewcraft.wakame.event.bukkit.NekoEntityDamageEvent
import cc.mewcraft.wakame.item2.behavior.ItemBehavior
import cc.mewcraft.wakame.item2.template.ItemTemplateTypes
import cc.mewcraft.wakame.player.interact.WrappedPlayerInteractEvent
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerItemDamageEvent
import org.bukkit.inventory.ItemStack

/**
 * 物品发动攻击的逻辑.
 * 用于实现各种攻击效果.
 */
object Attack : ItemBehavior {

    override fun handleInteract(player: Player, itemStack: ItemStack, action: Action, wrappedEvent: WrappedPlayerInteractEvent) {
        if (wrappedEvent.event.useItemInHand() == Event.Result.DENY) return
        val attack = koishStack.templates.get(ItemTemplateTypes.ATTACK) ?: return
        attack.attackType.handleInteract(player, koishStack, action, wrappedEvent)
    }

    override fun handleAttackEntity(player: Player, itemStack: ItemStack, damagee: Entity, event: NekoEntityDamageEvent) {
        if (event.isCancelled) return
        val attack = koishStack.templates.get(ItemTemplateTypes.ATTACK) ?: return
        if (damagee !is LivingEntity) return
        attack.attackType.handleAttackEntity(player, koishStack, damagee, event)
    }

    override fun handleDamage(player: Player, itemStack: ItemStack, event: PlayerItemDamageEvent) {
        if (event.isCancelled) return
        val attack = koishStack.templates.get(ItemTemplateTypes.ATTACK) ?: return
        attack.attackType.handleDamage(player, koishStack, event)
    }

}