package cc.mewcraft.wakame.item2.behavior.impl

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.ecs.bridge.EWorld
import cc.mewcraft.wakame.ecs.component.BukkitObject
import cc.mewcraft.wakame.ecs.component.BukkitPlayer
import cc.mewcraft.wakame.item2.ItemSlotChanges
import cc.mewcraft.wakame.item2.behavior.ItemBehavior
import cc.mewcraft.wakame.item2.data.ItemDataTypes
import cc.mewcraft.wakame.item2.getData
import cc.mewcraft.wakame.kizami2.KizamiMap
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem

/**
 * 使物品上的铭刻对玩家生效.
 */
object ApplyKizamiEffects : ItemBehavior, IteratingSystem(
    family = EWorld.family { all(BukkitObject, BukkitPlayer, ItemSlotChanges, KizamiMap) }
) {
    override fun onTickEntity(entity: Entity) {
        val player = entity[BukkitPlayer].unwrap()
        val slotChanges = entity[ItemSlotChanges]
        val liveKizamiContainer = entity[KizamiMap]

        // 优化: 保存变化前的铭刻状态, 用作之后铭刻效果的移除
        val prevKizamiContainer = liveKizamiContainer.copy()

        // 更新铭刻的数量
        var changed = false
        slotChanges.forEachChangingEntry { slot, curr, prev ->
            if (prev != null && ItemSlotChanges.testSlot(slot, prev)) {
                val kizami = prev.getData(ItemDataTypes.KIZAMI)
                if (kizami != null) {
                    LOGGER.info("Decrementing kizami count from ${slot.id}")
                    liveKizamiContainer.subtractOneEach(kizami)
                    changed = true
                }
            }
            if (curr != null &&
                ItemSlotChanges.testSlot(slot, curr) &&
                ItemSlotChanges.testLevel(player, curr) &&
                ItemSlotChanges.testDurability(curr)
            ) {
                val kizami = curr.getData(ItemDataTypes.KIZAMI)
                if (kizami != null) {
                    LOGGER.info("Incrementing kizami count from ${slot.id}")
                    liveKizamiContainer.addOneEach(kizami)
                    changed = true
                }
            }
        }

        // 更新铭刻的效果
        if (changed) {
            LOGGER.info("Updating kizami effects for ${player.name}")
            prevKizamiContainer.removeAllEffects(player)
            liveKizamiContainer.applyAllEffects(player)
        }
    }
}