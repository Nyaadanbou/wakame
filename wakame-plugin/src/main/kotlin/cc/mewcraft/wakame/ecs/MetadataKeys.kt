package cc.mewcraft.wakame.ecs

import cc.mewcraft.wakame.ecs.external.ComponentBridge
import cc.mewcraft.wakame.util.metadata.MetadataKey

object MetadataKeys {
    val ABILITY: MetadataKey<ComponentBridge> = MetadataKey.create("ability", ComponentBridge::class.java)
    val ELEMENT_STACK: MetadataKey<ComponentBridge> = MetadataKey.create("element_stack", ComponentBridge::class.java)
}