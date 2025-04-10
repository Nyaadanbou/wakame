package cc.mewcraft.wakame.enchantment2.system

import cc.mewcraft.wakame.ecs.bridge.EWorld
import cc.mewcraft.wakame.ecs.component.BukkitObject
import cc.mewcraft.wakame.ecs.component.BukkitPlayer
import cc.mewcraft.wakame.enchantment2.getListenerBasedEffects
import cc.mewcraft.wakame.enchantment2.koishEnchantments
import cc.mewcraft.wakame.item2.ItemSlotChanges
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem

/**
 * 读取 [net.minecraft.world.item.enchantment.Enchantment.effects],
 * 然后从中创建对应的 ecs component, 并添加到 ecs entity (player) 上.
 */
object EnchantmentEffectApplier : IteratingSystem(
    family = EWorld.family { all(BukkitObject, BukkitPlayer, ItemSlotChanges) }
) {

    override fun onTickEntity(entity: Entity) {
        val player = entity[BukkitPlayer].unwrap()
        val slotChanges = entity[ItemSlotChanges]
        slotChanges.forEachChangingEntry { slot, curr, prev ->

            if (prev != null && ItemSlotChanges.testSlot(slot, prev)) {
                prev.koishEnchantments.forEach { (enchant, level) ->
                    enchant.getListenerBasedEffects().forEach { effect ->
                        effect.remove(entity, level, slot)
                    }
                }
            }

            if (curr != null &&
                ItemSlotChanges.testSlot(slot, curr) &&
                ItemSlotChanges.testLevel(player, curr) &&
                ItemSlotChanges.testDurability(curr)
            ) {
                curr.koishEnchantments.forEach { (enchant, level) ->
                    enchant.getListenerBasedEffects().forEach { effect ->
                        effect.apply(entity, level, slot)
                    }
                }
            }
        }
    }

}