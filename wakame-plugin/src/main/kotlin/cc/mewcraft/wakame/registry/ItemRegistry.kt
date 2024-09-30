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
     * 用于原版物品代理的 [NekoItem]. 这些 NekoItem 不应该用来生成物品.
     */
    val VANILLA: Registry<Key, NekoItem> = SimpleRegistry()

    /**
     * 用于一般用途的 [NekoItem]. 这些 [NekoItem] 可以用来生成物品.
     */
    val CUSTOM: Registry<Key, NekoItem> = SimpleRegistry()

    /**
     * All namespaces of loaded items.
     */
    val NAMESPACES: List<String> by ReloadableProperty {
        CUSTOM.values.map { it.id.namespace() }.distinct().sorted()
    }

    /**
     * All paths of each available namespace.
     */
    val PATHS_BY_NAMESPACE: Map<String, List<String>> by ReloadableProperty {
        val ret = hashMapOf<String, MutableList<String>>()
        CUSTOM.values.forEach {
            val namespace = it.id.namespace()
            val path = it.id.value()
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
    fun Registry<Key, NekoItem>.get(key: String): NekoItem {
        return this[Key(key)]
    }

    /**
     * Gets specific [NekoItem] from the registry if there is one.
     *
     * @param key the key in string representation
     * @return the specific [NekoItem] or `null` if not found
     */
    fun Registry<Key, NekoItem>.find(key: String): NekoItem? {
        return this.find(Key(key))
    }

    /**
     * Gets specific [NekoItem] from the registry.
     *
     * @param namespace the namespace
     * @param path the path
     * @return the specific [NekoItem]
     */
    fun Registry<Key, NekoItem>.get(namespace: String, path: String): NekoItem {
        return this[Key(namespace, path)]
    }

    /**
     * Gets specific [NekoItem] from the registry if there is one.
     *
     * @param namespace the namespace
     * @param path the path
     * @return the specific [NekoItem] or `null` if not found
     */
    fun Registry<Key, NekoItem>.find(namespace: String, path: String): NekoItem? {
        return this.find(Key(namespace, path))
    }

    override fun onPreWorld() {
        loadConfiguration()
    }

    override fun onReload() {
        loadConfiguration()
    }

    private val logger: Logger by inject()

    private fun loadConfiguration() {
        VANILLA.clear()
        CUSTOM.clear()

        for ((key, path, node) in NekoItemNodeIterator) {
            val namespace = key.namespace()

            if (namespace == Key.MINECRAFT_NAMESPACE) {
                // Process as vanilla item
                runCatching {
                    NekoItemFactory.createVanilla(key, path, node)
                }.onSuccess {
                    VANILLA.register(key, it)
                }.onFailure {
                    logError(key, it)
                }
            } else {
                // Process as custom item
                runCatching {
                    NekoItemFactory.createCustom(key, path, node)
                }.onSuccess {
                    CUSTOM.register(key, it)
                }.onFailure {
                    logError(key, it)
                }
            }
        }
    }

    private fun logError(key: Key, throwable: Throwable) {
        if (Initializer.isDebug) {
            logger.error("Can't load item '$key'", throwable)
        } else {
            logger.error("Can't load item '$key': ${throwable.message}")
        }
    }
}