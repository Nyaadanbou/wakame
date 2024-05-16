package cc.mewcraft.wakame.attribute

import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.registry.Registry
import cc.mewcraft.wakame.registry.SimpleRegistry
import cc.mewcraft.wakame.util.Key
import net.kyori.adventure.key.Key
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger

/**
 * Provides default [AttributeInstance]s for various entity types.
 */
object DefaultAttributes : KoinComponent, Initializable {

    private val LOGGER: Logger by inject()

    /**
     * This registry holds the attribute suppliers for all entity types.
     *
     * ## Notes
     *
     * Using [Key] to identify the "type" of living entities because we want the whole
     * attribute system to be compatible with 3rd party mob system such as MythicMobs,
     * in which case the enum type is not enough to express all types.
     */
    private val SUPPLIERS: Registry<Key, AttributeSupplier> = SimpleRegistry()

    /**
     * Loads all attribute suppliers from config.
     */
    private fun deserializeSuppliers(): Map<Key, AttributeSupplier> {
        val node = AttributeSupport.ENTITY_ATTRIBUTE_CONFIG.get()
        val map = AttributeSupplierDeserializer(node).deserialize()
        return map
    }

    /**
     * Gets the [AttributeSupplier] for the [key].
     *
     * **Currently, only limited entity types are supported.** Getting an
     * unsupported entity type will throw an [IllegalArgumentException].
     *
     * @param key the entity key
     * @return the attribute supplier
     * @throws IllegalArgumentException
     */
    fun getSupplier(key: Key): AttributeSupplier {
        return requireNotNull(SUPPLIERS.find(key)) { "Can't find attribute supplier for entity $key" }
    }

    /**
     * Checks if the [key] entity has a [AttributeSupplier].
     *
     * @param key the entity key
     * @return true if the entity type has a default supplier, false otherwise
     */
    fun hasSupplier(key: Key): Boolean {
        return SUPPLIERS.has(key)
    }

    /**
     * Registers a [AttributeSupplier] for the [key].
     *
     * @param key the entity key
     * @param supplier the attribute supplier
     */
    fun addSupplier(key: Key, supplier: AttributeSupplier) {
        SUPPLIERS.register(key, supplier)
    }

    /**
     * Clears all suppliers.
     */
    fun clear() {
        SUPPLIERS.clear()
    }

    override fun onPostWorld() {
        clear()

        deserializeSuppliers().forEach { (k, v) ->
            addSupplier(k, v)
            LOGGER.info("Registered attribute supplier: {}", k)
        }
    }
}
