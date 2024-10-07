package cc.mewcraft.wakame.player.attackspeed

import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.tryNekoStack
import cc.mewcraft.wakame.user.User
import cc.mewcraft.wakame.user.toUser
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.inventory.ItemStack

class AttackSpeedEventHandler {
    fun handlePlayerAttackEntity(damager: Player, item: ItemStack, event: EntityDamageByEntityEvent) {
        val nekoStack = item.tryNekoStack ?: return
        val user = damager.toUser()
        if (user.attackSpeed.isActive(nekoStack.id)) {
            event.isCancelled = true
            return
        }
        tryApplyCooldown(user, nekoStack)
    }

    fun handlePlayerShootBow(damager: Player, item: ItemStack, event: EntityShootBowEvent) {
        val nekoStack = item.tryNekoStack ?: return
        val user = damager.toUser()
        if (user.attackSpeed.isActive(nekoStack.id)) {
            event.isCancelled = true
            return
        }
        tryApplyCooldown(user, nekoStack)
    }

    private fun NekoStack.getAttackSpeedLevel(): AttackSpeedLevel? {
        return components.get(ItemComponentTypes.ATTACK_SPEED)?.level
    }

    private fun tryApplyCooldown(user: User<Player>, stack: NekoStack) {
        val attackSpeedLevel = stack.getAttackSpeedLevel() ?: return
        // 设置实际冷却
        user.attackSpeed.activate(stack.id, attackSpeedLevel)
        // 应用视觉冷却
        user.player.setCooldown(stack.itemType, attackSpeedLevel.cooldown)
    }
}