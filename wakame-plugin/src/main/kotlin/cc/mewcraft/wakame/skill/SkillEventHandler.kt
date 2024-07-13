package cc.mewcraft.wakame.skill

import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.template.ItemTemplateTypes
import cc.mewcraft.wakame.item.toNekoStack
import cc.mewcraft.wakame.item.tryNekoStack
import cc.mewcraft.wakame.skill.context.SkillContext
import cc.mewcraft.wakame.skill.state.SkillStateResult
import cc.mewcraft.wakame.skill.trigger.SingleTrigger
import cc.mewcraft.wakame.user.toUser
import org.bukkit.Location
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

/**
 * 技能系统与事件系统的交互逻辑.
 *
 * 该类:
 * - 根据玩家的操作, 处理玩家的技能触发逻辑
 * - 根据玩家的物品, 从玩家身上添加/移除技能
 */
class SkillEventHandler {

    /* 玩家的技能触发逻辑 */

    fun onLeftClickBlock(player: Player, itemStack: ItemStack, location: Location, event: PlayerInteractEvent) {
        onLeftClick(player, itemStack, event) { TargetAdapter.adapt(location) }
    }

    fun onLeftClickAir(player: Player, itemStack: ItemStack, event: PlayerInteractEvent) {
        onLeftClick(player, itemStack, event) {
            val targetEntity = player.getTargetEntity(16)
            if (targetEntity == null || targetEntity !is LivingEntity) {
                TargetAdapter.adapt(player)
            } else {
                TargetAdapter.adapt(targetEntity)
            }
        }
    }

    private fun onLeftClick(
        player: Player,
        itemStack: ItemStack,
        event: PlayerInteractEvent,
        targetProvider: () -> Target
    ) {
        val user = player.toUser()
        val nekoStack = itemStack.toNekoStack
        val target = targetProvider.invoke()
        val result = user.skillState.addTrigger(SingleTrigger.LEFT_CLICK, SkillContext(CasterAdapter.adapt(player), target, nekoStack))
        if (result == SkillStateResult.CANCEL_EVENT) {
            event.isCancelled = true
        }
    }

    fun onRightClickBlock(player: Player, itemStack: ItemStack, location: Location, event: PlayerInteractEvent) {
        onRightClick(player, itemStack, event) { TargetAdapter.adapt(location) }
    }

    fun onRightClickAir(player: Player, itemStack: ItemStack, event: PlayerInteractEvent) {
        onRightClick(player, itemStack, event) {
            val targetEntity = player.getTargetEntity(16)
            if (targetEntity == null || targetEntity !is LivingEntity) {
                TargetAdapter.adapt(player)
            } else {
                TargetAdapter.adapt(targetEntity)
            }
        }
    }

    private fun onRightClick(
        player: Player,
        itemStack: ItemStack,
        event: PlayerInteractEvent,
        targetProvider: () -> Target
    ) {
        val user = player.toUser()
        val nekoStack = itemStack.toNekoStack
        val target = targetProvider.invoke()
        val result = user.skillState.addTrigger(SingleTrigger.RIGHT_CLICK, SkillContext(CasterAdapter.adapt(player), target, nekoStack))
        checkResult(result, event)
    }

    fun onAttack(player: Player, entity: LivingEntity, itemStack: ItemStack?, event: EntityDamageByEntityEvent) {
        val user = player.toUser()
        val nekoStack = itemStack?.tryNekoStack
        val result = user.skillState.addTrigger(SingleTrigger.ATTACK, SkillContext(CasterAdapter.adapt(player), TargetAdapter.adapt(entity), nekoStack))
        checkResult(result, event)
    }

    private fun checkResult(result: SkillStateResult, event: Cancellable) {
        if (result == SkillStateResult.CANCEL_EVENT) {
            event.isCancelled = true
        }
    }

    /* 玩家的技能添加/移除逻辑 */

