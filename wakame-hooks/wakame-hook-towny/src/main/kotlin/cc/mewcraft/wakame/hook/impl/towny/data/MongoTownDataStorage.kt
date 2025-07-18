package cc.mewcraft.wakame.hook.impl.towny.data

import cc.mewcraft.wakame.database.MongoDataStorage
import cc.mewcraft.wakame.ecs.get
import cc.mewcraft.wakame.hook.impl.towny.TownyFamilies
import cc.mewcraft.wakame.hook.impl.towny.component.Level
import cc.mewcraft.wakame.hook.impl.towny.component.TownHall
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.EntityComponentContext
import com.mongodb.client.model.Filters
import com.mongodb.client.model.ReplaceOptions
import com.mongodb.kotlin.client.coroutine.MongoCollection
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.firstOrNull
import java.util.*

internal object MongoTownDataStorage {
    private const val COLLECTION_NAME = "town.halls"

    private lateinit var townHallCollection: MongoCollection<DataTownHall>

    fun init() {
        val database: MongoDatabase = MongoDataStorage.database()
        townHallCollection = database.getCollection(COLLECTION_NAME)
    }

    /**
     * 从数据库中加载市政厅, 如果不存在则会创建默认数据并插入到表中.
     *
     * @return 返回市政厅数据.
     */
    suspend fun loadTownHall(townUUID: UUID): DataTownHall {
        return townHallCollection.find(Filters.eq(townUUID)).firstOrNull() ?: run {
            val defaultData = DataTownHall(
                townUUID = townUUID,
                enhancements = emptyList(),
            )
            townHallCollection.insertOne(defaultData)
            defaultData
        }
    }

    context(_: EntityComponentContext)
    suspend fun saveTownHall(entity: Entity) {
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
            enhancements = enhancements
        )
        townHallCollection.replaceOne(Filters.eq(townUUID), dataTownHall, ReplaceOptions().upsert(true))
    }

    /**
     * 将 ECS 世界内的所有市政厅 entity 数据保存到数据库中.
     */
    suspend fun saveAllTownHall() {
        val dataTownHalls = mutableListOf<DataTownHall>()
        TownyFamilies.TOWN_HALL.forEach { townHallEntity ->
            val townHall = townHallEntity[TownHall]
            val townUUID = townHall.townUUID
            val enhancements = townHall.enhancements.map { (type, enhancementEntity) ->
                DataEnhancement(
                    type = type,
                    level = enhancementEntity[Level].level
                )
            }
            val dataTownHall = DataTownHall(
                townUUID = townUUID,
                enhancements = enhancements
            )
            dataTownHalls.add(dataTownHall)
        }
    }
}