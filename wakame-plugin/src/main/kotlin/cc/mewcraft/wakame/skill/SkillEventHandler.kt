package cc.mewcraft.wakame.skill

import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.toNekoStack
import cc.mewcraft.wakame.item.tryNekoStack
import cc.mewcraft.wakame.skill.context.SkillContext
import cc.mewcraft.wakame.skill.state.SkillStateResult
import cc.mewcraft.wakame.skill.trigger.SingleTrigger
import cc.mewcraft.wakame.tick.Ticker
import cc.mewcraft.wakame.user.toUser
import org.bukkit.Location
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
class SkillEventHandler(
    private val ticker: Ticker,
) {

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
        targetProvider: () -> Target,
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
        targetProvider: () -> Target,
    ) {
        val user = player.toUser()
        val nekoStack = itemStack.toNekoStack
        val target = targetProvider.invoke()
        val result = user.skillState.addTrigger(SingleTrigger.RIGHT_CLICK, SkillContext(CasterAdapter.adapt(player), target, nekoStack))
        checkResult(result, event)
    }

    fun onAttack(player: Player, entity: LivingEntity, itemStack: ItemStack?, event: EntityDamageByEntityEvent) {
        val user = player.toUser()
        val nekoStack = itemStack?.tryNekoStack ?: return // 非萌芽物品应该完全不用处理吧?
        val result = user.skillState.addTrigger(SingleTrigger.ATTACK, SkillContext(CasterAdapter.adapt(player), TargetAdapter.adapt(entity), nekoStack))
        checkResult(result, event)
    }

    fun onProjectileHit(projectile: Projectile, hitEntity: Entity?) {
        when (projectile) {
            is AbstractArrow -> {
                val nekoStack = projectile.itemStack.tryNekoStack ?: return
                val cells = nekoStack.components.get(ItemComponentTypes.CELLS) ?: return
                val skills = cells.collectSkillInstances(nekoStack)
                val target = (hitEntity as? LivingEntity)?.let { TargetAdapter.adapt(it) } ?: TargetAdapter.adapt(projectile.location)
                val context = SkillContext(CasterAdapter.adapt(projectile), target, nekoStack)
                skills.values().map { it.cast(context) }.forEach { ticker.schedule(it) }
            }
        }
    }

    private fun checkResult(result: SkillStateResult, event: Cancellable) {
        if (result == SkillStateResult.CANCEL_EVENT) {
            event.isCancelled = true
        }
    }
}