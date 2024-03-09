package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.iterator.NekoItemNodeIterator
import cc.mewcraft.wakame.annotation.InternalApi
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.initializer.PreWorldDependency
import cc.mewcraft.wakame.initializer.ReloadDependency
import cc.mewcraft.wakame.item.scheme.NekoItem
import cc.mewcraft.wakame.item.scheme.NekoItemFactory
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
object NekoItemRegistry : KoinComponent, Initializable,
    Registry<Key, NekoItem> by HashMapRegistry() {

    private val logger: Logger by inject(mode = LazyThreadSafetyMode.NONE)

    fun get(key: String): NekoItem? = get(Key.key(key))
    fun getOrThrow(key: String): NekoItem = getOrThrow(Key.key(key))

    private fun loadConfiguration() {
        @OptIn(InternalApi::class) clearName2Object()

        NekoItemNodeIterator.execute { key, node ->
            runCatching {
                NekoItemFactory.create(key, node)
            }.onSuccess {
                registerName2Object(key, it)
            }.onFailure {
                logger.error("Can't load item '$key': {}", it.message)
            }
        }
    }

    override fun onPreWorld() {
        loadConfiguration()
    }

    override fun onReload() {
        loadConfiguration()
    }
}