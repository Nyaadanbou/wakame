package cc.mewcraft.wakame.config.configurate

import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.TypeSerializer
import java.lang.reflect.Type

interface TypeDeserializer<T> : TypeSerializer<T> {

    override fun deserialize(type: Type, node: ConfigurationNode): T

    override fun serialize(type: Type, obj: T?, node: ConfigurationNode): Unit = throw UnsupportedOperationException()

}