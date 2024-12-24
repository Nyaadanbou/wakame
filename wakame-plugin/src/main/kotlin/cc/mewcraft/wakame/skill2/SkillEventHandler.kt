package cc.mewcraft.wakame.skill2

import cc.mewcraft.wakame.item.ItemSlot
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.shadowNeko
import cc.mewcraft.wakame.skill2.character.CasterAdapter
import cc.mewcraft.wakame.skill2.character.TargetAdapter
import cc.mewcraft.wakame.skill2.state.SkillStateResult
import cc.mewcraft.wakame.skill2.trigger.SingleTrigger
import cc.mewcraft.wakame.user.toUser
import org.bukkit.entity.*
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
internal class SkillEventHandler {

    /* 玩家的技能触发逻辑 */

    fun onLeftClickBlock(player: Player, event: PlayerInteractEvent) {
        onLeftClick(player, event)
    }

    fun onLeftClickAir(player: Player, event: PlayerInteractEvent) {
        onLeftClick(player, event)
    }

    private fun onLeftClick(
        player: Player,
        event: PlayerInteractEvent,
    ) {
        val user = player.toUser()
        val result = user.skillState.addTrigger(SingleTrigger.LEFT_CLICK)
        if (result == SkillStateResult.CANCEL_EVENT) {
            event.isCancelled = true
        }
    }

    fun onRightClickBlock(player: Player, event: PlayerInteractEvent) {
        onRightClick(player, event)
    }

    fun onRightClickAir(player: Player, event: PlayerInteractEvent) {
        onRightClick(player, event)
    }

    private fun onRightClick(
        player: Player,
        event: PlayerInteractEvent,
    ) {
        val user = player.toUser()
        val result = user.skillState.addTrigger(SingleTrigger.RIGHT_CLICK)
        checkResult(result, event)
    }

    fun onAttack(player: Player, itemStack: ItemStack?, event: EntityDamageByEntityEvent) {
        val user = player.toUser()
        itemStack?.shadowNeko(false) ?: return // 非萌芽物品应该完全不用处理吧?
        val result = user.skillState.addTrigger(SingleTrigger.ATTACK)
        checkResult(result, event)
    }

    fun onProjectileHit(projectile: Projectile, hitEntity: Entity?) {
        when (projectile) {
            is AbstractArrow -> {
                val nekoStack = projectile.itemStack.shadowNeko(false) ?: return
                val cells = nekoStack.components.get(ItemComponentTypes.CELLS) ?: return
                val skills = cells.collectSkillModifiers(nekoStack, ItemSlot.imaginary())
                val target = (hitEntity as? LivingEntity)?.let { TargetAdapter.adapt(it) } ?: TargetAdapter.adapt(projectile.location)
                for (skill in skills) {
                    skill.recordBy(CasterAdapter.adapt(projectile.shooter as Player), target, ItemSlot.imaginary() to nekoStack)
                }
            }
        }
    }

    private fun checkResult(result: SkillStateResult, event: Cancellable) {
        if (result == SkillStateResult.CANCEL_EVENT) {
            event.isCancelled = true
        }
    }
}