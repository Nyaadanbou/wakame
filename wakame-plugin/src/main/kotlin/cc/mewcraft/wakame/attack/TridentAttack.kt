package cc.mewcraft.wakame.attack

import cc.mewcraft.wakame.damage.*
import cc.mewcraft.wakame.event.bukkit.NekoEntityDamageEvent
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.applyAttackCooldown
import cc.mewcraft.wakame.item.damageItemStack2
import cc.mewcraft.wakame.player.interact.WrappedPlayerInteractEvent
import cc.mewcraft.wakame.player.itemdamage.ItemDamageEventMarker
import cc.mewcraft.wakame.user.toUser
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerItemDamageEvent
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
class TridentAttack(
    private val cancelVanillaDamage: Boolean
) : AttackType {
    companion object {
        const val NAME = "trident"
    }

    override fun handleDamage(player: Player, nekoStack: NekoStack, event: PlayerItemDamageEvent) {
        if (cancelVanillaDamage && ItemDamageEventMarker.isAlreadyDamaged(player)) {
            event.isCancelled = true
        }
    }

    override fun generateDamageMetadata(player: Player, nekoStack: NekoStack): DamageMetadata? {
        val user = player.toUser()
        if (user.attackSpeed.isActive(nekoStack.id)) {
            return null
        }

        return PlayerDamageMetadata(
            user = user,
            damageTags = DamageTags(DamageTag.DIRECT, DamageTag.MELEE, DamageTag.TRIDENT),
            damageBundle = damageBundle(user.attributeMap) {
                every {
                    standard()
                }
            }
        )
    }

    override fun handleAttackEntity(player: Player, nekoStack: NekoStack, damagee: LivingEntity, event: NekoEntityDamageEvent) {
        if (!event.damageMetadata.damageTags.contains(DamageTag.DIRECT)) {
            return
        }

        val user = player.toUser()
        if (user.attackSpeed.isActive(nekoStack.id)) {
            return
        }

        // 应用攻击冷却
        nekoStack.applyAttackCooldown(player)
        // 扣除耐久
        player.damageItemStack2(EquipmentSlot.HAND, 1)
    }

    override fun handleInteract(player: Player, nekoStack: NekoStack, action: Action, wrappedEvent: WrappedPlayerInteractEvent) {
        val user = player.toUser()
        if (action.isLeftClick) {
            if (!user.attackSpeed.isActive(nekoStack.id)) {
                // 没有左键到生物时, 也应该应用攻击冷却
                nekoStack.applyAttackCooldown(player)
            }
        } else if (action.isRightClick) {
            if (user.attackSpeed.isActive(nekoStack.id)) {
                wrappedEvent.event.setUseItemInHand(Event.Result.DENY)
            } else {
                // 禁止副手使用三叉戟
                val event = wrappedEvent.event
                if (event.hand == EquipmentSlot.OFF_HAND) {
                    val playerInventory = player.inventory
                    event.setUseItemInHand(Event.Result.DENY)
                    val itemInOffHand = playerInventory.itemInOffHand
                    val itemInMainHand = playerInventory.itemInMainHand
                    playerInventory.setItem(EquipmentSlot.HAND, itemInOffHand)
                    playerInventory.setItem(EquipmentSlot.OFF_HAND, itemInMainHand)
                }
            }
        }
        wrappedEvent.actionPerformed = true
    }
}