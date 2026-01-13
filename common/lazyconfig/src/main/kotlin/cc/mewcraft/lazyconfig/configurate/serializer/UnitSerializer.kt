package cc.mewcraft.lazyconfig.configurate.serializer

import cc.mewcraft.lazyconfig.configurate.SimpleSerializer
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type

@Suppress(
    "RedundantUnitReturnType",
    "RedundantUnitExpression"
)
// FIXME #350: 暂时用值为 0 Byte 来表示 Unit.
//  正经做法是用空 Map 来表示 Unit, 但 ConfigurateOps
//  在 decode 时无法读取到 NbtOps 的空 CompoundTag.
/*internal*/ object UnitSerializer : SimpleSerializer<Unit> {
    const val ZERO_BYTE: Byte = 0

    override fun deserialize(type: Type, node: ConfigurationNode): Unit {
        if (node.empty() || node.get(Byte::class) == ZERO_BYTE) {
            return Unit
        } else {
            throw SerializationException(node, type, "Cannot deserialize node to Unit (value must be a zero byte or empty)")
        }
    }

    override fun serialize(type: Type, obj: Unit?, node: ConfigurationNode) {
        if (obj == null) return
        node.set(ZERO_BYTE)
    }
}