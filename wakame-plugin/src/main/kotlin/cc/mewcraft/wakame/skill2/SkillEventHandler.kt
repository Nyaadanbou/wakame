package cc.mewcraft.wakame.skill2

import cc.mewcraft.wakame.item.ItemSlot
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.toNekoStack
import cc.mewcraft.wakame.item.tryNekoStack
import cc.mewcraft.wakame.skill2.character.CasterAdapter
import cc.mewcraft.wakame.skill2.character.Target
import cc.mewcraft.wakame.skill2.character.TargetAdapter
import cc.mewcraft.wakame.skill2.character.toComposite
import cc.mewcraft.wakame.skill2.context.ImmutableSkillContext
import cc.mewcraft.wakame.skill2.state.SkillStateResult
import cc.mewcraft.wakame.skill2.trigger.SingleTrigger
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
        targetProvider: () -> Target,
    ) {
        val user = player.toUser()
        val nekoStack = itemStack.toNekoStack
        val target = targetProvider.invoke()
        val result = user.skillState.addTrigger(SingleTrigger.LEFT_CLICK, ImmutableSkillContext(CasterAdapter.adapt(player).toComposite(), target, nekoStack))
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
        val result = user.skillState.addTrigger(SingleTrigger.RIGHT_CLICK, ImmutableSkillContext(CasterAdapter.adapt(player).toComposite(), target, nekoStack))
        checkResult(result, event)
    }

    fun onAttack(player: Player, entity: LivingEntity, itemStack: ItemStack?, event: EntityDamageByEntityEvent) {
        val user = player.toUser()
        val nekoStack = itemStack?.tryNekoStack ?: return // 非萌芽物品应该完全不用处理吧?
        val result = user.skillState.addTrigger(SingleTrigger.ATTACK, ImmutableSkillContext(CasterAdapter.adapt(player).toComposite(), TargetAdapter.adapt(entity), nekoStack))
        checkResult(result, event)
    }

    fun onProjectileHit(projectile: Projectile, hitEntity: Entity?) {
        when (projectile) {
            is AbstractArrow -> {
                val nekoStack = projectile.itemStack.tryNekoStack ?: return
                val cells = nekoStack.components.get(ItemComponentTypes.CELLS) ?: return
                // FIXME 这里有潜在 BUG, 详见: https://github.com/Nyaadanbou/wakame/issues/132
                val skills = cells.collectSkillModifiers(nekoStack, ItemSlot.imaginary())
                val target = (hitEntity as? LivingEntity)?.let { TargetAdapter.adapt(it) } ?: TargetAdapter.adapt(projectile.location)
//                val context = SkillContext(CasterAdapter.adapt(projectile), target, nekoStack)
//                skills.values().map { it.cast(context) }.forEach { ticker.schedule(it) }
            }
        }
    }

    private fun checkResult(result: SkillStateResult, event: Cancellable) {
        if (result == SkillStateResult.CANCEL_EVENT) {
            event.isCancelled = true
        }
    }
}