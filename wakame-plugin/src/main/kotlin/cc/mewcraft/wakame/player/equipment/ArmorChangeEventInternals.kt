package cc.mewcraft.wakame.player.equipment

import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.util.event
import cc.mewcraft.wakame.util.giveItemStack
import org.bukkit.event.EventPriority

/**
 * 用于实现完整的 [ArmorChangeEvent].
 *
 * 当 [ArmorChangeEvent] 事件被取消时, 会将当前物品退还给玩家.
 * 如果不这么做, 物品会在事件被取消时直接消失, 而不是简单的不发生.
 */
@Init(
    stage = InitStage.POST_WORLD
)
internal object ArmorChangeEventInternals {

    @InitFun
    fun init() {
        event<ArmorChangeEvent>(EventPriority.MONITOR) { event ->
            if (!event.isCancelled)
                return@event
            if (event.action == ArmorChangeEvent.Action.UNEQUIP)
                return@event

            val slot = event.slot
            val player = event.player
            val current = event.current!!

            // 某些代码会直接设置盔甲, 例如原版的 /minecraft:item modify 指令.
            // 这里再次设置为 null 以确保直接设置的物品不会驻留在玩家身上.
            player.equipment.setItem(slot, null)

            player.giveItemStack(current)
        }
    }

}