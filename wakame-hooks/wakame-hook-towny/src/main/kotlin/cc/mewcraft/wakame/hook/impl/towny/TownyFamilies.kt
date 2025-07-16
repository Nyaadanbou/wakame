package cc.mewcraft.wakame.hook.impl.towny

import cc.mewcraft.wakame.ecs.Families
import cc.mewcraft.wakame.ecs.bridge.EWorld
import cc.mewcraft.wakame.hook.impl.towny.component.TownHall

object TownyFamilies : Families {
    @JvmField
    val TOWN_HALL = EWorld.family { all(TownHall) }
}