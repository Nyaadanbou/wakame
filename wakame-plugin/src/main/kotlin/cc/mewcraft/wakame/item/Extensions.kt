package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.user.toUser
import cc.mewcraft.wakame.util.*
import org.bukkit.entity.Player

fun NekoStack.applyAttackCooldown(player: Player) {
    val itemAttackSpeed = this.components.get(ItemComponentTypes.ATTACK_SPEED) ?: return
    val attackSpeedLevel = itemAttackSpeed.level
    val user = player.toUser()
    // 设置实际冷却
    user.attackSpeed.activate(this.id, attackSpeedLevel)
    // 应用视觉冷却 (即末影珍珠使用后的白色覆盖层特效)
    player.setCooldown(this.itemType, attackSpeedLevel.cooldown)
}

var NekoStack.damage: Int
    get() = wrapped.damage
    set(value) {
        wrapped.damage = value
    }

val NekoStack.maxDamage: Int
    get() = wrapped.maxDamage

val NekoStack.isDamageable: Boolean
    get() = wrapped.isDamageable

/**
 * 使用该方法使物品失去最后一点耐久时, 不会有损坏动画效果.
 */
fun NekoStack.hurtAndBreak(player: Player, amount: Int) {
    wrapped.damage(amount, player)
}
