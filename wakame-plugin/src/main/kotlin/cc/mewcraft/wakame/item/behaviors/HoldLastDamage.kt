@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.item.behaviors

import cc.mewcraft.wakame.event.bukkit.PostprocessDamageEvent
import cc.mewcraft.wakame.event.bukkit.WrappedPlayerInteractEvent
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.behavior.ItemBehavior
import cc.mewcraft.wakame.item.behavior.ItemBehaviorType
import cc.mewcraft.wakame.item.extension.damage
import cc.mewcraft.wakame.item.extension.isDamageable
import cc.mewcraft.wakame.player.equipment.ArmorChangeEvent
import cc.mewcraft.wakame.util.item.damage
import cc.mewcraft.wakame.util.item.maxDamage
import net.kyori.adventure.text.Component.text
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.event.player.PlayerItemDamageEvent
import org.bukkit.inventory.ItemStack

interface HoldLastDamage : ItemBehavior {
    private object Default : HoldLastDamage {
        override fun handleAttackEntity(player: Player, itemStack: ItemStack, koishStack: NekoStack, damagee: Entity, event: PostprocessDamageEvent) {
            tryCancelEvent(itemStack, koishStack, player, event)
        }

        override fun handleInteract(player: Player, itemStack: ItemStack, koishStack: NekoStack, action: Action, wrappedEvent: WrappedPlayerInteractEvent) {
            tryCancelEvent(itemStack, koishStack, player, wrappedEvent.event)
        }

        override fun handleInteractAtEntity(player: Player, itemStack: ItemStack, koishStack: NekoStack, clicked: Entity, event: PlayerInteractAtEntityEvent) {
            tryCancelEvent(itemStack, koishStack, player, event)
        }

        override fun handleBreakBlock(player: Player, itemStack: ItemStack, koishStack: NekoStack, event: BlockBreakEvent) {
            tryCancelEvent(itemStack, koishStack, player, event)
        }

        override fun handleDamage(player: Player, itemStack: ItemStack, koishStack: NekoStack, event: PlayerItemDamageEvent) {
            if (!koishStack.isDamageable) return
            // 物品要损坏了
            // 取消掉耐久事件, 设为 0 耐久
            val damage = itemStack.damage
            val maxDamage = itemStack.maxDamage
            if (damage + event.damage >= maxDamage) {
                event.isCancelled = true
                koishStack.damage = maxDamage
            }
        }

        override fun handleEquip(player: Player, itemStack: ItemStack, koishStack: NekoStack, equipped: Boolean, event: ArmorChangeEvent) {
            if (equipped) {
                tryCancelEvent(itemStack, koishStack, player, event)
            }
        }

        override fun handleConsume(player: Player, itemStack: ItemStack, koishStack: NekoStack, event: PlayerItemConsumeEvent) {
            tryCancelEvent(itemStack, koishStack, player, event)
        }

        private fun tryCancelEvent(itemStack: ItemStack, koishStack: NekoStack, player: Player, event: Cancellable) {
            val damage = itemStack.damage
            val maxDamage = itemStack.maxDamage
            if (damage >= maxDamage) {
                event.isCancelled = true
                player.sendMessage(text("无法使用完全损坏的物品"))
            }
        }
    }

    companion object Type : ItemBehaviorType<HoldLastDamage> {
        override fun create(): HoldLastDamage = Default
    }
}