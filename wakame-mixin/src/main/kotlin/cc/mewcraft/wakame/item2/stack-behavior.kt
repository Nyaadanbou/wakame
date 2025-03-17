@file:JvmName("KoishStackBehavior")

package cc.mewcraft.wakame.item2

import cc.mewcraft.wakame.item2.behavior.ItemBehavior
import cc.mewcraft.wakame.player.equipment.ArmorChangeEvent
import cc.mewcraft.wakame.util.MojangStack
import cc.mewcraft.wakame.util.item.toNMS
import io.papermc.paper.event.player.PlayerStopUsingItemEvent
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerItemBreakEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.event.player.PlayerItemDamageEvent
import org.bukkit.inventory.ItemStack


// ------------
// 用于访问 `org.bukkit.inventory.ItemStack` 上的 ItemBehavior
// ------------

fun ItemStack.hasBehavior(behavior: ItemBehavior): Boolean = toNMS().hasBehavior(behavior)
fun ItemStack.forEachBehavior(action: (ItemBehavior) -> Unit) = toNMS().forEachBehavior(action)

// 注: 这些 handle... 函数都是为了方便遍历 ItemBehavior

// FIXME #350: 等完全迁移
//fun ItemStack.handleInteract(player: Player, itemstack: ItemStack, action: Action, wrappedEvent: WrappedPlayerInteractEvent) =
//    forEachBehavior { it.handleInteract(player, itemstack, action, wrappedEvent) }

fun ItemStack.handleInteractAtEntity(player: Player, itemstack: ItemStack, clicked: Entity, event: PlayerInteractAtEntityEvent) =
    forEachBehavior { it.handleInteractAtEntity(player, itemstack, clicked, event) }

// FIXME #350: 等完全迁移
//fun ItemStack.handleAttackEntity(player: Player, itemstack: ItemStack, damagee: Entity, event: NekoEntityDamageEvent) =
//    forEachBehavior { it.handleAttackEntity(player, itemstack, damagee, event) }

fun ItemStack.handleItemProjectileLaunch(player: Player, itemstack: ItemStack, projectile: Projectile, event: ProjectileLaunchEvent) =
    forEachBehavior { it.handleItemProjectileLaunch(player, itemstack, projectile, event) }

fun ItemStack.handleItemProjectileHit(player: Player, itemstack: ItemStack, projectile: Projectile, event: ProjectileHitEvent) =
    forEachBehavior { it.handleItemProjectileHit(player, itemstack, projectile, event) }

fun ItemStack.handleBreakBlock(player: Player, itemstack: ItemStack, event: BlockBreakEvent) =
    forEachBehavior { it.handleBreakBlock(player, itemstack, event) }

fun ItemStack.handleDamage(player: Player, itemstack: ItemStack, event: PlayerItemDamageEvent) =
    forEachBehavior { it.handleDamage(player, itemstack, event) }

fun ItemStack.handleBreak(player: Player, itemstack: ItemStack, event: PlayerItemBreakEvent) =
    forEachBehavior { it.handleBreak(player, itemstack, event) }

fun ItemStack.handleEquip(player: Player, itemstack: ItemStack, equipped: Boolean, event: ArmorChangeEvent) =
    forEachBehavior { it.handleEquip(player, itemstack, equipped, event) }

fun ItemStack.handleInventoryClick(player: Player, itemstack: ItemStack, event: InventoryClickEvent) =
    forEachBehavior { it.handleInventoryClick(player, itemstack, event) }

fun ItemStack.handleInventoryClickOnCursor(player: Player, itemstack: ItemStack, event: InventoryClickEvent) =
    forEachBehavior { it.handleInventoryClickOnCursor(player, itemstack, event) }

fun ItemStack.handleInventoryHotbarSwap(player: Player, itemstack: ItemStack, event: InventoryClickEvent) =
    forEachBehavior { it.handleInventoryHotbarSwap(player, itemstack, event) }

fun ItemStack.handleRelease(player: Player, itemstack: ItemStack, event: PlayerStopUsingItemEvent) =
    forEachBehavior { it.handleRelease(player, itemstack, event) }

fun ItemStack.handleConsume(player: Player, itemstack: ItemStack, event: PlayerItemConsumeEvent) =
    forEachBehavior { it.handleConsume(player, itemstack, event) }

// FIXME #350: 等完全迁移
//fun ItemStack.handleAbilityPrepareCast(caster: Player, itemstack: ItemStack, ability: Ability, event: PlayerAbilityPrepareCastEvent) =
//    forEachBehavior { it.handleAbilityPrepareCast(caster, itemstack, ability, event) }

// ------------
// 用于访问 `net.minecraft.world.item.ItemStack` 上的 ItemBehavior
// ------------

fun MojangStack.hasBehavior(behavior: ItemBehavior): Boolean =
    koish?.behaviors?.has(behavior) == true

fun MojangStack.forEachBehavior(action: (ItemBehavior) -> Unit) {
    koish?.behaviors?.forEach(action)
}