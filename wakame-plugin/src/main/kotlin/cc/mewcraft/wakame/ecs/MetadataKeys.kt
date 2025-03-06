package cc.mewcraft.wakame.ecs

import cc.mewcraft.wakame.ecs.external.KoishEntity
import cc.mewcraft.wakame.util.metadata.MetadataKey

object MetadataKeys {
    val ECS_BUKKIT_PLAYER_ENTITY_ID = MetadataKey.create("ecs_bukkit_player_entity_id", KoishEntity::class.java)
    val ECS_BUKKIT_BLOCK_ENTITY_ID = MetadataKey.create("ecs_bukkit_block_entity_id", KoishEntity::class.java)
    val ECS_BUKKIT_ENTITY_ENTITY_ID = MetadataKey.create("ecs_bukkit_entity_entity_id", KoishEntity::class.java)
}