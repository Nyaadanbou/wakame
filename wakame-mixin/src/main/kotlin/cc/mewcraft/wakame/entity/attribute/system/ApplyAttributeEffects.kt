package cc.mewcraft.wakame.entity.attribute.system

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.ecs.bridge.EWorld
import cc.mewcraft.wakame.ecs.component.BukkitObject
import cc.mewcraft.wakame.ecs.component.BukkitPlayer
import cc.mewcraft.wakame.entity.attribute.AttributeMap
import cc.mewcraft.wakame.item.ItemSlotChanges
import cc.mewcraft.wakame.item.data.ItemDataTypes
import cc.mewcraft.wakame.item.getData
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem

/**
 * 使物品上的属性对玩家生效.
 */
object ApplyAttributeEffects : IteratingSystem(
    family = EWorld.family { all(BukkitObject, BukkitPlayer, ItemSlotChanges, AttributeMap) }
) {
    override fun onTickEntity(entity: Entity) {
        val player = entity[BukkitPlayer].unwrap()
        val slotChanges = entity[ItemSlotChanges]
        val attributeContainer = entity[AttributeMap]

        // 更新属性的状态
        slotChanges.forEachChangingEntry { slot, curr, prev ->
            if (prev != null && ItemSlotChanges.testSlot(slot, prev)) {
                val coreContainer = prev.getData(ItemDataTypes.CORE_CONTAINER)
                if (coreContainer != null) {
                    LOGGER.info("Removing attribute modifier from ${slot.id}")
                    val attrModifiers = coreContainer.collectAttributeModifiers(prev, slot)
                    attributeContainer.removeModifiers(attrModifiers)
                }
            }
            if (curr != null &&
                ItemSlotChanges.testSlot(slot, curr) &&
                ItemSlotChanges.testLevel(player, curr) &&
                ItemSlotChanges.testDurability(curr)
            ) {
                val coreContainer = curr.getData(ItemDataTypes.CORE_CONTAINER)
                if (coreContainer != null) {
                    LOGGER.info("Adding attribute modifier from ${slot.id}")
                    val attrModifiers = coreContainer.collectAttributeModifiers(curr, slot)
                    attributeContainer.addTransientModifiers(attrModifiers)
                }
            }
        }
    }
}