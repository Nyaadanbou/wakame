package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.ReloadableProperty
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.initializer.Initializer
import cc.mewcraft.wakame.initializer.PreWorldDependency
import cc.mewcraft.wakame.initializer.ReloadDependency
import cc.mewcraft.wakame.item.NekoItem
import cc.mewcraft.wakame.item.NekoItemFactory
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
        EntityRegistry::class,
        ItemSkinRegistry::class,
        KizamiRegistry::class,
        LevelMappingRegistry::class,
        RarityRegistry::class,
        SkillRegistry::class,
    ]
)
@ReloadDependency(
    runBefore = [
        AttributeRegistry::class,
        BehaviorRegistry::class,
        ElementRegistry::class,
        EntityRegistry::class,
        ItemSkinRegistry::class,
        KizamiRegistry::class,
        LevelMappingRegistry::class,
        RarityRegistry::class,
        SkillRegistry::class,
    ]
)
object ItemRegistry : KoinComponent, Initializable {
    /**
     * All loaded [NekoItem]s.
     */
    val INSTANCES: Registry<Key, NekoItem> = SimpleRegistry()

    /**
     * All namespaces of loaded items.
     */
    val NAMESPACES: List<String> by ReloadableProperty {
        INSTANCES.objects.map { it.key.namespace() }.distinct().sorted()
    }

    /**
     * All paths of each available namespace.
     */
    val PATHS_BY_NAMESPACE: Map<String, List<String>> by ReloadableProperty {
        val ret = hashMapOf<String, MutableList<String>>()
        INSTANCES.objects.forEach {
            val namespace = it.key.namespace()
            val path = it.key.value()
            ret.getOrPut(namespace, ::ArrayList).add(path)
        }
        ret
    }

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