package cc.mewcraft.wakame.item2.behavior.system

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.ecs.component.BukkitObject
import cc.mewcraft.wakame.ecs.component.BukkitPlayer
import cc.mewcraft.wakame.item2.ItemSlotChanges
import cc.mewcraft.wakame.item2.data.ItemDataTypes
import cc.mewcraft.wakame.item2.getData
import cc.mewcraft.wakame.kizami2.KizamiMap
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World

/**
 * 使物品上的铭刻对玩家生效.
 */
object ApplyKizamiEffect : IteratingSystem(
    family = World.family { all(BukkitObject, BukkitPlayer, ItemSlotChanges, KizamiMap) }
) {
    override fun onTickEntity(entity: Entity) {
        val player = entity[BukkitPlayer].unwrap()
        val slotChanges = entity[ItemSlotChanges]
        val kizamiContainer = entity[KizamiMap]

        // 更新铭刻的数量
        var changed = false
        for ((slot, curr, prev) in slotChanges.changingItems) {
            if (prev != null && ItemSlotChanges.testSlot(slot, prev)) {
                val kizami = prev.getData(ItemDataTypes.KIZAMI)
                if (kizami != null) {
                    changed = true
                    LOGGER.info("Decrementing kizami count from ${slot.id}")
                    kizamiContainer.subtractOneEach(kizami)
                }
            }
            if (curr != null &&
                ItemSlotChanges.testSlot(slot, curr) &&
                ItemSlotChanges.testLevel(player, curr) &&
                ItemSlotChanges.testDurability(curr)
            ) {
                val kizami = curr.getData(ItemDataTypes.KIZAMI)
                if (kizami != null) {
                    changed = true
                    LOGGER.info("Incrementing kizami count from ${slot.id}")
                    kizamiContainer.addOneEach(kizami)
                }
            }
        }

        // 更新铭刻的效果
        if (changed) {
            LOGGER.info("Updating kizami effects for ${player.name}")
        }
    }
}