package cc.mewcraft.wakame.hook.impl.towny

import cc.mewcraft.wakame.ecs.Families
import cc.mewcraft.wakame.ecs.bridge.EWorld
import cc.mewcraft.wakame.hook.impl.towny.component.Level
import cc.mewcraft.wakame.hook.impl.towny.component.TownEnhancementType
import cc.mewcraft.wakame.hook.impl.towny.component.TownHall

object TownyFamilies : Families {
    @JvmField
    val TOWN_HALL = EWorld.family { all(TownHall) }

    @JvmField
    val BUFF_FURNACE = EWorld.family { all(TownEnhancementType.BUFF_FURNACE, Level) }
}