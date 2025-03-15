package cc.mewcraft.wakame.serialization.configurate.typeserializer

import cc.mewcraft.wakame.config.configurate.TypeSerializer
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type

/*internal*/ object UnitSerializer : TypeSerializer<Unit> {
    override fun deserialize(type: Type, node: ConfigurationNode) {
        if (node.virtual() || !node.isMap) {
            throw SerializationException(node, type, "Unit is not present")
        }
    }

    override fun serialize(type: Type, obj: Unit?, node: ConfigurationNode) {
        if (obj == null) return
        node.set(emptyMap<String, String>())
    }
}