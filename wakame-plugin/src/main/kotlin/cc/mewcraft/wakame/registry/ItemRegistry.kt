package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.display2.NekoItemHolder
import cc.mewcraft.wakame.initializer.Initializer
import cc.mewcraft.wakame.initializer2.Init
import cc.mewcraft.wakame.initializer2.InitFun
import cc.mewcraft.wakame.initializer2.InitStage
import cc.mewcraft.wakame.item.*
import cc.mewcraft.wakame.item.behavior.ItemBehaviorMap
import cc.mewcraft.wakame.item.template.ItemTemplateMap
import cc.mewcraft.wakame.iterator.NekoItemNodeIterator
import cc.mewcraft.wakame.iterator.NekoItemNodeIterator.iterator
import cc.mewcraft.wakame.reloader.Reload
import cc.mewcraft.wakame.reloader.ReloadableFun
import cc.mewcraft.wakame.reloader.ReloadableOrder
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import net.kyori.adventure.key.Key
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.jetbrains.annotations.Contract
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.slf4j.Logger
import xyz.xenondevs.commons.provider.Provider
import xyz.xenondevs.commons.provider.immutable.map
import xyz.xenondevs.commons.provider.immutable.orElse
import xyz.xenondevs.commons.provider.immutable.provider

@Init(
    stage = InitStage.PRE_WORLD,
    runAfter = [
        AttributeRegistry::class,
        ElementRegistry::class,
        EntityRegistry::class,
        ItemSkinRegistry::class,
        KizamiRegistry::class,
        LevelMappingRegistry::class,
        RarityRegistry::class,
        AbilityRegistry::class,
    ]
)
@Reload(
    order = ReloadableOrder.NORMAL,
    runAfter = [
        AttributeRegistry::class,
        ElementRegistry::class,
        EntityRegistry::class,
        ItemSkinRegistry::class,
        KizamiRegistry::class,
        LevelMappingRegistry::class,
        RarityRegistry::class,
        AbilityRegistry::class,
    ]
)
//@PreWorldDependency(
//    runBefore = [
//        AttributeRegistry::class,
//        ElementRegistry::class,
//        EntityRegistry::class,
//        ItemSkinRegistry::class,
//        KizamiRegistry::class,
//        LevelMappingRegistry::class,
//        RarityRegistry::class,
//        AbilityRegistry::class,
//    ]
//)
//@ReloadDependency(
//    runBefore = [
//        AttributeRegistry::class,
//        ElementRegistry::class,
//        EntityRegistry::class,
//        ItemSkinRegistry::class,
//        KizamiRegistry::class,
//        LevelMappingRegistry::class,
//        RarityRegistry::class,
//        AbilityRegistry::class,
//    ]
//)
object ItemRegistry : KoinComponent {
    /**
     * 用于一般用途的 [NekoItem].
     * 这些物品类型可以用来生成 [ItemStack].
     */
    @JvmField
    val CUSTOM: Registry<Key, NekoItem> = SimpleRegistry()

    /**
     * 包含了虚拟的 [NekoItem].
     * 这些物品类型 *不应该* 用来生成 [ItemStack].
     */
    @JvmField
    val IMAGINARY: Registry<Key, NekoItem> = SimpleRegistry()

    /**
     * 用于在忽略命名空间的前提下, 模糊查找具有相同路径的 [NekoItem].
     * 这些物品类型可以用来生成 [ItemStack].
     */
    @JvmField
    val CUSTOM_FUZZY: FuzzyRegistry<NekoItem> = FuzzyRegistry()

    /**
     * 所有已加载物品类型的命名空间.
     */
    @get:JvmName("namespaces")
    val NAMESPACES: List<String>
        get() = namespaces

    private val namespaces: ObjectArrayList<String> = ObjectArrayList()

    /**
     * All paths of each available namespace.
     */
    @get:JvmName("pathsByNamespace")
    val NAMESPACE_TO_PATHS: Map<String, List<String>>
        get() = namespace2Paths

    private val namespace2Paths: Object2ObjectOpenHashMap<String, ObjectArrayList<String>> = Object2ObjectOpenHashMap()

    @InitFun
    private fun onPreWorld() {
        loadConfiguration()
    }

    @ReloadableFun
    private fun onReload() {
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
     * 默认的 [Error NekoItemHolder][NekoItemHolder].
     */
    @get:JvmName("getErrorNekoItemHolder")
    val ERROR_NEKO_ITEM_HOLDER: NekoItemHolder
        get() = NekoItemHolder.get(ERROR_NEKO_ITEM_ID)

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
        // 清空现有的注册表
        if (!IMAGINARY.isEmpty()) {
            IMAGINARY.clear()
            LOGGER.info("Unregistered all vanilla items.")
        }
        if (!CUSTOM.isEmpty()) {
            CUSTOM.clear()
            CUSTOM_FUZZY.clear()
            namespaces.clear()
            namespace2Paths.clear()
            LOGGER.info("Unregistered all custom items.")
        }

        // 加载所有配置文件
        for ((key, path, node) in NekoItemNodeIterator) {
            val keyNamespace = key.namespace()
            val keyValue = key.value()

            if (keyNamespace == Key.MINECRAFT_NAMESPACE) {
                // Process as vanilla item
                LOGGER.info("Loading vanilla item: '$key'")
                runCatching { NekoItemFactory.createVanilla(key, path, node) }
                    .onSuccess { nekoItem -> IMAGINARY.register(key, nekoItem) }
                    .onFailure { ex -> reportError(key, ex) }
            } else {
                // Process as custom item
                LOGGER.info("Loading custom item: '$key'")
                runCatching { NekoItemFactory.createCustom(key, path, node) }
                    .onSuccess { nekoItem ->
                        CUSTOM.register(key, nekoItem)
                        CUSTOM_FUZZY.register(keyValue, nekoItem)
                        namespaces.add(keyNamespace)
                        namespace2Paths.getOrPut(keyNamespace, ::ObjectArrayList).add(keyValue)
                    }
                    .onFailure { ex -> reportError(key, ex) }
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
        provider { ItemRegistry.CUSTOM.getOrNull(ItemRegistry.ERROR_NEKO_ITEM_ID) }.orElse(DEFAULT_ERROR_NEKO_ITEM)

    @JvmField
    val ERROR_NEKO_STACK_PROVIDER: Provider<NekoStack> =
        ERROR_NEKO_ITEM_PROVIDER.map(NekoItem::realize)
}