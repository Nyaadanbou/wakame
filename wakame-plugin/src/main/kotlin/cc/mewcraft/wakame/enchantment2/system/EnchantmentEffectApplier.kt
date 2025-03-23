package cc.mewcraft.wakame.enchantment2.system

import cc.mewcraft.wakame.ecs.component.BukkitObject
import cc.mewcraft.wakame.ecs.component.BukkitPlayerComponent
import cc.mewcraft.wakame.enchantment2.effects
import cc.mewcraft.wakame.enchantment2.koishEnchantments
import cc.mewcraft.wakame.enchantment2.metadata.EnchantmentMetaTypes
import cc.mewcraft.wakame.item.logic.ItemSlotChanges
import cc.mewcraft.wakame.item.wrap
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World

/**
 * 读取 [net.minecraft.world.item.enchantment.Enchantment.effects],
 * 然后从中创建对应的 [com.github.quillraven.fleks.Component]
 * 并添加到 Player [Entity] 上.
 */
object EnchantmentEffectApplier : IteratingSystem(
    family = World.family { all(BukkitObject, BukkitPlayerComponent, ItemSlotChanges) }
) {

    override fun onTickEntity(entity: Entity) {
        val bukkitPlayer = entity[BukkitPlayerComponent].bukkitPlayer
        val slotChanges = entity[ItemSlotChanges]
        slotChanges.changingItems.forEach { (slot, curr, prev, _) ->
            // 获取魔咒效果组件, 然后再获取对应的 EnchantmentMeta
            // 使用 EnchantmentMeta 获取对应的 fleks component 类型, 从玩家身上移除
            if (prev != null && ItemSlotChanges.testSlot(slot, prev.wrap())) {
                prev.koishEnchantments.forEach { (enchant, level) ->
                    enchant.effects.forEach { typedEffect ->
                        val type = typedEffect.type
                        EnchantmentMetaTypes.getMeta(type).remove(entity)
                    }
                }
            }

            if (curr != null &&
                ItemSlotChanges.testSlot(slot, curr.wrap()) &&
                ItemSlotChanges.testLevel(bukkitPlayer, curr.wrap()) &&
                ItemSlotChanges.testDurability(curr)
            ) {
                // 获取魔咒效果组件, 然后再获取对应的 EnchantmentMeta
                // 使用 EnchantmentMeta 创建 fleks component, 添加到玩家身上
                curr.koishEnchantments.forEach { (enchant, level) ->
                    enchant.effects.forEach { typedEffect ->
                        val type = typedEffect.type
                        val value = typedEffect.value
                        EnchantmentMetaTypes.getMeta(type).apply(entity, value)
                    }
                }
            }

        }
    }

}