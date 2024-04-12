package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.initializer.Initializer
import cc.mewcraft.wakame.initializer.PreWorldDependency
import cc.mewcraft.wakame.initializer.ReloadDependency
import cc.mewcraft.wakame.item.schema.NekoItem
import cc.mewcraft.wakame.item.schema.NekoItemFactory
import cc.mewcraft.wakame.iterator.NekoItemNodeIterator
import cc.mewcraft.wakame.util.Key
import net.kyori.adventure.key.Key
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger

@PreWorldDependency(
    runBefore = [
        AttributeRegistry::class,
        BehaviorRegistry::class,
        ElementRegistry::class,
        EntityReferenceRegistry::class,
        ItemSkinRegistry::class,
        KizamiRegistry::class,
        LevelMappingRegistry::class,
        RarityRegistry::class,
    ]
)
@ReloadDependency(
    runBefore = [
        AttributeRegistry::class,
        BehaviorRegistry::class,
        ElementRegistry::class,
        EntityReferenceRegistry::class,
        ItemSkinRegistry::class,
        KizamiRegistry::class,
        LevelMappingRegistry::class,
        RarityRegistry::class,
    ]
)
object NekoItemRegistry : KoinComponent, Initializable {
    /**
     * The registry has all loaded [NekoItem]s.
     */
    val INSTANCES: Registry<Key, NekoItem> = SimpleRegistry()

    /**
     * Gets specific [NekoItem] from the registry.
     *
     * @param key the key in string representation
     * @return the specific [NekoItem]
     */
    fun Registry<Key, NekoItem>.get(key: String): NekoItem = this[Key(key)]

    /**
     * Gets specific [NekoItem] from the registry if there is one.
     *
     * @param key the key in string representation
     * @return the specific [NekoItem] or `null` if not found
     */
    fun Registry<Key, NekoItem>.find(key: String): NekoItem? = this.find(Key(key))

    /**
     * Gets specific [NekoItem] from the registry.
     *
     * @param namespace the namespace
     * @param path the path
     * @return the specific [NekoItem]
     */
    fun Registry<Key, NekoItem>.get(namespace: String, path: String): NekoItem = this[Key(namespace, path)]

    /**
     * Gets specific [NekoItem] from the registry if there is one.
     *
     * @param namespace the namespace
     * @param path the path
     * @return the specific [NekoItem] or `null` if not found
     */
    fun Registry<Key, NekoItem>.find(namespace: String, path: String): NekoItem? = this.find(Key(namespace, path))

    override fun onPreWorld() = loadConfiguration()
    override fun onReload() = loadConfiguration()

    private val LOGGER: Logger by inject()
    private fun loadConfiguration() {
        INSTANCES.clear()

        NekoItemNodeIterator.forEach { key, path, node ->
            runCatching {
                NekoItemFactory.create(key, path, node)
            }.onSuccess {
                INSTANCES.register(key, it)
            }.onFailure {
                if (Initializer.isDebug) {
                    LOGGER.error("Can't load item '$key'", it)
                } else {
                    LOGGER.error("Can't load item '$key': ${it.message}")
                }
            }
        }
    }
}