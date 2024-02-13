package cc.mewcraft.wakame.item.scheme.meta

import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.ConfigurationOptions
import org.spongepowered.configurate.serialize.TypeSerializer
import java.lang.reflect.Type

internal interface SchemeMetaSerializer<T : SchemeMeta<*>> : TypeSerializer<T> {
    /**
     * You must override this property to provide a non-null empty value **if
     * and only if** `this` scheme meta is optional in the configuration. In
     * other words, the scheme meta is not optional if this property always
     * returns `null`.
     */
    val emptyValue: T? get() = null

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
        return emptyValue
    }
}