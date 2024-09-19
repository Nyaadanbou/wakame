package cc.mewcraft.wakame.attackspeed

import cc.mewcraft.wakame.item.ItemSlot
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.tryNekoStack
import cc.mewcraft.wakame.packet.SendPacketUtil
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
        if (user.attackSpeed.isCooldown(nekoStack.key)) {
            event.isCancelled = true
            return
        }
        setCooldown(user, nekoStack)
    }

    fun handlePlayerShootBow(damager: Player, item: ItemStack, event: EntityShootBowEvent) {
        val nekoStack = item.tryNekoStack ?: return
        val user = damager.toUser()

        if (user.attackSpeed.isCooldown(nekoStack.key)) {
            event.isCancelled = true
            return
        }
        setCooldown(user, nekoStack)
    }

    fun handlePlayerSlotChange(player: Player, slot: ItemSlot, oldItem: ItemStack?, newItem: ItemStack?) {
        val user = player.toUser()
        if (oldItem != null) {
            val oldStack = oldItem.tryNekoStack ?: return
            getItemAttackSpeedLevel(oldStack) ?: return
            user.attackSpeed.removeCooldown(oldStack.key)
            removeEffect(player)
        }
        if (newItem != null) {
            val newStack = newItem.tryNekoStack ?: return
            val attackSpeedLevel = getItemAttackSpeedLevel(newStack) ?: return
            setCooldown(user, newStack)
            sendEffect(player, attackSpeedLevel)
        }
    }

    private fun getItemAttackSpeedLevel(stack: NekoStack): AttackSpeedLevel? {
        return stack.components.get(ItemComponentTypes.ATTACK_SPEED)?.level
    }

    private fun setCooldown(user: User<*>, stack: NekoStack) {
        val attackSpeedLevel = getItemAttackSpeedLevel(stack) ?: return
        user.attackSpeed.setCooldown(stack.key, attackSpeedLevel)
    }

    private fun sendEffect(player: Player, level: AttackSpeedLevel) {
        val fatigueLevel = level.fatigueLevel ?: return
        val miningFatigueEffect = PotionEffect(
            PotionEffectType.MINING_FATIGUE,
            -1,
            fatigueLevel,
            false,
            false,
            false
        )

        SendPacketUtil.sendPlayerPotionEffect(player, miningFatigueEffect)
    }

    private fun removeEffect(player: Player) {
        SendPacketUtil.removePlayerPotionEffect(player, PotionEffectType.MINING_FATIGUE)
    }
}