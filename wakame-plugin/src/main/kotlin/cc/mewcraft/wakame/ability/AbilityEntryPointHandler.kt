package cc.mewcraft.wakame.ability

import cc.mewcraft.wakame.ability.state.PlayerComboResult
import cc.mewcraft.wakame.ability.trigger.SingleTrigger
import cc.mewcraft.wakame.ecs.bridge.koishify
import cc.mewcraft.wakame.item.ItemSlot
import cc.mewcraft.wakame.item.extension.playerAbilities
import cc.mewcraft.wakame.item.wrap
import cc.mewcraft.wakame.user.toUser
import org.bukkit.entity.AbstractArrow
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.Cancellable
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
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
        val result = user.combo.addTrigger(SingleTrigger.LEFT_CLICK)
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
        val result = user.combo.addTrigger(SingleTrigger.RIGHT_CLICK)
        tryApplyAbilityResult(result, event)
    }

    fun onAttack(player: Player, itemStack: ItemStack?, event: EntityDamageByEntityEvent) {
        val user = player.toUser()
        itemStack?.wrap() ?: return // 非萌芽物品应该完全不用处理吧?
        val result = user.combo.addTrigger(SingleTrigger.ATTACK)
        tryApplyAbilityResult(result, event)
    }

    fun onProjectileHit(projectile: Projectile, hitEntity: Entity?) {
        when (projectile) {
            is AbstractArrow -> {
                val koishStack = projectile.itemStack.wrap() ?: return
                val abilities = koishStack.playerAbilities.takeUnlessEmpty() ?: return
                val target = (hitEntity as? LivingEntity)?.koishify() ?: projectile.attachedBlock?.koishify() ?: return
                for (ability in abilities) {
                    ability.recordBy(projectile.shooter as Player, target, ItemSlot.imaginary() to koishStack)
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