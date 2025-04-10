package cc.mewcraft.wakame.attack

import cc.mewcraft.wakame.damage.DamageMetadata
import cc.mewcraft.wakame.damage.DamageTag
import cc.mewcraft.wakame.damage.PlayerDamageMetadata
import cc.mewcraft.wakame.damage.damageBundle
import cc.mewcraft.wakame.entity.player.attributeContainer
import cc.mewcraft.wakame.entity.player.itemCooldownContainer
import cc.mewcraft.wakame.event.bukkit.PostprocessDamageEvent
import cc.mewcraft.wakame.event.bukkit.WrappedPlayerInteractEvent
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.extension.applyAttackCooldown
import cc.mewcraft.wakame.item.extension.damageItemStack2
import cc.mewcraft.wakame.item2.ItemDamageEventMarker
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
    private val cancelVanillaDamage: Boolean,
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
        if (player.itemCooldownContainer.isActive(nekoStack.id)) {
            return null
        }

        val attributes = player.attributeContainer
        val damageMeta = PlayerDamageMetadata(
            attributes = attributes,
            damageBundle = damageBundle(attributes) {
                every {
                    standard()
                }
            }
        )

        return damageMeta
    }

    override fun handleAttackEntity(player: Player, nekoStack: NekoStack, damagee: LivingEntity, event: PostprocessDamageEvent) {
        if (!event.damageMetadata.damageTags.contains(DamageTag.DIRECT)) {
            return
        }

        if (player.itemCooldownContainer.isActive(nekoStack.id)) {
            return
        }

        // 应用攻击冷却
        nekoStack.applyAttackCooldown(player)
        // 扣除耐久
        player.damageItemStack2(EquipmentSlot.HAND, 1)
    }

    override fun handleInteract(player: Player, nekoStack: NekoStack, action: Action, wrappedEvent: WrappedPlayerInteractEvent) {
        if (action.isLeftClick) {
            if (!player.itemCooldownContainer.isActive(nekoStack.id)) {
                // 没有左键到生物时, 也应该应用攻击冷却
                nekoStack.applyAttackCooldown(player)
            }
        } else if (action.isRightClick) {
            if (player.itemCooldownContainer.isActive(nekoStack.id)) {
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