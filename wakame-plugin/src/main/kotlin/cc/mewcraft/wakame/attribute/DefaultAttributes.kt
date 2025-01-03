package cc.mewcraft.wakame.attribute

import cc.mewcraft.wakame.initializer2.Init
import cc.mewcraft.wakame.initializer2.InitFun
import cc.mewcraft.wakame.initializer2.InitStage
import cc.mewcraft.wakame.registry.AttributeRegistry
import cc.mewcraft.wakame.util.Key
import net.kyori.adventure.key.Key
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger

/**
 * Provides default [AttributeInstance]s for various subject types.
 */
@Init(
    stage = InitStage.PRE_WORLD,
    runBefore = [AttributeRegistry::class],
)
//@PreWorldDependency(
//    runBefore = [AttributeRegistry::class],
//)
//@ReloadDependency(
//    runBefore = [AttributeRegistry::class],
//)
object DefaultAttributes : KoinComponent {

    private val LOGGER: Logger by inject()

    /**
     * This registry holds the attribute suppliers for all subject types.
     *
     * ## Notes
     *
     * Using [Key] to identify the "type" of living entities because we want the whole
     * attribute system to be compatible with 3rd party mob system such as MythicMobs,
     * in which case the enum type is not enough to express all types.
     */
    private val SUPPLIERS: HashMap<Key, AttributeSupplier> = HashMap()

    /**
     * Gets the [AttributeSupplier] for the [key].
     *
     * **Currently, only limited subject types are supported.** Getting an
     * unsupported subject type will throw an [IllegalArgumentException].
     *
     * @param key the subject key
     * @return the attribute supplier
     * @throws IllegalArgumentException
     */
    fun getSupplier(key: Key): AttributeSupplier {
        return requireNotNull(SUPPLIERS[key]) { "Can't find attribute supplier for key '$key'" }
    }

    /**
     * Checks if the [key] subject has a [AttributeSupplier].
     *
     * @param key the subject key
     * @return true if the subject type has a default supplier, false otherwise
     */
    fun hasSupplier(key: Key): Boolean {
        return SUPPLIERS.containsKey(key)
    }

    /**
     * Registers a [AttributeSupplier] for the [key].
     *
     * @param key the subject key
     * @param supplier the attribute supplier
     * @return the previous supplier, if any
     */
    fun addSupplier(key: Key, supplier: AttributeSupplier): AttributeSupplier? {
        return SUPPLIERS.put(key, supplier)
    }

    /* Internals */

    @InitFun
    fun onPreWorld() {
        loadConfiguration()
    }

//    override fun onReload() {
//        loadConfiguration()
//    }

    private fun loadConfiguration() {
        SUPPLIERS.clear()

        val node = AttributeSupport.ENTITY_ATTRIBUTE_CONFIG.get()
        val map = AttributeSupplierDeserializer(node).deserialize()
        map.forEach { (k, v) ->
            addSupplier(k, v)
        }

        LOGGER.info("Registered attribute suppliers: {}", map.keys.joinToString(transform = Key::asString))
    }
}
