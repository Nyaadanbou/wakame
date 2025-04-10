@file:JvmName("KoishStackBehavior")

package cc.mewcraft.wakame.item2

import cc.mewcraft.wakame.event.bukkit.PlayerItemLeftClickEvent
import cc.mewcraft.wakame.event.bukkit.PlayerItemRightClickEvent
import cc.mewcraft.wakame.event.bukkit.PostprocessDamageEvent
import cc.mewcraft.wakame.event.bukkit.WrappedPlayerInteractEvent
import cc.mewcraft.wakame.item2.behavior.ItemBehavior
import cc.mewcraft.wakame.player.equipment.ArmorChangeEvent
import cc.mewcraft.wakame.util.MojangStack
import cc.mewcraft.wakame.util.item.toNMS
import io.papermc.paper.event.player.PlayerStopUsingItemEvent
import org.bukkit.damage.DamageSource
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.block.Action
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

/**
 * 当判断一个物品是否拥有特定的 [ItemBehavior] 时 (类型绝对匹配, 不包含子类型), 尽量使用这个函数, 性能比较好.
 */
fun ItemStack.hasBehaviorExact(behavior: ItemBehavior): Boolean = toNMS().hasBehaviorExact(behavior)

inline fun <reified T : ItemBehavior> ItemStack.hasBehavior(): Boolean = toNMS().hasBehavior<T>()

inline fun <reified T : ItemBehavior> ItemStack.getBehavior(): T? = toNMS().getBehavior<T>()

inline fun ItemStack.handleBehavior(action: (ItemBehavior) -> Unit) = toNMS().handleBehavior(action)

// 注: 这些 handle... 函数都是为了方便遍历 ItemBehavior

fun ItemStack.handleLeftClick(player: Player, itemstack: ItemStack, event: PlayerItemLeftClickEvent) =
    handleBehavior { it.handleLeftClick(player, itemstack, event) }

fun ItemStack.handleRightClick(player: Player, itemstack: ItemStack, hand: PlayerItemRightClickEvent.Hand, event: PlayerItemRightClickEvent) =
    handleBehavior { it.handleRightClick(player, itemstack, hand, event) }

fun ItemStack.handleInteract(player: Player, itemstack: ItemStack, action: Action, wrappedEvent: WrappedPlayerInteractEvent) =
    handleBehavior { it.handleInteract(player, itemstack, action, wrappedEvent) }

fun ItemStack.handleInteractAtEntity(player: Player, itemstack: ItemStack, clicked: Entity, event: PlayerInteractAtEntityEvent) =
    handleBehavior { it.handleInteractAtEntity(player, itemstack, clicked, event) }

fun ItemStack.handlePlayerReceiveDamage(player: Player, itemstack: ItemStack, damageSource: DamageSource, event: PostprocessDamageEvent) =
    handleBehavior { it.handlePlayerReceiveDamage(player, itemstack, damageSource, event) }

fun ItemStack.handlePlayerAttackEntity(player: Player, itemstack: ItemStack, damagee: Entity, event: PostprocessDamageEvent) =
    handleBehavior { it.handlePlayerAttackEntity(player, itemstack, damagee, event) }

fun ItemStack.handleItemProjectileLaunch(player: Player, itemstack: ItemStack, projectile: Projectile, event: ProjectileLaunchEvent) =
    handleBehavior { it.handleItemProjectileLaunch(player, itemstack, projectile, event) }

fun ItemStack.handleItemProjectileHit(player: Player, itemstack: ItemStack, projectile: Projectile, event: ProjectileHitEvent) =
    handleBehavior { it.handleItemProjectileHit(player, itemstack, projectile, event) }

fun ItemStack.handleBreakBlock(player: Player, itemstack: ItemStack, event: BlockBreakEvent) =
    handleBehavior { it.handlePlayerBreakBlock(player, itemstack, event) }

fun ItemStack.handleDamage(player: Player, itemstack: ItemStack, event: PlayerItemDamageEvent) =
    handleBehavior { it.handleDamage(player, itemstack, event) }

fun ItemStack.handleBreak(player: Player, itemstack: ItemStack, event: PlayerItemBreakEvent) =
    handleBehavior { it.handleBreak(player, itemstack, event) }

fun ItemStack.handleEquip(player: Player, itemstack: ItemStack, equipped: Boolean, event: ArmorChangeEvent) =
    handleBehavior { it.handleEquip(player, itemstack, equipped, event) }

fun ItemStack.handleInventoryClick(player: Player, itemstack: ItemStack, event: InventoryClickEvent) =
    handleBehavior { it.handleInventoryClick(player, itemstack, event) }

fun ItemStack.handleInventoryClickOnCursor(player: Player, itemstack: ItemStack, event: InventoryClickEvent) =
    handleBehavior { it.handleInventoryClickOnCursor(player, itemstack, event) }

fun ItemStack.handleInventoryHotbarSwap(player: Player, itemstack: ItemStack, event: InventoryClickEvent) =
    handleBehavior { it.handleInventoryHotbarSwap(player, itemstack, event) }

fun ItemStack.handleRelease(player: Player, itemstack: ItemStack, event: PlayerStopUsingItemEvent) =
    handleBehavior { it.handleRelease(player, itemstack, event) }

fun ItemStack.handleConsume(player: Player, itemstack: ItemStack, event: PlayerItemConsumeEvent) =
    handleBehavior { it.handleConsume(player, itemstack, event) }

// ------------
// 用于访问 `net.minecraft.world.item.ItemStack` 上的 ItemBehavior
// ------------

fun MojangStack.hasBehaviorExact(behavior: ItemBehavior): Boolean =
    koishItem?.behaviors?.hasExact(behavior) == true

inline fun <reified T : ItemBehavior> MojangStack.hasBehavior(): Boolean =
    koishItem?.behaviors?.has(T::class) == true

inline fun <reified T : ItemBehavior> MojangStack.getBehavior(): T? =
    koishItem?.behaviors?.get(T::class)

inline fun MojangStack.handleBehavior(action: (ItemBehavior) -> Unit) {
    koishItem?.behaviors?.forEach(action)
}