package cc.mewcraft.wakame.item.feature

import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.util.registerEvents
import org.bukkit.event.Listener

@Init(stage = InitStage.POST_WORLD)
object CastableFeature : Listener {

    init {
        registerEvents()
    }
}