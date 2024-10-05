package cc.mewcraft.wakame.item.behavior

import cc.mewcraft.wakame.event.PlayerItemSlotChangeEvent
import cc.mewcraft.wakame.event.PlayerSkillPrepareCastEvent
import cc.mewcraft.wakame.item.ItemSlot
import cc.mewcraft.wakame.player.equipment.ArmorChangeEvent
import cc.mewcraft.wakame.skill.Skill
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
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.event.player.PlayerItemDamageEvent
import org.bukkit.inventory.ItemStack

/**
 * 代表一个包含 [ItemBehavior] 的类型.
 */
sealed interface ItemBehaviorHolder

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
interface ItemBehavior : ItemBehaviorHolder {

    // 除非特别说明，所有函数的 ItemStack 参数都保证已经是合法的 NekoItem

    fun handleInteract(player: Player, itemStack: ItemStack, action: Action, wrappedEvent: PlayerInteractEvent) = Unit
    fun handleEntityInteract(player: Player, itemStack: ItemStack, clicked: Entity, event: PlayerInteractAtEntityEvent) = Unit
    fun handleAttackEntity(player: Player, itemStack: ItemStack, attacked: Entity, event: EntityDamageByEntityEvent) = Unit
    fun handleBreakBlock(player: Player, itemStack: ItemStack, event: BlockBreakEvent) = Unit
    fun handleDamage(player: Player, itemStack: ItemStack, event: PlayerItemDamageEvent) = Unit
    fun handleBreak(player: Player, itemStack: ItemStack, event: PlayerItemBreakEvent) = Unit
    fun handleEquip(player: Player, itemStack: ItemStack, equipped: Boolean, event: ArmorChangeEvent) = Unit
    fun handleSlotChange(player: Player, itemStack: ItemStack, previous: Boolean, slot: ItemSlot, event: PlayerItemSlotChangeEvent) = Unit
    fun handleInventoryClick(player: Player, itemStack: ItemStack, event: InventoryClickEvent) = Unit
    fun handleInventoryClickOnCursor(player: Player, itemStack: ItemStack, event: InventoryClickEvent) = Unit
    fun handleInventoryHotbarSwap(player: Player, itemStack: ItemStack, event: InventoryClickEvent) = Unit
    fun handleRelease(player: Player, itemStack: ItemStack, event: PlayerStopUsingItemEvent) = Unit
    fun handleConsume(player: Player, itemStack: ItemStack, event: PlayerItemConsumeEvent) = Unit
    fun handleSkillPrepareCast(caster: Player, itemStack: ItemStack, skill: Skill, event: PlayerSkillPrepareCastEvent) = Unit
}

interface ItemBehaviorType<T : ItemBehavior> : ItemBehaviorHolder {
    /**
     * Creates a [ItemBehavior].
     *
     * @return an instance of [T]
     */
    fun create(): T
}