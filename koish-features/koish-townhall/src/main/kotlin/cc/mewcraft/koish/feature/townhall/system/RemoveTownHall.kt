package cc.mewcraft.koish.feature.townhall.system

import cc.mewcraft.koish.feature.townhall.TownyFamilies
import cc.mewcraft.koish.feature.townhall.component.TownHall
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem

object RemoveTownHall : IteratingSystem(
    family = TownyFamilies.TOWN_HALL
) {
    override fun onTickEntity(entity: Entity) {
        val townHall = entity[TownHall]
        val isFullyLoaded = townHall.town.homeBlock.worldCoord.isFullyLoaded
        if (!isFullyLoaded) {
            // If the town hall is not fully loaded, remove the entity
            townHall.storage.remove()
            entity.remove()
        }
    }
}