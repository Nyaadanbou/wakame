package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.attribute.AttributeEventHandler
import cc.mewcraft.wakame.event.PlayerInventorySlotChangeEvent
import cc.mewcraft.wakame.event.PlayerSkillPrepareCastEvent
import cc.mewcraft.wakame.item.binary.tryNekoStack
import cc.mewcraft.wakame.item.schema.behavior.ItemBehavior
import cc.mewcraft.wakame.kizami.KizamiEventHandler
import cc.mewcraft.wakame.skill.SkillEventHandler
import cc.mewcraft.wakame.util.takeUnlessEmpty
import com.destroystokyo.paper.event.player.PlayerJumpEvent
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemBreakEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.event.player.PlayerItemDamageEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * 代表一个“物品交互结果”的封装.
 *
 * 物品交互结果, 即玩家使用该物品与世界发生了交互后所发生的结果.
 *
 * ## 注意事项
 * 本接口覆盖了绝大部分与世界进行交互的事件, 但这里特别不包含 [org.bukkit.event.player.PlayerItemHeldEvent] 和 [cc.mewcraft.wakame.event.PlayerInventorySlotChangeEvent].
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
 * ## [cc.mewcraft.wakame.event.PlayerInventorySlotChangeEvent]
 * 玩家背包内的某个物品发生了变化 (包括从空气变成某个物品), 没有与世界发生交互.
 * 从空气变成某个物品, 其实就包括了玩家登录时的情况. 你可以把玩家刚登录时的背包当成是空的,
 * 然后服务端会一个一个根据地图存档里的数据, 将背包里的物品一个一个填充回去.
 * 也就是说, 玩家登录时对于背包里的每个非空气物品都会触发一次该事件.
 */
class MultipleItemListener : KoinComponent, Listener {
    private val attributeEventHandler: AttributeEventHandler by inject()
    private val kizamiEventHandler: KizamiEventHandler by inject()
    private val skillEventHandler: SkillEventHandler by inject()

    @EventHandler
    fun onItemHeld(event: PlayerItemHeldEvent) {
        val player = event.player
        val previousSlot = event.previousSlot
        val newSlot = event.newSlot
        val oldItem = player.inventory.getItem(previousSlot) // it returns `null` to represent emptiness
        val newItem = player.inventory.getItem(newSlot) // same as above

        attributeEventHandler.handlePlayerItemHeld(player, previousSlot, newSlot, oldItem, newItem)
        kizamiEventHandler.handlePlayerItemHeld(player, previousSlot, newSlot, oldItem, newItem)
        skillEventHandler.handlePlayerItemHeld(player, previousSlot, newSlot, oldItem, newItem)
    }

    @EventHandler
    fun onSlotChange(event: PlayerInventorySlotChangeEvent) {
        val player = event.player
        val rawSlot = event.rawSlot
        val slot = event.slot
        val oldItem = event.oldItemStack.takeUnlessEmpty() // it always returns a non-null ItemStack - it uses AIR to represent emptiness
        val newItem = event.newItemStack.takeUnlessEmpty() // same as above

        attributeEventHandler.handlePlayerInventorySlotChange(player, rawSlot, slot, oldItem, newItem)
        kizamiEventHandler.handlePlayerInventorySlotChange(player, rawSlot, slot, oldItem, newItem)
        skillEventHandler.handlePlayerInventorySlotChange(player, rawSlot, slot, oldItem, newItem)
    }
}

/**
 * 此监听器包含的事件仅涉及到一个物品.
 */
class SingleItemListener : KoinComponent, Listener {
    private val skillEventHandler: SkillEventHandler by inject()

    @EventHandler
    fun onItemInteract(event: PlayerInteractEvent) {
        val item = event.item ?: return
        val nekoStack = item.tryNekoStack ?: return
        nekoStack.behaviors.forEach { behavior ->
            behavior.handleInteract(event.player, item, event.action, event)
        }
    }

    @EventHandler
    fun onDamageEntity(event: EntityDamageByEntityEvent) {
        // TODO: 这是一个 POC，可能需要考虑背包内的所有 Neko 物品
        val damager = event.damager as? Player ?: return
        val item = damager.inventory.itemInMainHand
        val nekoStack = item.tryNekoStack ?: return
        nekoStack.behaviors.forEach { behavior ->
            behavior.handleAttackEntity(damager, item, event.entity, event)
        }
    }

    @EventHandler
    fun onItemBreakBlock(event: BlockBreakEvent) {
        val player = event.player
        val item = player.inventory.itemInMainHand.takeIf { !it.isEmpty } ?: return
        val nekoStack = item.tryNekoStack ?: return
        nekoStack.behaviors.forEach { behavior ->
            behavior.handleBreakBlock(player, item, event)
        }
    }

    @EventHandler
    fun onItemDamage(event: PlayerItemDamageEvent) {
        val item = event.item
        val nekoStack = item.tryNekoStack ?: return
        nekoStack.behaviors.forEach { behavior ->
            behavior.handleDamage(event.player, item, event)
        }
    }

    @EventHandler
    fun onItemBreak(event: PlayerItemBreakEvent) {
        val item = event.brokenItem
        val nekoStack = item.tryNekoStack ?: return
        nekoStack.behaviors.forEach { behavior ->
            behavior.handleBreak(event.player, item, event)
        }
    }

    @EventHandler
    fun onItemConsume(event: PlayerItemConsumeEvent) {
        val item = event.item
        val nekoStack = item.tryNekoStack ?: return
        nekoStack.behaviors.forEach { behavior ->
            behavior.handleConsume(event.player, item, event)
        }
    }

    @EventHandler
    fun onSkillPrepareCast(event: PlayerSkillPrepareCastEvent) {
        val item = event.item
        val nekoStack = item?.tryNekoStack ?: return
        nekoStack.behaviors.forEach { behavior ->
            behavior.handleSkillPrepareCast(event.caster, item, event.skill, event)
        }
    }

    @EventHandler
    fun onClick(event: PlayerInteractEvent) {
        val player = event.player
        val slot = event.hand ?: return
        val item = player.inventory.itemInMainHand.takeUnlessEmpty() ?: return
        val nekoStack = item.tryNekoStack ?: return
        if (!nekoStack.slot.testEquipmentSlot(slot))
            return

        when (event.action) {
            Action.LEFT_CLICK_BLOCK -> {
                skillEventHandler.onLeftClickBlock(player, item, event.clickedBlock?.location!!, event)
            }

            Action.LEFT_CLICK_AIR -> {
                skillEventHandler.onLeftClickAir(player, item, event)
            }

            Action.RIGHT_CLICK_BLOCK -> {
                skillEventHandler.onRightClickBlock(player, item, event.clickedBlock?.location!!, event)
            }

            Action.RIGHT_CLICK_AIR -> {
                skillEventHandler.onRightClickAir(player, item, event)
            }

            else -> return
        }
    }

    @EventHandler
    fun onJump(event: PlayerJumpEvent) {
        val player = event.player
        val item = player.inventory.itemInMainHand.takeUnlessEmpty() ?: return
        val nekoStack = item.tryNekoStack ?: return

        skillEventHandler.onJump(player, item)
    }

    @EventHandler
    fun onAttack(event: EntityDamageByEntityEvent) {
        val damager = event.damager as? Player ?: return
        val entity = event.entity as? LivingEntity ?: return
        val item = damager.inventory.itemInMainHand.takeUnlessEmpty() ?: return
        val nekoStack = item.tryNekoStack ?: return

        skillEventHandler.onAttack(damager, entity, item)
    }
}