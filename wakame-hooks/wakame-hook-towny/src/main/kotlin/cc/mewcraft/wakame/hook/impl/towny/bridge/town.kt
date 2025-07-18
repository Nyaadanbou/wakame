package cc.mewcraft.wakame.hook.impl.towny.bridge

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.ecs.Fleks
import cc.mewcraft.wakame.ecs.bridge.KoishEntity
import cc.mewcraft.wakame.hook.impl.towny.TownMetadataKeys
import cc.mewcraft.wakame.hook.impl.towny.TownMetadataRegistry
import cc.mewcraft.wakame.hook.impl.towny.component.Level
import cc.mewcraft.wakame.hook.impl.towny.component.TownHall
import cc.mewcraft.wakame.hook.impl.towny.data.DummyTownDataStorage
import com.palmergames.bukkit.towny.`object`.Town

fun Town.koishify(): KoishEntity {
    val town = this
    val metadataMap = TownMetadataRegistry.provide(town)
    val koishEntity = metadataMap.getOrPut(TownMetadataKeys.ECS_TOWNY_TOWN_ENTITY_ID) {
        val dataTownHall = DummyTownDataStorage.loadTownHall(town.uuid)
        KoishEntity(
            Fleks.INSTANCE.createEntity { townHall ->
                townHall += TownHall(
                    townUUID = this@koishify.uuid,
                    enhancements = dataTownHall.enhancements.associateTo(HashMap()) { dataEnhancement ->
                        dataEnhancement.type to Fleks.INSTANCE.createEntity {
                            it += dataEnhancement.type
                            it += Level(dataEnhancement.level)
                        }
                    },
                    storage = Fleks.INSTANCE.createEntity {
                    }
                )
            }.also {
                // Log the creation of the KoishEntity for the Town
                LOGGER.info("[ECS] $it created for ${town.name} (${town.uuid})")
            }
        )
    }
    return koishEntity
}