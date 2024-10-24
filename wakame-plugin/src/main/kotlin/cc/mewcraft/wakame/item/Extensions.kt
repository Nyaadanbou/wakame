package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.user.toUser
import org.bukkit.EntityEffect
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerItemBreakEvent
import org.bukkit.event.player.PlayerItemDamageEvent

fun NekoStack.applyAttackCooldown(player: Player) {
    val itemAttackSpeed = this.components.get(ItemComponentTypes.ATTACK_SPEED) ?: return
    val attackSpeedLevel = itemAttackSpeed.level
    val user = player.toUser()
    // 设置实际冷却
    user.attackSpeed.activate(this.id, attackSpeedLevel)
    // 应用视觉冷却 (即末影珍珠使用后的白色覆盖层特效)
    player.setCooldown(this.itemType, attackSpeedLevel.cooldown)
}

fun NekoStack.decreaseDurability(player: Player, amount: Int) {
    require(amount >= 0) { "The reduce durability can't less than 0." }

    val unbreakable = this.components.get(ItemComponentTypes.UNBREAKABLE)
    // 无法破坏的物品不能损失耐久
    if (unbreakable != null) return

    // 耐久组件不全的物品不做处理
    val maxDamage = this.components.get(ItemComponentTypes.MAX_DAMAGE) ?: return
    val damage = this.components.get(ItemComponentTypes.DAMAGE) ?: return

    // TODO 考虑耐久附魔
    val amountAfterEnchantment = amount

    val playerItemDamageEvent = PlayerItemDamageEvent(player, this.itemStack, amountAfterEnchantment, amount)
    // 如果掉耐久事件被取消则不处理
    if (!playerItemDamageEvent.callEvent()) return

    // 物品要损坏了
    // 保持物品最后一点耐久的行为在其handle中处理, 这里不需要考虑
    if (damage + playerItemDamageEvent.damage + 1 >= maxDamage) {
        PlayerItemBreakEvent(player, this.itemStack.clone()).callEvent()
        player.playEffect(EntityEffect.BREAK_EQUIPMENT_MAIN_HAND)
        this.itemStack.amount = 0
    } else {
        this.components.set(ItemComponentTypes.DAMAGE, damage + playerItemDamageEvent.damage)
    }
}