package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.initializer.PreWorldDependency
import cc.mewcraft.wakame.initializer.ReloadDependency
import cc.mewcraft.wakame.item.scheme.NekoItem
import cc.mewcraft.wakame.item.scheme.NekoItemFactory
import cc.mewcraft.wakame.iterator.NekoItemNodeIterator
import net.kyori.adventure.key.Key
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger

@PreWorldDependency(
    runBefore = [
        AttributeRegistry::class,
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
        ElementRegistry::class,
        EntityReferenceRegistry::class,
        ItemSkinRegistry::class,
        KizamiRegistry::class,
        LevelMappingRegistry::class,
        RarityRegistry::class,
    ]
)
object NekoItemRegistry : KoinComponent, Initializable {
    val INSTANCES: Registry<Key, NekoItem> = SimpleRegistry()

    fun get(key: String): NekoItem = INSTANCES.get(Key.key(key))
    fun find(key: String): NekoItem? = INSTANCES.find(Key.key(key))

    override fun onPreWorld() {
        loadConfiguration()
    }

    override fun onReload() {
        loadConfiguration()
    }

    private val logger: Logger by inject(mode = LazyThreadSafetyMode.NONE)

    private fun loadConfiguration() {
        INSTANCES.clear()

        NekoItemNodeIterator.forEach { key, node ->
            runCatching {
                NekoItemFactory.create(key, node)
            }.onSuccess {
                INSTANCES.register(key, it)
            }.onFailure {
                logger.error("Can't load item '$key'", it)
            }
        }
    }
}