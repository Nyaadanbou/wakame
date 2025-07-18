package cc.mewcraft.wakame.hook.impl.towny.data

import cc.mewcraft.wakame.ecs.get
import cc.mewcraft.wakame.hook.impl.towny.TownyFamilies
import cc.mewcraft.wakame.hook.impl.towny.component.Level
import cc.mewcraft.wakame.hook.impl.towny.component.TownEnhancementType
import cc.mewcraft.wakame.hook.impl.towny.component.TownHall
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.EntityComponentContext
import java.util.*
import java.util.concurrent.ConcurrentHashMap

internal object DummyTownDataStorage {

    private val townHalls: ConcurrentHashMap<UUID, DataTownHall> = ConcurrentHashMap()

    /**
     * 从数据库中加载市政厅, 如果不存在则会创建默认数据并插入到表中.
     *
     * @return 返回市政厅数据.
     */
    fun loadTownHall(townUUID: UUID): DataTownHall {
        return townHalls.getOrPut(townUUID) {
            DataTownHall(
                townUUID = townUUID,
                enhancements = listOf(
                    DataEnhancement(
                        type = TownEnhancementType.BUFF_FURNACE,
                        level = 1 // 默认等级为 1
                    )
                ),
            )
        }
    }

    context(_: EntityComponentContext)
    fun saveTownHall(entity: Entity) {
        val townHall = entity[TownHall]
        val townUUID = townHall.townUUID
        val enhancements = townHall.enhancements.map { (type, enhancementEntity) ->
            DataEnhancement(
                type = type,
                level = enhancementEntity[Level].level
            )
        }
        val dataTownHall = DataTownHall(
            townUUID = townUUID,
            enhancements = enhancements,
        )
        townHalls[townUUID] = dataTownHall
    }

    /**
     * 将 ECS 世界内的所有市政厅 entity 数据保存到数据库中.
     */
    fun saveAllTownHall() {
        TownyFamilies.TOWN_HALL.forEach { townHall -> saveTownHall(townHall) }
    }
}