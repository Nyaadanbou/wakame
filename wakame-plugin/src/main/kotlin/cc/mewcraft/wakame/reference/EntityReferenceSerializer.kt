package cc.mewcraft.wakame.reference

import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.TypeSerializer
import java.lang.reflect.Type

class EntityReferenceSerializer : TypeSerializer<EntityReference> {
    override fun deserialize(type: Type, node: ConfigurationNode): EntityReference {
        TODO("Not yet implemented")
    }

    override fun serialize(type: Type, obj: EntityReference?, node: ConfigurationNode) {
        TODO("Not yet implemented")
    }
}