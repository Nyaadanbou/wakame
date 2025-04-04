package cc.mewcraft.wakame.ability2.meta

import cc.mewcraft.wakame.serialization.configurate.TypeSerializer2
import cc.mewcraft.wakame.util.require
import com.github.quillraven.fleks.Component
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type

/**
 * 代表运行一个技能逻辑所需要的参数.
 *
 * @see AbilityMetaType
 */
data class AbilityMeta(
    val type: AbilityMetaType<*>,
    val params: Component<*>,
    val display: AbilityDisplay,
) {
    companion object {
        val SERIALIZER: TypeSerializer2<AbilityMeta> = Serializer
    }

    private object Serializer : TypeSerializer2<AbilityMeta> {
        override fun deserialize(type: Type, node: ConfigurationNode): AbilityMeta {
            val metaType = node.node("type").require<AbilityMetaType<*>>()
            val component = node.get(metaType.typeToken)
            if (component == null) {
                throw SerializationException(node, type, "Failed to deserialize $type")
            }
            val display = AbilityDisplay.EMPTY // TODO: Deserialize display
            return AbilityMeta(metaType, component, display)
        }
    }
}