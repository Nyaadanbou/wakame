package cc.mewcraft.wakame.config.configurate

import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.ConfigurationOptions
import org.spongepowered.configurate.serialize.TypeSerializer
import java.lang.reflect.Type

@Deprecated("Bad naming. Use TypeSerializer instead", replaceWith = ReplaceWith("TypeSerializer", "cc.mewcraft.wakame.config.configurate.TypeSerializer"))
interface TypeDeserializer<T> : TypeSerializer<T> {

    override fun deserialize(type: Type, node: ConfigurationNode): T

    override fun serialize(type: Type, obj: T?, node: ConfigurationNode): Unit = throw UnsupportedOperationException()

    override fun emptyValue(specificType: Type, options: ConfigurationOptions): T? = null

}