package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.initializer.Initializable
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

    fun get(key: String): NekoItem = INSTANCES[Key(key)]
    fun find(key: String): NekoItem? = INSTANCES.find(Key(key))

    override fun onPreWorld() {
        loadConfiguration()
    }

    override fun onReload() {
        loadConfiguration()
    }

    private val logger: Logger by inject(mode = LazyThreadSafetyMode.NONE)

    private fun loadConfiguration() {
        INSTANCES.clear()

        NekoItemNodeIterator.forEach { key, node, path ->
            runCatching {
                NekoItemFactory.create(key, node, path)
            }.onSuccess {
                INSTANCES.register(key, it)
            }.onFailure {
                logger.error("Can't load item '$key'", it)
            }
        }
    }
}