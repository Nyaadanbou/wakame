package cc.mewcraft.wakame.item.feature

import cc.mewcraft.wakame.event.bukkit.PlayerItemSlotChangeEvent
import cc.mewcraft.wakame.item.ItemSlotChanges
import cc.mewcraft.wakame.item.getProp
import cc.mewcraft.wakame.item.property.ItemPropTypes
import cc.mewcraft.wakame.item.property.impl.SpecialCastableTrigger
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.util.registerEvents
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

// 开发日记 2025/11/24:
// 这里仅仅用来实现 castable 中的几个特殊触发器:
// - special/on_equip
// - special/on_unequip
@Init(stage = InitStage.POST_WORLD)
object CastableFeature : Listener {

    init {
        registerEvents()
    }

    @EventHandler
    fun on(event: PlayerItemSlotChangeEvent) {
        val player = event.player
        val slot = event.slot
        val prev = event.oldItemStack
        val curr = event.newItemStack
        if (prev != null &&
            ItemSlotChanges.testSlot(slot, prev)
        ) {
            val castables = prev.getProp(ItemPropTypes.CASTABLE)
            if (castables != null) {
                for (castable in castables.values) {
                    if (castable.trigger.unwrap() == SpecialCastableTrigger.ON_UNEQUIP) {
                        castable.skill.cast(player)
                    }
                }
            }
        }
        if (curr != null &&
            ItemSlotChanges.testSlot(slot, curr) &&
            ItemSlotChanges.testLevel(player, curr) &&
            ItemSlotChanges.testDurability(curr)
        ) {
            val castables = curr.getProp(ItemPropTypes.CASTABLE)
            if (castables != null) {
                for (castable in castables.values) {
                    if (castable.trigger.unwrap() == SpecialCastableTrigger.ON_EQUIP) {
                        castable.skill.cast(player)
                    }
                }
            }
        }
    }
}