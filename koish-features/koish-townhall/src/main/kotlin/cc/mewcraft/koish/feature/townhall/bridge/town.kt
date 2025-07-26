package cc.mewcraft.koish.feature.townhall.bridge

import cc.mewcraft.koish.feature.townhall.TownMetadataKeys
import cc.mewcraft.koish.feature.townhall.TownMetadataRegistry
import cc.mewcraft.koish.feature.townhall.component.TownHall
import cc.mewcraft.koish.feature.townhall.data.DummyTownDataStorage
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.ecs.Fleks
import cc.mewcraft.wakame.ecs.bridge.KoishEntity
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
                    enhancements = dataTownHall.enhancements.toMutableList(),
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