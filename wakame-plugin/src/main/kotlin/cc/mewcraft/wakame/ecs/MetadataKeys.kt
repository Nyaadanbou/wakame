package cc.mewcraft.wakame.ecs

import cc.mewcraft.wakame.ecs.external.ComponentMap
import me.lucko.helper.metadata.MetadataKey

object MetadataKeys {
    val ABILITY: MetadataKey<ComponentMap> = MetadataKey.create("ability", ComponentMap::class.java)
    val ELEMENT_STACK: MetadataKey<ComponentMap> = MetadataKey.create("element_stack", ComponentMap::class.java)
}