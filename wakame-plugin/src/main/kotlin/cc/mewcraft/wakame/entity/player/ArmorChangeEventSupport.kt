package cc.mewcraft.wakame.entity.player

import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.player.equipment.ArmorChangeEvent
import cc.mewcraft.wakame.util.giveItemStack
import cc.mewcraft.wakame.util.registerEvents
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

/**
 * 用于实现 [cc.mewcraft.wakame.player.equipment.ArmorChangeEvent].
 */
@Init(stage = InitStage.POST_WORLD)
internal object ArmorChangeEventSupport : Listener {

    @InitFun
    fun init() {
        registerEvents()
    }

    // 当 ArmorChangeEvent 事件被取消时, 将当前物品退还给玩家.
    // 否则物品会在事件被取消时 *直接消失*, 而非当前的操作被取消.
    @EventHandler
    fun on(event: ArmorChangeEvent) {
        if (event.isCancelled)
            return
        if (event.action == ArmorChangeEvent.Action.UNEQUIP)
            return

        val player = event.player
        val current = event.current!! // if this is null then this is a bug

        // 某些代码会直接设置盔甲, 例如原版的 /minecraft:item modify 指令.
        // 这里再次设置为 null 以确保直接设置的物品不会驻留在玩家身上.
        player.equipment.setItem(event.slot, null)

        player.giveItemStack(current)
    }

}