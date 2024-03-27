package cc.mewcraft.wakame.item.scheme.behavior

import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.ConfigurationOptions
import org.spongepowered.configurate.serialize.TypeSerializer
import java.lang.reflect.Type

internal interface ItemBehaviorSerializer<T : ItemBehavior> : TypeSerializer<T> {
    /**
     * Deserializes the scheme item meta from [node].
     */
    override fun deserialize(type: Type, node: ConfigurationNode): T

    /**
     * DO NOT OVERRIDE THIS.
     */
    override fun serialize(type: Type, obj: T?, node: ConfigurationNode): Nothing =
        throw UnsupportedOperationException()

    /**
     * DO NOT OVERRIDE THIS.
     */
    override fun emptyValue(specificType: Type?, options: ConfigurationOptions?): T? {
        return null
    }
}