package cc.mewcraft.koish.feature.townhall

import cc.mewcraft.koish.feature.townhall.bridge.koishify
import cc.mewcraft.koish.feature.townhall.enhancement.TownEnhancementsMenu
import cc.mewcraft.koish.feature.townhall.system.BuffFurnace
import cc.mewcraft.koish.feature.townhall.system.RemoveTownHall
import cc.mewcraft.koish.feature.townhall.util.TOWNY
import cc.mewcraft.wakame.KoishPlugin
import cc.mewcraft.wakame.ecs.FleksPatcher
import cc.mewcraft.wakame.ecs.bridge.KoishEntity
import cc.mewcraft.wakame.feature.Feature
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.util.registerEvents
import org.bukkit.block.Block
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerSwapHandItemsEvent

@Init(stage = InitStage.PRE_FLEKS)
object TownHallFeature : Feature(), FleksPatcher, Listener {

    internal fun getTownHallEntity(furnace: Block): KoishEntity? {
        val town = TOWNY.getTownBlock(furnace.location)?.townOrNull ?: return null
        return town.koishify()
    }

    @InitFun
    fun init() {
        if (!KoishPlugin.isPluginPresent("Towny")) {
            return
        }
        addToRegistryFamily("towny_families") { TownyFamilies }

        addToRegistrySystem("buff_furnace") { BuffFurnace }
        addToRegistrySystem("remove_town_hall") { RemoveTownHall }
        // MongoTownDataStorage.init()

        registerEvents()
    }

    @EventHandler
    fun on(event: PlayerSwapHandItemsEvent) {
        val player = event.player
        if (!player.isSneaking) {
            return
        }
        event.isCancelled = true
        TownEnhancementsMenu(
            town = TOWNY.getTown(event.player) ?: return,
            viewer = event.player
        ).open()
    }

    override val namespace: String = "townhall"
}