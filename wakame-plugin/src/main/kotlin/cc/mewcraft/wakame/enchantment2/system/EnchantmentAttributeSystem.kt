package cc.mewcraft.wakame.enchantment2.system

import cc.mewcraft.wakame.ecs.component.BukkitObject
import cc.mewcraft.wakame.ecs.component.BukkitPlayerComponent
import cc.mewcraft.wakame.enchantment2.getEffectList
import cc.mewcraft.wakame.enchantment2.koishEnchantments
import cc.mewcraft.wakame.item.logic.ItemSlotChanges
import cc.mewcraft.wakame.item.wrap
import cc.mewcraft.wakame.mixin.support.ExtraEnchantmentEffectComponents
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World

/**
 * @see cc.mewcraft.wakame.enchantment2.effect.EnchantmentAttributeEffect
 */
object EnchantmentAttributeSystem : IteratingSystem(
    family = World.family { all(BukkitObject, BukkitPlayerComponent, ItemSlotChanges) }
) {

    override fun onTickEntity(entity: Entity) {
        val bukkitPlayer = entity[BukkitPlayerComponent].bukkitPlayer
        val slotChanges = entity[ItemSlotChanges]
        for ((slot, prev, curr, _) in slotChanges.changingItems) {

            // 返回的 slot 一定都是在当前 tick 发生了变化的,
            // 因此这个 if-block 不可能执行, 写在这里仅供参考
            if (prev == null && curr == null) {
                continue
            }

            // 对于 prev (变化之前的物品), 我们需要从玩家身上 <移除> 其属性修饰器
            if (prev != null && ItemSlotChanges.testSlot(slot, prev.wrap())) {
                prev.koishEnchantments.forEach { (enchant, level) ->
                    enchant.getEffectList(ExtraEnchantmentEffectComponents.ATTRIBUTES).forEach { attribute ->
                        attribute.remove(bukkitPlayer, level, slot)
                    }
                }
            }

            // 对于 curr (变化之后的物品), 我们需要从玩家身上 <添加> 其属性修饰器
            if (curr != null &&
                ItemSlotChanges.testSlot(slot, curr.wrap()) &&
                ItemSlotChanges.testLevel(bukkitPlayer, curr.wrap()) &&
                ItemSlotChanges.testDurability(curr)
            ) {
                curr.koishEnchantments.forEach { (enchant, level) ->
                    enchant.getEffectList(ExtraEnchantmentEffectComponents.ATTRIBUTES).forEach { attribute ->
                        attribute.apply(bukkitPlayer, level, slot)
                    }
                }
            }

        }
    }

}