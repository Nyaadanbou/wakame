package cc.mewcraft.wakame.item2.behavior

import cc.mewcraft.wakame.event.bukkit.PlayerItemLeftClickEvent
import cc.mewcraft.wakame.event.bukkit.PlayerItemRightClickEvent
import cc.mewcraft.wakame.event.bukkit.PostprocessDamageEvent
import cc.mewcraft.wakame.event.bukkit.WrappedPlayerInteractEvent
import io.papermc.paper.event.entity.EntityEquipmentChangedEvent
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
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

/**
 * 代表一个“物品交互结果”的封装.
 *
 * 物品交互结果, 即玩家使用该物品与世界发生了交互后所发生的结果.
 *
 * ## 注意事项
 * 本接口覆盖了绝大部分与世界进行交互的事件, 但这里特别不包含 [org.bukkit.event.player.PlayerItemHeldEvent] 和 [io.papermc.paper.event.player.PlayerInventorySlotChangeEvent].
 * 因为这两事件并不在“物品交互结果”这个范畴内, 它们并没有让物品与世界发生交互, 而仅仅是玩家自身的状态发生了变化而已. 这样看来, 这两个事件不符合“物品交互结果”的定义,
 * 因此它们也不应该被放到 [ItemBehavior] 这个架构下.
 *
 * **而且经过我们的实践证明, 这两个事件确实是没有办法纳入 [ItemBehavior] 这个架构下的.**
 *
 * 下面分别解释一下这两个事件.
 *
 * ## [org.bukkit.event.player.PlayerItemHeldEvent]
 * 该事件仅仅是玩家切换了手持的物品, 没有与世界发生交互.
 * 而且, 该事件涉及到两个物品, 一个切换之前的, 一个切换之后的.
 *
 * ## [io.papermc.paper.event.player.PlayerInventorySlotChangeEvent]
 * 玩家背包内的某个物品发生了变化 (包括从空气变成某个物品), 没有与世界发生交互.
 * 从空气变成某个物品, 其实就包括了玩家登录时的情况. 你可以把玩家刚登录时的背包当成是空的,
 * 然后服务端会一个一个根据地图存档里的数据, 将背包里的物品一个一个填充回去.
 * 也就是说, 玩家登录时对于背包里的每个非空气物品都会触发一次该事件.
 */
interface ItemBehavior {

    // 除非特别说明，所有函数的 ItemStack 参数都保证已经是合法的 KoishItem

    /**
     * 玩家手持该物品对方块按下使用键(默认为鼠标右键)进行交互执行的行为.
     */
    fun handleUseOn(context: UseOnContext): InteractionResult = InteractionResult.PASS

    /**
     * 玩家手持该物品对空气按下使用键(默认为鼠标右键)进行交互执行的行为.
     */
    fun handleUse(context: UseContext): InteractionResult = InteractionResult.PASS

    /**
     * 玩家手持该物品对实体按下使用键(默认为鼠标右键)进行交互执行的行为.
     */
    fun handleUseEntity(context: UseEntityContext): InteractionResult = InteractionResult.PASS

    /**
     * 玩家手持该物品对方块按下攻击键(默认为鼠标左键)进行交互执行的行为.
     */
    fun handleAttackOn(context: AttackOnContext) = InteractionResult.PASS

    /**
     * 玩家手持该物品对空气按下攻击键(默认为鼠标左键)进行交互执行的行为.
     */
    fun handleAttack(context: AttackContext) = InteractionResult.PASS

    /**
     * 玩家手持该物品对实体按下攻击键(默认为鼠标左键)进行交互执行的行为.
     */
    fun handleAttackEntity(context: AttackEntityContext) = InteractionResult.PASS

    @Deprecated("use handleAttack/handleAttackOn/handleAttackEntity instead")
    fun handleLeftClick(player: Player, itemstack: ItemStack, event: PlayerItemLeftClickEvent) = Unit

    @Deprecated("use handleUseOn/handleUse instead")
    fun handleRightClick(player: Player, itemstack: ItemStack, hand: EquipmentSlot, event: PlayerItemRightClickEvent) = Unit

    @Deprecated("use handleUseOn/handleUse instead")
    fun handleInteract(player: Player, itemstack: ItemStack, action: Action, wrappedEvent: WrappedPlayerInteractEvent) = Unit

    @Deprecated("use handleUseEntity instead")
    fun handleInteractAtEntity(player: Player, itemstack: ItemStack, clicked: Entity, event: PlayerInteractAtEntityEvent) = Unit

    // TODO 以下需要整理和重命名
    fun handlePlayerReceiveDamage(player: Player, itemstack: ItemStack, damageSource: DamageSource, event: PostprocessDamageEvent) = Unit

    fun handlePlayerAttackEntity(player: Player, itemstack: ItemStack, damagee: Entity, event: PostprocessDamageEvent) = Unit

    fun handleItemProjectileLaunch(player: Player, itemstack: ItemStack, projectile: Projectile, event: ProjectileLaunchEvent) = Unit

    fun handleItemProjectileHit(player: Player, itemstack: ItemStack, projectile: Projectile, event: ProjectileHitEvent) = Unit

    fun handlePlayerBreakBlock(player: Player, itemstack: ItemStack, event: BlockBreakEvent) = Unit

    fun handleDamage(player: Player, itemstack: ItemStack, event: PlayerItemDamageEvent) = Unit

    fun handleBreak(player: Player, itemstack: ItemStack, event: PlayerItemBreakEvent) = Unit

    fun handleEquip(player: Player, itemstack: ItemStack, slot: EquipmentSlot, equipped: Boolean, event: EntityEquipmentChangedEvent) = Unit

    fun handleInventoryClick(player: Player, itemstack: ItemStack, event: InventoryClickEvent) = Unit

    fun handleInventoryClickOnCursor(player: Player, itemstack: ItemStack, event: InventoryClickEvent) = Unit

    fun handleInventoryHotbarSwap(player: Player, itemstack: ItemStack, event: InventoryClickEvent) = Unit

    fun handleRelease(player: Player, itemstack: ItemStack, event: PlayerStopUsingItemEvent) = Unit

    fun handleConsume(player: Player, itemstack: ItemStack, event: PlayerItemConsumeEvent) = Unit

}
