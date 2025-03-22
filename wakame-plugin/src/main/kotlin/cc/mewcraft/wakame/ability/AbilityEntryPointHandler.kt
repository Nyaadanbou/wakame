package cc.mewcraft.wakame.ability

import cc.mewcraft.wakame.ability.combo.PlayerComboResult
import cc.mewcraft.wakame.ability.trigger.AbilitySingleTrigger
import cc.mewcraft.wakame.ecs.bridge.koishify
import cc.mewcraft.wakame.item.extension.playerAbilities
import cc.mewcraft.wakame.item.wrap
import cc.mewcraft.wakame.user.toUser
import org.bukkit.entity.*
import org.bukkit.event.Cancellable
import org.bukkit.event.player.PlayerInteractEvent
import xyz.xenondevs.commons.collections.takeUnlessEmpty

/**
 * 控制 [Ability] 开始执行的逻辑.
 *
 * 具体来说, 这个类负责:
 * - 根据玩家的操作, 处理玩家的机制触发逻辑
 * - 根据玩家的物品, 从玩家身上添加/移除技能
 */
internal object AbilityEntryPointHandler {

    fun onLeftClickBlock(player: Player, event: PlayerInteractEvent) {
        onLeftClick(player, event)
    }

    fun onLeftClickAir(player: Player, event: PlayerInteractEvent) {
        onLeftClick(player, event)
    }

    private fun onLeftClick(player: Player, event: PlayerInteractEvent) {
        val user = player.toUser()
        val result = user.combo.addTrigger(AbilitySingleTrigger.LEFT_CLICK)
        if (result == PlayerComboResult.CANCEL_EVENT) {
            event.isCancelled = true
        }
    }

    fun onRightClickBlock(player: Player, event: PlayerInteractEvent) {
        onRightClick(player, event)
    }

    fun onRightClickAir(player: Player, event: PlayerInteractEvent) {
        onRightClick(player, event)
    }

    private fun onRightClick(player: Player, event: PlayerInteractEvent) {
        val user = player.toUser()
        val result = user.combo.addTrigger(AbilitySingleTrigger.RIGHT_CLICK)
        tryApplyAbilityResult(result, event)
    }

    fun onProjectileHit(projectile: Projectile, hitEntity: Entity?) {
        when (projectile) {
            is AbstractArrow -> {
                val koishStack = projectile.itemStack.wrap() ?: return
                val abilities = koishStack.playerAbilities.takeUnlessEmpty() ?: return
                val target = (hitEntity as? LivingEntity)?.koishify() ?: projectile.attachedBlock?.koishify() ?: return
                for (ability in abilities) {
                    ability.cast(projectile.shooter as Player, target)
                }
            }
        }
    }

    private fun tryApplyAbilityResult(result: PlayerComboResult, event: Cancellable) {
        if (result == PlayerComboResult.CANCEL_EVENT) {
            event.isCancelled = true
        }
    }
}