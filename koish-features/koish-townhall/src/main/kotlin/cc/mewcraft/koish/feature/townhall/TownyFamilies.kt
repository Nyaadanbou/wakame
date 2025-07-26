package cc.mewcraft.koish.feature.townhall

import cc.mewcraft.koish.feature.townhall.component.EnhancementType
import cc.mewcraft.koish.feature.townhall.component.TownHall
import cc.mewcraft.wakame.ecs.Families
import cc.mewcraft.wakame.ecs.bridge.EWorld

object TownyFamilies : Families {
    @JvmField
    val TOWN_HALL = EWorld.family { all(TownHall) }

    @JvmField
    val BUFF_FURNACE = EWorld.family { all(EnhancementType.BUFF_FURNACE) }
}