package cc.mewcraft.wakame.ability.system

import cc.mewcraft.wakame.ability.AbilityCastUtils
import cc.mewcraft.wakame.ecs.bridge.EWorld
import cc.mewcraft.wakame.ecs.component.BukkitObject
import cc.mewcraft.wakame.ecs.component.BukkitPlayer
import cc.mewcraft.wakame.entity.player.component.InventoryListenable
import cc.mewcraft.wakame.item.ItemSlotChanges
import cc.mewcraft.wakame.item.getProperty
import cc.mewcraft.wakame.item.property.ItemPropertyTypes
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem

object AbilityActivator : IteratingSystem(
    family = EWorld.family { all(BukkitObject, BukkitPlayer, InventoryListenable) }
) {
    override fun onTickEntity(entity: Entity) {
        val player = entity[BukkitPlayer].unwrap()
        val slotChanges = entity[ItemSlotChanges]
        slotChanges.forEachChangingEntry { slot, curr, _ ->
            if (curr != null &&
                ItemSlotChanges.testSlot(slot, curr) &&
                ItemSlotChanges.testLevel(player, curr) &&
                ItemSlotChanges.testDurability(curr)
            ) {
                val ability = curr.getProperty(ItemPropertyTypes.ABILITY) ?: return@forEachChangingEntry
                AbilityCastUtils.idle(ability, player, player, slot)
            }
        }
    }
}