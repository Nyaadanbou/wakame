package cc.mewcraft.wakame.ability

import cc.mewcraft.wakame.ability2.AbilityCastManager
import cc.mewcraft.wakame.ability2.combo.PlayerComboResult
import cc.mewcraft.wakame.ability2.trigger.AbilitySingleTrigger
import cc.mewcraft.wakame.item2.data.ItemDataTypes
import cc.mewcraft.wakame.item2.getData
import cc.mewcraft.wakame.user.toUser
import cc.mewcraft.wakame.util.item.takeUnlessEmpty
import org.bukkit.entity.AbstractArrow
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.Cancellable
import org.bukkit.event.player.PlayerInteractEvent

/**
 * 控制 [cc.mewcraft.wakame.ability2.AbilityObject] 开始执行的逻辑.
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
                val itemStack = projectile.itemStack.takeUnlessEmpty() ?: return
                val abilities = itemStack.getData(ItemDataTypes.ABILITY_OBJECT) ?: return
                val caster = projectile.shooter as? LivingEntity ?: return
                val target = hitEntity ?: return
                AbilityCastManager.castObject(abilities, caster, target)
            }
        }
    }

    private fun tryApplyAbilityResult(result: PlayerComboResult, event: Cancellable) {
        if (result == PlayerComboResult.CANCEL_EVENT) {
            event.isCancelled = true
        }
    }
}