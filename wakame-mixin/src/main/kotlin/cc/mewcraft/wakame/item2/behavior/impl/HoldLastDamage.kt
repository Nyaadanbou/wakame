@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.item2.behavior.impl

import cc.mewcraft.wakame.event.bukkit.WrappedPlayerInteractEvent
import cc.mewcraft.wakame.item2.behavior.ItemBehavior
import cc.mewcraft.wakame.player.equipment.ArmorChangeEvent
import cc.mewcraft.wakame.util.item.damage
import cc.mewcraft.wakame.util.item.isDamageable
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

data object HoldLastDamage : ItemBehavior {

    //override fun handleAttackEntity(player: Player, itemstack: ItemStack, damagee: Entity, event: NekoEntityDamageEvent) {
    //    tryCancelEvent(itemstack,player, event)
    //}

    override fun handleInteract(player: Player, itemstack: ItemStack, action: Action, wrappedEvent: WrappedPlayerInteractEvent) {
        tryCancelEvent(itemstack, player, wrappedEvent.event)
    }

    override fun handleInteractAtEntity(player: Player, itemstack: ItemStack, clicked: Entity, event: PlayerInteractAtEntityEvent) {
        tryCancelEvent(itemstack, player, event)
    }

    override fun handleBreakBlock(player: Player, itemstack: ItemStack, event: BlockBreakEvent) {
        tryCancelEvent(itemstack, player, event)
    }

    override fun handleDamage(player: Player, itemstack: ItemStack, event: PlayerItemDamageEvent) {
        if (!itemstack.isDamageable) return
        // 物品要损坏了
        // 取消掉耐久事件, 设为 0 耐久
        val damage = itemstack.damage
        val maxDamage = itemstack.maxDamage
        if (damage + event.damage >= maxDamage) {
            event.isCancelled = true
            itemstack.damage = maxDamage
        }
    }

    override fun handleEquip(player: Player, itemstack: ItemStack, equipped: Boolean, event: ArmorChangeEvent) {
        if (equipped) {
            tryCancelEvent(itemstack, player, event)
        }
    }

    override fun handleConsume(player: Player, itemstack: ItemStack, event: PlayerItemConsumeEvent) {
        tryCancelEvent(itemstack, player, event)
    }

    private fun tryCancelEvent(itemstack: ItemStack, player: Player, event: Cancellable) {
        val damage = itemstack.damage
        val maxDamage = itemstack.maxDamage
        if (damage >= maxDamage) {
            event.isCancelled = true
            player.sendMessage(text("无法使用完全损坏的物品"))
        }
    }

}