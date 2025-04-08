package cc.mewcraft.wakame.item.logic

import cc.mewcraft.wakame.ecs.Families
import cc.mewcraft.wakame.ecs.component.BukkitPlayer
import cc.mewcraft.wakame.entity.player.component.InventoryListenable
import cc.mewcraft.wakame.item.ItemSlotRegistry
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.FamilyOnAdd
import com.github.quillraven.fleks.IteratingSystem

/**
 * 该类属于事件 [cc.mewcraft.wakame.event.bukkit.PlayerItemSlotChangeEvent] 内部完整实现的一部分.
 *
 * 该类扫描玩家背包的变化, 按情况触发 [cc.mewcraft.wakame.event.bukkit.PlayerItemSlotChangeEvent].
 *
 * 具体来说:
 * 我们每 tick 扫描每个玩家背包里的特定槽位上的物品.
 * 如果第 `n` tick 扫描的结果和第 `n-1` tick 扫描的结果不同,
 * 则认为这个槽位发生了变化, 那么此时就会记录并触发一个事件.
 */
class ItemSlotChangeMonitor : IteratingSystem(
    family = Families.BUKKIT_PLAYER
), FamilyOnAdd {
    override fun onTickEntity(entity: Entity) {
        if (!entity.has(InventoryListenable)) {
            // 当玩家的背包不可监听时, 跳过扫描, 跳过触发事件.
            // 换句话说, 在 isInventoryListenable 为 false 时,
            // ItemSlotChanges 永远不会更新, 并且 isEmpty 为 true.
            // 直到当 isInventoryListenable 为 true 时,
            // ItemSlotChanges 才会开始更新.
            return
        }

        val player = entity[BukkitPlayer].unwrap()
        for (slot in ItemSlotRegistry.all()) {
            val curr = slot.getItem(player)
            val entry = entity[ItemSlotChanges][slot]
            entry.update(curr)
            if (entry.changing) {
                //PlayerItemSlotChangeEvent(player, slot, entry.previous, entry.current).callEvent()
            }
        }
    }

    override fun onAddEntity(entity: Entity) {
        entity.configure { it += ItemSlotChanges() }
    }
}