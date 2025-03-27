package cc.mewcraft.wakame.attack

import cc.mewcraft.wakame.damage.DamageMetadata
import cc.mewcraft.wakame.damage.PlayerDamageMetadata
import cc.mewcraft.wakame.damage.damageBundle
import cc.mewcraft.wakame.event.bukkit.NekoPreprocessDamageEvent
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.extension.applyAttackCooldown
import cc.mewcraft.wakame.item.extension.damageItemStack2
import cc.mewcraft.wakame.player.interact.WrappedPlayerInteractEvent
import cc.mewcraft.wakame.player.itemdamage.ItemDamageEventMarker
import cc.mewcraft.wakame.user.attackSpeed
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
    private val cancelVanillaDamage: Boolean,
) : AttackType {
    companion object {
        const val NAME = "trident"
    }

    override fun generateDamageMetadata(itemstack: NekoStack, event: NekoPreprocessDamageEvent): DamageMetadata? {
        val player = event.damager
        if (player.attackSpeed.isActive(itemstack.id)) return null
        val playerAttributes = event.damagerAttributes
        val playerDamageMetadata = PlayerDamageMetadata(
            attributes = playerAttributes,
            damageBundle = damageBundle(playerAttributes) {
                every {
                    standard()
                }
            }
        )

        return playerDamageMetadata
    }

    override fun handleAttackEntity(itemstack: NekoStack, event: NekoPreprocessDamageEvent) {
        val player = event.damager
        if (player.attackSpeed.isActive(itemstack.id)) return
        itemstack.applyAttackCooldown(player)
        player.damageItemStack2(EquipmentSlot.HAND, 1)
    }

    override fun handleInteract(player: Player, itemstack: NekoStack, action: Action, wrappedEvent: WrappedPlayerInteractEvent) {
        if (action.isLeftClick) {
            if (!player.attackSpeed.isActive(itemstack.id)) {
                // 没有左键到生物时, 也应该应用攻击冷却
                itemstack.applyAttackCooldown(player)
            }
        } else if (action.isRightClick) {
            if (player.attackSpeed.isActive(itemstack.id)) {
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

    override fun handleDamage(player: Player, itemstack: NekoStack, event: PlayerItemDamageEvent) {
        if (cancelVanillaDamage && ItemDamageEventMarker.isAlreadyDamaged(player)) {
            event.isCancelled = true
        }
    }
}