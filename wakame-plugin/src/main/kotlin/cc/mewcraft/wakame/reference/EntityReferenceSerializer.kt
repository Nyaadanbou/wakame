package cc.mewcraft.wakame.reference

import cc.mewcraft.wakame.SchemeSerializer
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type

class EntityReferenceSerializer : SchemeSerializer<EntityReference> {
    override fun deserialize(type: Type, node: ConfigurationNode): EntityReference {
        TODO("Not yet implemented")
    }
}