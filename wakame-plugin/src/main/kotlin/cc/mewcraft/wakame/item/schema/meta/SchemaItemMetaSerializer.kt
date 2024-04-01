package cc.mewcraft.wakame.item.schema.meta

import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.ConfigurationOptions
import org.spongepowered.configurate.serialize.TypeSerializer
import java.lang.reflect.Type

internal interface SchemaItemMetaSerializer<T : SchemaItemMeta<*>> : TypeSerializer<T> {
    /**
     * You must override this property to provide a non-null empty value **if
     * and only if** `this` schema item meta is optional in the configuration.
     * In other words, the schema item meta is not optional if this property
     * always returns `null`.
     */
    val defaultValue: T? get() = null

    /**
     * Deserializes the schema item meta from [node].
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
        return defaultValue
    }
}