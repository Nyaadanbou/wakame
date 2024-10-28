package cc.mewcraft.wakame.attack

import cc.mewcraft.wakame.damage.*
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.applyAttackCooldown
import cc.mewcraft.wakame.player.interact.WrappedPlayerInteractEvent
import cc.mewcraft.wakame.user.toUser
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.inventory.EquipmentSlot

/**
 * 原版三叉戟攻击.
 *
 * ## Node structure
 * ```yaml
 * <node>:
 *   type: trident
 * ```
 */
class TridentAttack : AttackType {
    companion object {
        const val NAME = "trident"
    }

    override fun handleDirectMeleeAttackEntity(player: Player, nekoStack: NekoStack, event: EntityDamageEvent): DamageMetadata? {
        val user = player.toUser()
        if (user.attackSpeed.isActive(nekoStack.id)) {
            return null
        } else {
            nekoStack.applyAttackCooldown(player)
        }

        return PlayerDamageMetadata(
            user = user,
            damageTags = DamageTags(DamageTag.MELEE, DamageTag.TRIDENT),
            damageBundle = damageBundle(player.toUser().attributeMap) { every { standard() } }
        )
    }

    // 禁止副手使用三叉戟
    override fun handleInteract(player: Player, nekoStack: NekoStack, action: Action, wrappedEvent: WrappedPlayerInteractEvent) {
        val event = wrappedEvent.event
        if (event.hand != EquipmentSlot.OFF_HAND) return

        val playerInventory = player.inventory
        event.setUseItemInHand(Event.Result.DENY)
        val itemInOffHand = playerInventory.itemInOffHand
        val itemInMainHand = playerInventory.itemInMainHand
        playerInventory.setItem(EquipmentSlot.HAND, itemInOffHand)
        playerInventory.setItem(EquipmentSlot.OFF_HAND, itemInMainHand)
    }
}