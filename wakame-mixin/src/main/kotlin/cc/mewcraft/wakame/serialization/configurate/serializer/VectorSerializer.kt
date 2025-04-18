package cc.mewcraft.wakame.serialization.configurate.serializer

import cc.mewcraft.wakame.serialization.configurate.TypeSerializer2
import org.joml.Vector3f
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.kotlin.extensions.typedSet
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type

/*internal*/ object Vector3fSerializer : TypeSerializer2<Vector3f> {
    override fun deserialize(type: Type, node: ConfigurationNode): Vector3f {
        val floats = node.get<FloatArray>() ?: throw SerializationException(node, type, "Vector3f must be an array of 3 floats")
        require(floats.size == 3) { "Vector3f must exactly have 3 floats" }
        return Vector3f(floats[0], floats[1], floats[2])
    }

    override fun serialize(type: Type, obj: Vector3f?, node: ConfigurationNode) {
        if (obj == null) return
        node.typedSet(floatArrayOf(obj.x, obj.y, obj.z))
    }
}