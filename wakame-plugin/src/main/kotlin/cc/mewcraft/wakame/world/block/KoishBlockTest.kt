package cc.mewcraft.wakame.world.block

import cc.mewcraft.wakame.event.bukkit.PlayerItemRightClickEvent
import cc.mewcraft.wakame.item2.config.property.ItemPropertyTypes
import cc.mewcraft.wakame.item2.getProperty
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.util.item.takeUnlessEmpty
import cc.mewcraft.wakame.util.registerEvents
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

@Init(stage = InitStage.POST_WORLD)
object KoishBlockTest : Listener {

    @InitFun
    fun init() {
        registerEvents()
    }

    @EventHandler
    fun on(event: PlayerItemRightClickEvent) {
        val player = event.player
        val itemInMainHand = player.inventory.itemInMainHand.takeUnlessEmpty() ?: return
        val koishBlock = itemInMainHand.getProperty(ItemPropertyTypes.BLOCK)?.unwrap() ?: return
        player.sendMessage(koishBlock.test)
    }

}