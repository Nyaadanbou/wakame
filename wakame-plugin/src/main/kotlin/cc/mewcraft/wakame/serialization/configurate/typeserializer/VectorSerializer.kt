package cc.mewcraft.wakame.serialization.configurate.typeserializer

import cc.mewcraft.wakame.config.configurate.TypeSerializer
import org.joml.Vector3f
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.getList
import org.spongepowered.configurate.kotlin.extensions.typedSet
import java.lang.reflect.Type

internal object Vector3fSerializer : TypeSerializer<Vector3f> {
    override fun deserialize(type: Type, node: ConfigurationNode): Vector3f {
        val floats = node.getList<Float>(emptyList())
        require(floats.size == 3) { "Vector3f must have 3 elements" }
        return Vector3f(floats[0], floats[1], floats[2])
    }

    override fun serialize(type: Type, obj: Vector3f?, node: ConfigurationNode) {
        if (obj == null) return
        node.typedSet(listOf(obj.x, obj.y, obj.z))
    }
}