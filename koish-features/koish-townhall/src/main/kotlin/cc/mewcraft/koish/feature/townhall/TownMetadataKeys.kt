package cc.mewcraft.koish.feature.townhall

import cc.mewcraft.wakame.ecs.bridge.KoishEntity
import cc.mewcraft.wakame.util.metadata.MetadataKey

object TownMetadataKeys {
    @JvmField
    val ECS_TOWNY_TOWN_ENTITY_ID = MetadataKey.create("ecs_towny_town_entity_id", KoishEntity::class.java)
}