package cc.mewcraft.wakame.item2

import cc.mewcraft.wakame.item2.behavior.ItemBehavior
import cc.mewcraft.wakame.item2.config.property.ItemPropertyType
import cc.mewcraft.wakame.item2.data.ItemDataContainer
import cc.mewcraft.wakame.item2.data.ItemDataType
import cc.mewcraft.wakame.item2.data.ItemDataTypes
import cc.mewcraft.wakame.mixin.support.DataComponentsPatch
import cc.mewcraft.wakame.player.equipment.ArmorChangeEvent
import cc.mewcraft.wakame.registry2.KoishRegistries2
import cc.mewcraft.wakame.util.Identifier
import cc.mewcraft.wakame.util.MojangStack
import cc.mewcraft.wakame.util.item.unwrapToMojang
import io.papermc.paper.event.player.PlayerStopUsingItemEvent
import org.bukkit.craftbukkit.inventory.CraftItemType
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


// ------------------
// 用于访问 `org.bukkit.inventory.ItemStack` 上的自定义数据
// ------------------

//// Base

val ItemStack.isKoish: Boolean get() = unwrapToMojang().isKoish
val ItemStack.koishItem: KoishItem? get() = unwrapToMojang().koishItem
val ItemStack.dataContainer: ItemDataContainer? get() = unwrapToMojang().dataContainer
val ItemStack.koishProxy: KoishItemProxy? get() = unwrapToMojang().koishProxy

//// Behavior

fun ItemStack.hasBehavior(behavior: ItemBehavior): Boolean = unwrapToMojang().hasBehavior(behavior)
fun ItemStack.forEachBehavior(action: (ItemBehavior) -> Unit) = unwrapToMojang().forEachBehavior(action)

//<editor-fold desc="Fast access to call ItemBehavior functions" defaultstate="collapsed">
// FIXME #350
//fun ItemStack.handleInteract(player: Player, itemstack: ItemStack, action: Action, wrappedEvent: WrappedPlayerInteractEvent) =
//    forEachBehavior { it.handleInteract(player, itemstack, action, wrappedEvent) }

fun ItemStack.handleInteractAtEntity(player: Player, itemstack: ItemStack, clicked: Entity, event: PlayerInteractAtEntityEvent) =
    forEachBehavior { it.handleInteractAtEntity(player, itemstack, clicked, event) }

// FIXME #350
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

// FIXME #350
//fun ItemStack.handleAbilityPrepareCast(caster: Player, itemstack: ItemStack, ability: Ability, event: PlayerAbilityPrepareCastEvent) =
//    forEachBehavior { it.handleAbilityPrepareCast(caster, itemstack, ability, event) }
//</editor-fold>

//// Property

fun <T> ItemStack.hasProperty(type: ItemPropertyType<T>): Boolean = unwrapToMojang().hasProperty(type)
fun <T> ItemStack.getProperty(type: ItemPropertyType<out T>): T? = unwrapToMojang().getProperty(type)

//// ItemData

fun ItemStack.hasData(type: ItemDataType<*>): Boolean = unwrapToMojang().hasData(type)
fun <T> ItemStack.getData(type: ItemDataType<out T>): T? = unwrapToMojang().getData(type)
fun <T> ItemStack.getDataOrDefault(type: ItemDataType<out T>, fallback: T): T? = unwrapToMojang().getDataOrDefault(type, fallback)
fun <T> ItemStack.setData(type: ItemDataType<in T>, value: T): T? = unwrapToMojang().setData(type, value)
fun <T> ItemStack.removeData(type: ItemDataType<out T>): T? = unwrapToMojang().removeData(type)

// ------------------
// 用于访问 `net.minecraft.world.item.ItemStack` 上的自定义数据
// ------------------

//// Base

val MojangStack.isKoish: Boolean
    get() = koishItem != null

val MojangStack.koishItem: KoishItem?
    get() = dataContainer?.get(ItemDataTypes.ID)?.koishItem

val MojangStack.dataContainer: ItemDataContainer?
    get() = get(DataComponentsPatch.ITEM_DATA_CONTAINER) ?: koishProxy?.data

val MojangStack.koishProxy: KoishItemProxy?
    get() = KoishRegistries2.ITEM_PROXY[id]

//// Behavior

fun MojangStack.hasBehavior(behavior: ItemBehavior): Boolean =
    koishItem?.behaviors?.has(behavior) == true

fun MojangStack.forEachBehavior(action: (ItemBehavior) -> Unit) {
    koishItem?.behaviors?.forEach(action)
}

//// Property

fun <T> MojangStack.getProperty(type: ItemPropertyType<out T>): T? =
    koishItem?.properties?.get(type)

fun <T> MojangStack.hasProperty(type: ItemPropertyType<T>): Boolean =
    koishItem?.properties?.has(type) == true

//// ItemData

fun MojangStack.hasData(type: ItemDataType<*>): Boolean =
    dataContainer?.has(type) == true

fun <T> MojangStack.getData(type: ItemDataType<out T>): T? =
    dataContainer?.get(type)

fun <T> MojangStack.getDataOrDefault(type: ItemDataType<out T>, fallback: T): T? =
    dataContainer?.getOrDefault(type, fallback)

fun <T> MojangStack.setData(type: ItemDataType<in T>, value: T): T? {
    blockWriteToProxy() // FIXME #350: 更严格的实现?

    val builder = dataContainer?.toBuilder() ?: return null
    val oldValue = builder.set(type, value)
    set(DataComponentsPatch.ITEM_DATA_CONTAINER, builder.build())
    return oldValue
}

fun <T> MojangStack.removeData(type: ItemDataType<out T>): T? {
    blockWriteToProxy()

    val builder = dataContainer?.toBuilder() ?: return null
    val oldValue = builder.remove(type)
    set(DataComponentsPatch.ITEM_DATA_CONTAINER, builder.build())
    return oldValue
}

private fun MojangStack.blockWriteToProxy() {
    if (koishProxy != null) throw IllegalStateException("Cannot write data on ${KoishItemProxy::class.simpleName}")
}

// -----------------
// 方便函数
// -----------------

// 获得一个 ItemStack 的字符串形式的命名空间 ID
private val MojangStack.id: Identifier
    get() = CraftItemType.minecraftToBukkit(item).key()
