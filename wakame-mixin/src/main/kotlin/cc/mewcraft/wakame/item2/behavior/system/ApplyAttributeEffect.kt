package cc.mewcraft.wakame.item2.behavior.system

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.ecs.component.BukkitObject
import cc.mewcraft.wakame.ecs.component.BukkitPlayer
import cc.mewcraft.wakame.entity.attribute.AttributeMap
import cc.mewcraft.wakame.item2.ItemSlotChanges
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World

/**
 * 使物品上的属性对玩家生效.
 */
object ApplyAttributeEffect : IteratingSystem(
    family = World.family { all(BukkitObject, BukkitPlayer, ItemSlotChanges, AttributeMap) }
) {
    override fun onTickEntity(entity: Entity) {
        val player = entity[BukkitPlayer].unwrap()
        val slotChanges = entity[ItemSlotChanges]
        val attributeContainer = entity[AttributeMap]
        for ((slot, curr, prev) in slotChanges.changingItems) {
            if (prev != null && ItemSlotChanges.testSlot(slot, prev)) {
                // TODO #373: 移除属性
                LOGGER.info("Removing attribute modifier from ${slot.id}")
            }
            if (curr != null &&
                ItemSlotChanges.testSlot(slot, curr) &&
                ItemSlotChanges.testLevel(player, curr) &&
                ItemSlotChanges.testDurability(curr)
            ) {
                // TODO #373: 添加属性
                LOGGER.info("Adding attribute modifier from ${slot.id}")
            }
        }
    }
}