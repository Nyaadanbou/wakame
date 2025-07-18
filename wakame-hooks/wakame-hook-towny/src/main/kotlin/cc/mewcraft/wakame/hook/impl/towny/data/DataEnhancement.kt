package cc.mewcraft.wakame.hook.impl.towny.data

import cc.mewcraft.wakame.ecs.bridge.KoishEntity
import cc.mewcraft.wakame.ecs.get
import cc.mewcraft.wakame.hook.impl.towny.component.Level
import cc.mewcraft.wakame.hook.impl.towny.component.TownEnhancementType
import cc.mewcraft.wakame.hook.impl.towny.component.TownHall
import com.github.quillraven.fleks.EntityComponentContext

/**
 * 一个只读的城镇增强数据类.
 */
data class DataEnhancement(
    val type: TownEnhancementType,
    val level: Int,
) {
    companion object {
        context(_: EntityComponentContext)
        fun fromTownHallEntity(entity: KoishEntity): Set<DataEnhancement> {
            return entity[TownHall].enhancements
                .map { (type, entity) ->
                    DataEnhancement(
                        type = type,
                        level = entity[Level].level
                    )
                }.toSet()
        }
    }
}
