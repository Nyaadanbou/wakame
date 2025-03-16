package cc.mewcraft.wakame.serialization.configurate.typeserializer

import cc.mewcraft.wakame.config.configurate.TypeSerializer
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type

@Suppress(
    "RedundantUnitReturnType",
    "RedundantUnitExpression"
)
// FIXME #350: 搞清楚在 configurate 中表示 Unit 的最好方式
/*internal*/ object UnitSerializer : TypeSerializer<Unit> {
    override fun deserialize(type: Type, node: ConfigurationNode): Unit {
        if (node.virtual() && node.isMap) {
            throw SerializationException(node, type, "Unit is not present")
        }
        return Unit
    }

    override fun serialize(type: Type, obj: Unit?, node: ConfigurationNode) {
        if (obj == null) return
        // 只需让默认的 loader 可接受该泛型参数即可
        node.set(emptyMap<String, String>())
    }
}