package cc.mewcraft.wakame.registry

import cc.mewcraft.commons.provider.Provider
import cc.mewcraft.commons.provider.immutable.*
import cc.mewcraft.wakame.ReloadableProperty
import cc.mewcraft.wakame.display2.NekoItemHolder
import cc.mewcraft.wakame.initializer.*
import cc.mewcraft.wakame.item.*
import cc.mewcraft.wakame.item.behavior.ItemBehaviorMap
import cc.mewcraft.wakame.item.template.ItemTemplateMap
import cc.mewcraft.wakame.iterator.NekoItemNodeIterator
import cc.mewcraft.wakame.util.Key
import net.kyori.adventure.key.Key
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
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
    @JvmField
    val VANILLA: Registry<Key, NekoItem> = SimpleRegistry()

    /**
     * 用于一般用途的 [NekoItem]. 这些 [NekoItem] 可以用来生成物品.
     */
    @JvmField
    val CUSTOM: Registry<Key, NekoItem> = SimpleRegistry()

    /**
     * All namespaces of loaded items.
     */
    @get:JvmName("namespaces")
    val NAMESPACES: List<String> by ReloadableProperty {
        CUSTOM.values.map { it.id.namespace() }.distinct().sorted()
    }

    /**
     * All paths of each available namespace.
     */
    @get:JvmName("pathsByNamespace")
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

    private val logger: Logger = get()

    /**
     * Error [NekoItem] 的唯一标识.
     */
    @JvmField
    val ERROR_NEKO_ITEM_ID: Key = Key.key("internal:error")

    /**
     * Error [NekoStack] 的实例.
     */
    @get:JvmName("errorNekoStack")
    val ERROR_NEKO_STACK: NekoStack
        get() = ItemRegistryInternals.errorNekoStackProvider.get().clone()

    /**
     * Error [ItemStack] 的实例 (相当于 [NekoStack.itemStack]).
     */
    @get:JvmName("errorItemStack")
    val ERROR_ITEM_STACK: ItemStack
        get() = ItemRegistryInternals.errorNekoStackProvider.get().itemStack

    private fun loadConfiguration() {
        // 清空注册表
        VANILLA.clear()
        CUSTOM.clear()

        // 注册默认的 error item
        // 配置文件可以将该实例覆盖
        CUSTOM.register(ERROR_NEKO_ITEM_ID, ItemRegistryInternals.defaultErrorNekoItem)

        // 加载所有配置文件
        for ((key, path, node) in NekoItemNodeIterator) {
            val namespace = key.namespace()
            if (namespace == Key.MINECRAFT_NAMESPACE) {
                // Process as vanilla item
                runCatching { NekoItemFactory.createVanilla(key, path, node) }
                    .onSuccess { VANILLA.register(key, it) }
                    .onFailure { logError(key, it) }
            } else {
                // Process as custom item
                runCatching { NekoItemFactory.createCustom(key, path, node) }
                    .onSuccess { CUSTOM.register(key, it) }
                    .onFailure { logError(key, it) }
            }
        }

        // 重载所有 NekoItemHolder
        NekoItemHolder.reload()

        // 重载 Error NekoItem
        ItemRegistryInternals.errorNekoStackProvider.update()
    }

    private fun logError(key: Key, throwable: Throwable) {
        if (Initializer.isDebug) {
            logger.error("Can't load item '$key'", throwable)
        } else {
            logger.error("Can't load item '$key': ${throwable.message}")
        }
    }
}

private object ItemRegistryInternals {
    @JvmField
    val defaultErrorNekoItem: SimpleNekoItem =
        SimpleNekoItem(
            id = ItemRegistry.ERROR_NEKO_ITEM_ID,
            base = ItemBaseImpl(Material.BARRIER, """[item_name="ERROR"]"""),
            slotGroup = ItemSlotGroup.empty(),
            templates = ItemTemplateMap.empty(),
            behaviors = ItemBehaviorMap.empty(),
        )

    @JvmField
    val errorNekoStackProvider: Provider<NekoStack> =
        provider { ItemRegistry.CUSTOM.find(ItemRegistry.ERROR_NEKO_ITEM_ID) }
            .orElse(defaultErrorNekoItem)
            .map(NekoItem::realize)
}