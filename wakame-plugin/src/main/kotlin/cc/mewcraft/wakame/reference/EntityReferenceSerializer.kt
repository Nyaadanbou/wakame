package cc.mewcraft.wakame.reference

import cc.mewcraft.wakame.SchemaSerializer
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.getList
import java.lang.reflect.Type

class EntityReferenceSerializer : SchemaSerializer<EntityReference> {

    override fun deserialize(type: Type, node: ConfigurationNode): EntityReference {
        val name = node.key().toString()
        val keySet = node.getList<Key>(emptyList()).toSet()
        return ImmutableEntityReference(name, keySet)
    }

}