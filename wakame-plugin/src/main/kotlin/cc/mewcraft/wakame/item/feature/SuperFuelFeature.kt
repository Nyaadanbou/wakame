package cc.mewcraft.wakame.item.feature

import cc.mewcraft.wakame.item.getProp
import cc.mewcraft.wakame.item.property.ItemPropTypes
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.util.registerEvents
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.FurnaceBurnEvent

@Init(stage = InitStage.POST_WORLD)
object SuperFuelFeature : Listener {

    init {
        registerEvents()
    }

    @EventHandler(ignoreCancelled = true)
    private fun on(event: FurnaceBurnEvent) {
        val fuelItem = event.fuel
        val fuel = fuelItem.getProp(ItemPropTypes.FUEL) ?: return
        event.burnTime = fuel.burnTime
        event.setConsumeFuel(fuel.consume)
    }
}