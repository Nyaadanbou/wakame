package cc.mewcraft.wakame.ecs

import cc.mewcraft.wakame.ecs.external.ComponentBridge
import cc.mewcraft.wakame.util.metadata.MetadataKey

object MetadataKeys {
    val PLAYER_ENTITY = MetadataKey.create("player_entity", ComponentBridge::class.java)
    val BLOCK_ENTITY = MetadataKey.create("block_entity", ComponentBridge::class.java)
    val BUKKIT_ENTITY_ENTITY = MetadataKey.create("bukkit_entity_entity", ComponentBridge::class.java)
}