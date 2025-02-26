package cc.mewcraft.wakame.item.extension

import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.player.itemdamage.ItemDamageEventMarker
import cc.mewcraft.wakame.user.toUser
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot

/**
 * 使物品的攻击进入冷却.
 */
// TODO 让视觉冷却仅应用于萌芽物品类型, 而非 Minecraft 物品类型.
//  升级 1.21.2 后使用 use_cooldown 组件即可解决这个问题.
fun NekoStack.applyAttackCooldown(player: Player) {
    val itemAttackSpeed = this.components.get(ItemComponentTypes.ATTACK_SPEED) ?: return
    val attackSpeedLevel = itemAttackSpeed.level
    val user = player.toUser()
    // 设置实际冷却
    user.attackSpeed.activate(this.id, attackSpeedLevel)
    // 应用视觉冷却 (即末影珍珠使用后的白色覆盖层特效)
    player.setCooldown(this.itemType, attackSpeedLevel.cooldown)
}


/**
 * 使用该方法使物品失去最后一点耐久时, 不会有损坏动画效果.
 */
fun NekoStack.hurtAndBreak(player: Player, amount: Int) {
    this.bukkitStack.damage(amount, player)
}

/**
 * Damages the itemstack in this slot by the specified amount.
 *
 * This runs all logic associated with damaging an itemstack like
 * gamemode and enchantment checks, events, stat changes, advancement triggers,
 * and notifying clients to play break animations.
 *
 * 该函数会特殊处理自定义攻击特效与原版损耗机制之间的交互,
 * 以便修复“重复”损耗物品的问题. 因此, 几乎在所有情况下,
 * 程序员应该使用这个函数来增加物品的损耗.
 *
 * @param slot the slot of the stack to damage
 * @param amount the amount of damage to do
 */
fun Player.damageItemStack2(slot: EquipmentSlot, amount: Int) {
    // 执行 Paper 的逻辑
    damageItemStack(slot, amount)
    // 正确处理此次的损耗
    ItemDamageEventMarker.markAlreadyDamaged(this)
}
