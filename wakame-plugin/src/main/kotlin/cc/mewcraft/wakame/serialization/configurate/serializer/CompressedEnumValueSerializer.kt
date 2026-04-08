package cc.mewcraft.wakame.serialization.configurate.serializer

import cc.mewcraft.lazyconfig.configurate.SimpleSerializer
import cc.mewcraft.wakame.util.EnumLookup
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.serialize.SerializationException
import xyz.xenondevs.commons.reflection.rawType
import java.lang.reflect.Type

object CompressedEnumValueSerializer : SimpleSerializer<Enum<*>> {

    override fun deserialize(type: Type, node: ConfigurationNode): Enum<*>? {
        val ordinal = node.get<Int>() ?: throw SerializationException(node, type, "Invalid enum ordinal")
        val subclass = type.rawType.asSubclass(Enum::class.java)
        val ret = EnumLookup.lookupByOrdinal(subclass, ordinal) ?: throw SerializationException(type, "Enum class $subclass does not contain ordinal $ordinal")
        return ret
    }

    override fun serialize(type: Type, obj: Enum<*>?, node: ConfigurationNode) {
        if (obj == null) {
            node.set(null)
            return
        }
        node.set(obj.ordinal)
    }

}