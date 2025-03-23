package cc.mewcraft.wakame.enchantment2.system

import cc.mewcraft.wakame.ecs.component.BukkitObject
import cc.mewcraft.wakame.ecs.component.BukkitPlayerComponent
import cc.mewcraft.wakame.enchantment2.getListenerBasedEffects
import cc.mewcraft.wakame.enchantment2.koishEnchantments
import cc.mewcraft.wakame.item.logic.ItemSlotChanges
import cc.mewcraft.wakame.item.wrap
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World

/**
 * 读取 [net.minecraft.world.item.enchantment.Enchantment.effects],
 * 然后从中创建对应的 ecs component, 并添加到 ecs entity (player) 上.
 */
object EnchantmentEffectApplier : IteratingSystem(
    family = World.family { all(BukkitObject, BukkitPlayerComponent, ItemSlotChanges) }
) {

    override fun onTickEntity(entity: Entity) {
        val bukkitPlayer = entity[BukkitPlayerComponent].bukkitPlayer
        val slotChanges = entity[ItemSlotChanges]
        for ((slot, curr, prev, _) in slotChanges.changingItems) {

            if (prev != null && ItemSlotChanges.testSlot(slot, prev.wrap())) {
                prev.koishEnchantments.forEach { (enchant, level) ->
                    enchant.getListenerBasedEffects().forEach { effect ->
                        effect.remove(entity, level, slot)
                    }
                }
            }

            if (curr != null &&
                ItemSlotChanges.testSlot(slot, curr.wrap()) &&
                ItemSlotChanges.testLevel(bukkitPlayer, curr.wrap()) &&
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