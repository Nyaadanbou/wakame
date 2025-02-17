package cc.mewcraft.wakame.item.behaviors

import cc.mewcraft.wakame.event.bukkit.NekoEntityDamageEvent
import cc.mewcraft.wakame.item.behavior.ItemBehavior
import cc.mewcraft.wakame.item.behavior.ItemBehaviorType
import cc.mewcraft.wakame.item.projectNeko
import cc.mewcraft.wakame.item.template.ItemTemplateTypes
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
interface Attack : ItemBehavior {
    private object Default : Attack {
        override fun handleInteract(player: Player, itemStack: ItemStack, action: Action, wrappedEvent: WrappedPlayerInteractEvent) {
            if (wrappedEvent.event.useItemInHand() == Event.Result.DENY) {
                return
            }
            val nekoStack = itemStack.projectNeko()
            val attack = nekoStack.templates.get(ItemTemplateTypes.ATTACK) ?: return
            attack.attackType.handleInteract(player, nekoStack, action, wrappedEvent)
        }

        override fun handleAttackEntity(player: Player, itemStack: ItemStack, damagee: Entity, event: NekoEntityDamageEvent) {
            if (event.isCancelled) {
                return
            }
            val nekoStack = itemStack.projectNeko()
            val attack = nekoStack.templates.get(ItemTemplateTypes.ATTACK) ?: return
            if (damagee !is LivingEntity) {
                return
            }
            attack.attackType.handleAttackEntity(player, nekoStack, damagee, event)
        }

        override fun handleDamage(player: Player, itemStack: ItemStack, event: PlayerItemDamageEvent) {
            if (event.isCancelled) {
                return
            }
            val nekoStack = itemStack.projectNeko()
            val attack = nekoStack.templates.get(ItemTemplateTypes.ATTACK) ?: return
            attack.attackType.handleDamage(player, nekoStack, event)
        }
    }

    companion object Type : ItemBehaviorType<Attack> {
        override fun create(): Attack {
            return Default
        }
    }
}