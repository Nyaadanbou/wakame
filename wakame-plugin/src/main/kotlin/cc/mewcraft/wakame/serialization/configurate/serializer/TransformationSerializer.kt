package cc.mewcraft.wakame.serialization.configurate.serializer

import cc.mewcraft.lazyconfig.configurate.SimpleSerializer
import cc.mewcraft.wakame.util.math.Vec3f
import org.bukkit.util.Transformation
import org.joml.Quaternionf
import org.joml.Vector3f
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get
import java.lang.reflect.Type

/*internal*/ object TransformationSerializer : SimpleSerializer<Transformation> {
    override fun deserialize(type: Type, node: ConfigurationNode): Transformation {
        val translation = node.node("translation").get<Vector3f>(Vec3f.zero())
        val scale = node.node("scale").get<Vector3f>(Vec3f.one())
        val leftRotation = node.node("left_rotation").get<Quaternionf>(Quaternionf())
        val rightRotation = node.node("right_rotation").get<Quaternionf>(Quaternionf())
        return Transformation(translation, leftRotation, scale, rightRotation)
    }

    // TODO 序列化方法似乎没必要实现
}