package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.ReloadableProperty
import cc.mewcraft.wakame.display2.NekoItemHolder
import cc.mewcraft.wakame.initializer.*
import cc.mewcraft.wakame.item.*
import cc.mewcraft.wakame.item.behavior.ItemBehaviorMap
import cc.mewcraft.wakame.item.template.ItemTemplateMap
import cc.mewcraft.wakame.iterator.NekoItemNodeIterator
import net.kyori.adventure.key.Key
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.jetbrains.annotations.Contract
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.slf4j.Logger
import xyz.xenondevs.commons.provider.Provider
import xyz.xenondevs.commons.provider.immutable.*

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
     * 用于一般用途的 [NekoItem]. 这些模板可以用来生成 [ItemStack].
     */
    @JvmField
    val CUSTOM: Registry<Key, NekoItem> = SimpleRegistry()

    /**
     * 包含了虚拟的 [NekoItem]. 这些模板不应该用来生成 [ItemStack].
     */
    @JvmField
    val IMAGINARY: Registry<Key, NekoItem> = SimpleRegistry()

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
        return this[Key.key(key)]
    }

    /**
     * Gets specific [NekoItem] from the registry if there is one.
     *
     * @param key the key in string representation
     * @return the specific [NekoItem] or `null` if not found
     */
    fun Registry<Key, NekoItem>.find(key: String): NekoItem? {
        return this.find(Key.key(key))
    }

    /**
     * Gets specific [NekoItem] from the registry.
     *
     * @param namespace the namespace
     * @param path the path
     * @return the specific [NekoItem]
     */
    fun Registry<Key, NekoItem>.get(namespace: String, path: String): NekoItem {
        return this[Key.key(namespace, path)]
    }

    /**
     * Gets specific [NekoItem] from the registry if there is one.
     *
     * @param namespace the namespace
     * @param path the path
     * @return the specific [NekoItem] or `null` if not found
     */
    fun Registry<Key, NekoItem>.find(namespace: String, path: String): NekoItem? {
        return this.find(Key.key(namespace, path))
    }

    override fun onPreWorld() {
        loadConfiguration()
    }

    override fun onReload() {
        loadConfiguration()
    }

    private val LOGGER: Logger = get()

    /**
     * 默认的 [Error NekoItem][NekoItem] 的唯一标识.
     */
    @JvmField
    val ERROR_NEKO_ITEM_ID: Key = Key.key("internal:error")

    /**
     * 默认的 [Error NekoStack][NekoStack].
     */
    @get:JvmName("getErrorNekoItemProvider")
    val ERROR_NEKO_ITEM_PROVIDER: Provider<NekoItem>
        get() = ItemRegistryInternals.ERROR_NEKO_ITEM_PROVIDER

    /**
     * 默认的 [Error NekoStack][NekoStack].
     */
    @get:Contract(" -> new")
    @get:JvmName("getErrorNekoStack")
    val ERROR_NEKO_STACK: NekoStack
        get() = ItemRegistryInternals.ERROR_NEKO_STACK_PROVIDER.get().clone()

    /**
     * 默认的 [Error NekoStack][ItemStack].
     */
    @get:Contract(" -> new")
    @get:JvmName("getErrorItemStack")
    val ERROR_ITEM_STACK: ItemStack
        get() = ItemRegistryInternals.ERROR_NEKO_STACK_PROVIDER.get().itemStack

    private fun loadConfiguration() {
        // 清空注册表
        IMAGINARY.clear()
        LOGGER.info("Unregistered all vanilla items.")
        CUSTOM.clear()
        LOGGER.info("Unregistered all custom items.")

        // 加载所有配置文件
        for ((key, path, node) in NekoItemNodeIterator) {
            val namespace = key.namespace()
            if (namespace == Key.MINECRAFT_NAMESPACE) {
                // Process as vanilla item
                LOGGER.info("Loading vanilla item: '$key'")
                runCatching { NekoItemFactory.createVanilla(key, path, node) }
                    .onSuccess { IMAGINARY.register(key, it) }
                    .onFailure { reportError(key, it) }
            } else {
                // Process as custom item
                LOGGER.info("Loading custom item: '$key'")
                runCatching { NekoItemFactory.createCustom(key, path, node) }
                    .onSuccess { CUSTOM.register(key, it) }
                    .onFailure { reportError(key, it) }
            }
        }
        LOGGER.info("Registered all items.")

        // 注册默认的 error item
        if (CUSTOM.has(ERROR_NEKO_ITEM_ID)) {
            LOGGER.info("Found a custom error neko item!")
        } else {
            LOGGER.warn("Custom error neko item not found, using default one.")
            CUSTOM.register(ERROR_NEKO_ITEM_ID, ItemRegistryInternals.DEFAULT_ERROR_NEKO_ITEM)
            LOGGER.info("Registered default error neko item.")
        }

        // 重新加载 Error NekoItem
        ItemRegistryInternals.ERROR_NEKO_ITEM_PROVIDER.update()

        // 重新加载 NekoItemHolder
        NekoItemHolder.reload()
    }

    private fun reportError(key: Key, throwable: Throwable) {
        if (Initializer.isDebug) {
            LOGGER.error("Can't load item '$key'", throwable)
        } else {
            LOGGER.error("Can't load item '$key': ${throwable.message}")
        }
    }
}

private object ItemRegistryInternals {
    /**
     * 如果用户没有提供默认的物品, 则使用内置的这个.
     */
    @JvmField
    val DEFAULT_ERROR_NEKO_ITEM: SimpleNekoItem =
        SimpleNekoItem(
            id = ItemRegistry.ERROR_NEKO_ITEM_ID,
            base = ItemBaseImpl(Material.BARRIER, """[item_name="ERROR"]"""),
            slotGroup = ItemSlotGroup.empty(),
            templates = ItemTemplateMap.empty(),
            behaviors = ItemBehaviorMap.empty(),
        )

    @JvmField
    val ERROR_NEKO_ITEM_PROVIDER: Provider<NekoItem> =
        provider { ItemRegistry.CUSTOM.find(ItemRegistry.ERROR_NEKO_ITEM_ID) }.orElse(DEFAULT_ERROR_NEKO_ITEM)

    @JvmField
    val ERROR_NEKO_STACK_PROVIDER: Provider<NekoStack> =
        ERROR_NEKO_ITEM_PROVIDER.map(NekoItem::realize)
}