package cc.mewcraft.wakame.hook.impl.towny.system

import cc.mewcraft.wakame.hook.impl.towny.TownyFamilies
import cc.mewcraft.wakame.hook.impl.towny.component.TownHall
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
            townHall.enhancements.forEach { it.value.remove() }
            entity.remove()
        }
    }
}