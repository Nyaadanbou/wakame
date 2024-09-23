package cc.mewcraft.wakame.player.attackspeed

import cc.mewcraft.wakame.item.ItemSlot
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.tryNekoStack
import cc.mewcraft.wakame.packet.addFakePotionEffect
import cc.mewcraft.wakame.packet.removeFakePotionEffect
import cc.mewcraft.wakame.user.User
import cc.mewcraft.wakame.user.toUser
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class AttackSpeedEventHandler {

    fun handlePlayerAttackEntity(damager: Player, item: ItemStack, event: EntityDamageByEntityEvent) {
        val nekoStack = item.tryNekoStack ?: return
        val user = damager.toUser()
        if (user.attackSpeed.isActive(nekoStack.key)) {
            event.isCancelled = true
            return
        }
        tryApplyCooldown(user, nekoStack)
    }

    fun handlePlayerShootBow(damager: Player, item: ItemStack, event: EntityShootBowEvent) {
        val nekoStack = item.tryNekoStack ?: return
        val user = damager.toUser()
        if (user.attackSpeed.isActive(nekoStack.key)) {
            event.isCancelled = true
            return
        }
        tryApplyCooldown(user, nekoStack)
    }

    fun handlePlayerSlotChange(player: Player, slot: ItemSlot, oldItem: ItemStack?, newItem: ItemStack?) {
        if (oldItem != null) {
            oldItem.tryNekoStack?.getAttackSpeedLevel() ?: return
            removeEffect(player)
        }
        if (newItem != null) {
            val attackSpeedLevel = newItem.tryNekoStack?.getAttackSpeedLevel() ?: return
            sendEffect(player, attackSpeedLevel)
        }
    }

    private fun NekoStack.getAttackSpeedLevel(): AttackSpeedLevel? {
        return components.get(ItemComponentTypes.ATTACK_SPEED)?.level
    }

    private fun tryApplyCooldown(user: User<Player>, stack: NekoStack) {
        val attackSpeedLevel = stack.getAttackSpeedLevel() ?: return
        // 设置实际冷却
        user.attackSpeed.activate(stack.key, attackSpeedLevel)
        // 应用视觉冷却
        user.player.setCooldown(stack.itemType, attackSpeedLevel.cooldown)
    }

    private fun sendEffect(player: Player, level: AttackSpeedLevel) {
        val fatigueLevel = level.fatigueLevel ?: return
        val miningFatigueEffect = PotionEffect(
            /* type = */ PotionEffectType.MINING_FATIGUE,
            /* duration = */ -1,
            /* amplifier = */ fatigueLevel,
            /* ambient = */ false,
            /* particles = */ false,
            /* icon = */ false
        )
        player.addFakePotionEffect(miningFatigueEffect)
    }

    private fun removeEffect(player: Player) {
        player.removeFakePotionEffect(PotionEffectType.MINING_FATIGUE)
    }
}