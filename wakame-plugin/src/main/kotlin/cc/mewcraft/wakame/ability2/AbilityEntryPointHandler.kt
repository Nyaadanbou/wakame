package cc.mewcraft.wakame.ability2

import cc.mewcraft.wakame.ability2.combo.PlayerComboResult
import cc.mewcraft.wakame.ability2.trigger.AbilitySingleTrigger
import cc.mewcraft.wakame.item2.config.property.ItemPropertyTypes
import cc.mewcraft.wakame.item2.getProperty
import cc.mewcraft.wakame.user.combo
import cc.mewcraft.wakame.util.item.takeUnlessEmpty
import org.bukkit.entity.*
import org.bukkit.event.Cancellable
import org.bukkit.event.player.PlayerInteractEvent

/**
 * 控制 [cc.mewcraft.wakame.item2.config.property.impl.AbilityOnItem] 开始执行的逻辑.
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
        val result = player.combo.addTrigger(AbilitySingleTrigger.LEFT_CLICK)
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
        val result = player.combo.addTrigger(AbilitySingleTrigger.RIGHT_CLICK)
        tryApplyAbilityResult(result, event)
    }

    fun onProjectileHit(projectile: Projectile, hitEntity: Entity?) {
        when (projectile) {
            is AbstractArrow -> {
                val itemStack = projectile.itemStack.takeUnlessEmpty() ?: return
                val abilityOnItem = itemStack.getProperty(ItemPropertyTypes.ABILITY) ?: return
                val caster = projectile.shooter as? LivingEntity ?: return
                val target = hitEntity ?: return
                AbilityCastUtils.castPoint(abilityOnItem.meta, caster, target)
            }
        }
    }

    private fun tryApplyAbilityResult(result: PlayerComboResult, event: Cancellable) {
        if (result == PlayerComboResult.CANCEL_EVENT) {
            event.isCancelled = true
        }
    }
}