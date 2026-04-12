package cc.mewcraft.wakame.serialization.configurate.serializer

import cc.mewcraft.lazyconfig.configurate.SimpleSerializer
import org.joml.Quaternionf
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.kotlin.extensions.typedSet
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type

internal object QuaternionfSerializer : SimpleSerializer<Quaternionf> {
    override fun deserialize(type: Type, node: ConfigurationNode): Quaternionf {
        val floats = node.get<FloatArray>() ?: throw SerializationException(node, type, "Quaternionf must be an array of 4 floats")
        require(floats.size == 4) { "Quaternionf must exactly have 4 floats" }
        return Quaternionf(floats[0], floats[1], floats[2], floats[3])
    }

    override fun serialize(type: Type, obj: Quaternionf?, node: ConfigurationNode) {
        if (obj == null) return
        node.typedSet(floatArrayOf(obj.x, obj.y, obj.z, obj.w))
    }
}