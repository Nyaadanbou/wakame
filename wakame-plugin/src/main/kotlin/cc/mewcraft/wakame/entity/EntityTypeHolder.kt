package cc.mewcraft.wakame.entity

import cc.mewcraft.wakame.SchemaSerializer
import cc.mewcraft.wakame.registry.EntityRegistry
import net.kyori.adventure.key.Key
import org.bukkit.entity.Entity
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.getList
import java.lang.reflect.Type

/**
 * A simple data structure holding a set of entity types.
 */
interface EntityTypeHolder {
    /**
     * The unique name of this holder.
     */
    val name: String

    /**
     * The keys of the objects in this holder.
     */
    val keySet: Set<Key>

    /**
     * Returns `true` if the key of the [obj] is in the [keySet].
     *
     * @param obj the key of the object to be checked
     */
    fun contains(obj: Entity): Boolean

    /**
     * Returns `true` if the [key] is in the [keySet].
     *
     * @param key the key to be checked
     */
    fun contains(key: Key): Boolean
}

private data class EntityTypeHolderImpl(
    override val name: String,
    override val keySet: Set<Key>,
) : KoinComponent, EntityTypeHolder {
    private val entityKeyLookup: EntityKeyLookup by inject()

    override fun contains(obj: Entity): Boolean {
        return entityKeyLookup.getKey(obj) in keySet
    }

    override fun contains(key: Key): Boolean {
        return key in keySet
    }
}

internal object EntityTypeHolderSerializer : SchemaSerializer<EntityTypeHolder> {
    override fun deserialize(type: Type, node: ConfigurationNode): EntityTypeHolder {
        val rawScalar = node.rawScalar()?.toString()
        if (rawScalar != null) {
            return EntityRegistry.TYPES[rawScalar]
        }

        val name = node.key().toString()
        val keySet = node.getList<Key>(emptyList()).toSet()
        return EntityTypeHolderImpl(name, keySet)
    }
}