    /**
     * 玩家切换当前手持物品时, 执行的逻辑.
     *
     * @param player
     * @param previousSlot
     * @param newSlot
     * @param oldItem 之前“激活”的物品; 如果为空气, 则应该传入 `null`
     * @param newItem 当前“激活”的物品; 如果为空气, 则应该传入 `null`
     */
    fun handlePlayerItemHeld(
        player: Player,
        previousSlot: Int,
        newSlot: Int,
        oldItem: ItemStack?,
        newItem: ItemStack?,
    ) {
        updateSkills(player, oldItem, newItem) {
            it.slot.testItemHeldEvent(player, previousSlot, newSlot)
                    && it.templates.has(ItemTemplateTypes.CASTABLE)
        }
        player.toUser().skillState.clear()
    }

    /**
     * 玩家背包里的物品发生变化时, 执行的逻辑.
     *
     * @param player
     * @param rawSlot
     * @param slot
     * @param oldItem 之前“激活”的物品; 如果为空气, 则应该传入 `null`
     * @param newItem 当前“激活”的物品; 如果为空气, 则应该传入 `null`
     */
    fun handlePlayerInventorySlotChange(
        player: Player,
        rawSlot: Int,
        slot: Int,
        oldItem: ItemStack?,
        newItem: ItemStack?,
    ) {
        updateSkills(player, oldItem, newItem) {
            it.slot.testInventorySlotChangeEvent(player, slot, rawSlot)
                    && it.templates.has(ItemTemplateTypes.CASTABLE)
        }
        player.toUser().skillState.clear()
    }

    /**
     * 根据玩家之前“激活”的物品和当前“激活”的物品所提供的属性, 更新玩家的铭技能
     *
     * 这里的新/旧指的是玩家先前“激活”的物品和当前“激活”的物品.
     *
     * @param player 玩家
     * @param oldItem 之前“激活”的物品; 如果为空气, 则应该传入 `null`
     * @param newItem 当前“激活”的物品; 如果为空气, 则应该传入 `null`
     * @param predicate 判断物品能否提供技能的谓词
     */
    private fun updateSkills(
        player: Player,
        oldItem: ItemStack?,
        newItem: ItemStack?,
        predicate: (NekoStack) -> Boolean,
    ) {
        oldItem?.tryNekoStack?.removeSkills(player, predicate)
        newItem?.tryNekoStack?.attachSkills(player, predicate)
    }

    /**
     * 把物品提供的技能添加到 [player] 身上.
     *
     * @param player 要添加技能的玩家
     * @param predicate 判断物品能否提供技能的谓词
     * @receiver 可能提供技能的物品
     */
    private fun NekoStack.attachSkills(player: Player, predicate: (NekoStack) -> Boolean) {
        if (!predicate(this)) {
            return
        }
        val itemCells = this.components.get(ItemComponentTypes.CELLS) ?: return
        val configuredSkills = itemCells.collectConfiguredSkills(this, ignoreCurse = true, ignoreVariant = true) // TODO: no more ignorance if skill module is complete
        val skillMap = player.toUser().skillMap
        skillMap.addSkillsByInstance(configuredSkills)
    }

    /**
     * 把物品提供的技能从 [player] 身上移除.
     *
     * @param player 要移除技能的玩家
     * @param predicate 判断物品能否提供技能的谓词
     * @receiver 可能提供技能的物品
     */
    private fun NekoStack.removeSkills(player: Player, predicate: (NekoStack) -> Boolean) {
        if (!predicate(this)) {
            return
        }
        val itemCells = this.components.get(ItemComponentTypes.CELLS) ?: return
        val configuredSkills = itemCells.collectConfiguredSkills(this, ignoreCurse = true, ignoreVariant = true) // TODO: no more ignorance if skill module is complete
        val skillMap = player.toUser().skillMap
        skillMap.removeSkill(configuredSkills)
    }
}