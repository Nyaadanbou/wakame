package cc.mewcraft.wakame.ability2.system

import cc.mewcraft.wakame.ability2.AbilityCastUtils
import cc.mewcraft.wakame.ecs.component.BukkitObject
import cc.mewcraft.wakame.ecs.component.BukkitPlayer
import cc.mewcraft.wakame.entity.player.component.InventoryListenable
import cc.mewcraft.wakame.item2.ItemSlotChanges
import cc.mewcraft.wakame.item2.config.property.ItemPropertyTypes
import cc.mewcraft.wakame.item2.getProperty
import cc.mewcraft.wakame.user.combo
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World

class AbilityAddSystem : IteratingSystem(
    family = World.family { all(BukkitObject, BukkitPlayer, InventoryListenable) }
) {
    override fun onTickEntity(entity: Entity) {
        val player = entity[BukkitPlayer].unwrap()
        val slotChanges = entity[ItemSlotChanges]
        for ((slot, curr) in slotChanges.changingItems) {
            if (curr != null &&
                ItemSlotChanges.testSlot(slot, curr) &&
                ItemSlotChanges.testLevel(player, curr) &&
                ItemSlotChanges.testDurability(curr)
            ) {
                val ability = curr.getProperty(ItemPropertyTypes.ABILITY) ?: continue
                AbilityCastUtils.idle(ability, player, player, slot)
            }
        }

        player.combo.reset()
    }
}