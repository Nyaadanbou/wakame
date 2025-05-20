package cc.mewcraft.wakame.item2.extension

import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.EquipmentSlot

/**
 * 该函数会特殊处理自定义攻击特效与原版损耗机制之间的交互,
 * 以便修复“重复”损耗物品的问题. 因此, 几乎在所有情况下,
 * 程序员应该使用这个函数来增加物品的损耗.
 *
 * @param slot the slot of the stack to damage
 * @param amount the amount of damage to do
 * @see LivingEntity.damageItemStack
 */
fun LivingEntity.damageItem(slot: EquipmentSlot, amount: Int) {
    // 执行 Paper 的逻辑
    damageItemStack(slot, amount)
}