package cc.mewcraft.wakame.item.schema.behavior

import cc.mewcraft.wakame.item.schema.NekoItem
import cc.mewcraft.wakame.player.equipment.ArmorEquipEvent
import cc.mewcraft.wakame.provider.ConfigProvider
import cc.mewcraft.wakame.world.block.event.BlockBreakActionEvent
import io.papermc.paper.event.player.PlayerStopUsingItemEvent
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemBreakEvent
import org.bukkit.event.player.PlayerItemDamageEvent
import org.bukkit.inventory.ItemStack

sealed interface ItemBehaviorHolder

interface ItemBehavior : ItemBehaviorHolder {

    /* Event handlers */

    fun handleInteract(player: Player, itemStack: ItemStack, action: Action, wrappedEvent: PlayerInteractEvent) = Unit
    fun handleEntityInteract(player: Player, itemStack: ItemStack, clicked: Entity, event: PlayerInteractAtEntityEvent) = Unit
    fun handleAttackEntity(player: Player, itemStack: ItemStack, attacked: Entity, event: EntityDamageByEntityEvent) = Unit
    fun handleBreakBlock(player: Player, itemStack: ItemStack, event: BlockBreakEvent) = Unit
    fun handleDamage(player: Player, itemStack: ItemStack, event: PlayerItemDamageEvent) = Unit
    fun handleBreak(player: Player, itemStack: ItemStack, event: PlayerItemBreakEvent) = Unit
    fun handleEquip(player: Player, itemStack: ItemStack, equipped: Boolean, event: ArmorEquipEvent) = Unit
    fun handleInventoryClick(player: Player, itemStack: ItemStack, event: InventoryClickEvent) = Unit
    fun handleInventoryClickOnCursor(player: Player, itemStack: ItemStack, event: InventoryClickEvent) = Unit
    fun handleInventoryHotbarSwap(player: Player, itemStack: ItemStack, event: InventoryClickEvent) = Unit
    fun handleBlockBreakAction(player: Player, itemStack: ItemStack, event: BlockBreakActionEvent) = Unit
    fun handleRelease(player: Player, itemStack: ItemStack, event: PlayerStopUsingItemEvent) = Unit

}

interface ItemBehaviorFactory<T : ItemBehavior> : ItemBehaviorHolder {
    fun create(item: NekoItem, behaviorConfig: ConfigProvider): T
}