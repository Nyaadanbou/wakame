package item

import cc.mewcraft.wakame.InjectionQualifier
import cc.mewcraft.wakame.KoishDataPaths
import cc.mewcraft.wakame.ability.AbilityRegistryLoader
import cc.mewcraft.wakame.adventure.adventureModule
import cc.mewcraft.wakame.element.ElementTypeRegistryLoader
import cc.mewcraft.wakame.entity.attribute.AttributeBundleFacadeRegistryLoader
import cc.mewcraft.wakame.entity.typeholder.EntityTypeHolderRegistryLoader
import cc.mewcraft.wakame.item.ItemTypeRegistryLoader
import cc.mewcraft.wakame.item.NekoItem
import cc.mewcraft.wakame.item.NekoItemFactory
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.template.*
import cc.mewcraft.wakame.item.templates.components.ElementSampleNodeFacade
import cc.mewcraft.wakame.item.templates.components.KizamiSampleNodeFacade
import cc.mewcraft.wakame.item.templates.components.cells.CoreArchetypeSampleNodeFacade
import cc.mewcraft.wakame.item.templates.filters.ItemFilterNodeFacade
import cc.mewcraft.wakame.kizami.KizamiTypeRegistryLoader
import cc.mewcraft.wakame.rarity.LevelRarityMappingRegistryLoader
import cc.mewcraft.wakame.rarity.RarityTypeRegistryLoader
import cc.mewcraft.wakame.util.buildYamlConfigLoader
import cc.mewcraft.wakame.world.worldModule
import net.kyori.adventure.key.Key
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.get
import org.koin.test.inject
import org.slf4j.Logger
import org.spongepowered.configurate.ConfigurationNode
import testEnv
import java.nio.file.Path
import kotlin.test.fail

object CommonNekoStackTest {
    fun beforeAll() {
        // 配置依赖注入
        startKoin {
            modules(
                // environment
                testEnv(),

                // dependencies
                adventureModule(),
                worldModule(),
            )
        }

        KoishDataPaths.initialize()

        // 按依赖顺序, 初始化注册表
        ElementTypeRegistryLoader.init()
        AttributeBundleFacadeRegistryLoader.init()
        AbilityRegistryLoader.init()
        KizamiTypeRegistryLoader.init()
        RarityTypeRegistryLoader.init()
        LevelRarityMappingRegistryLoader.init()
        EntityTypeHolderRegistryLoader.init()

        // 初始化所有 random3.Node 相关的实现
        ElementSampleNodeFacade.init()
        KizamiSampleNodeFacade.init()
        CoreArchetypeSampleNodeFacade.init()
        ItemFilterNodeFacade.init()
    }

    fun afterAll() {
        stopKoin()
    }
}

fun KoinTest.readItemNode(namespace: String, path: String): Triple<Key, Path, ConfigurationNode> {
    val configsDir = get<Path>(InjectionQualifier.CONFIGS_FOLDER).toFile()
    val dataDir = configsDir.resolve("item")
    val namespaceDir = dataDir.resolve(namespace)
    val itemFile = namespaceDir.resolve("$path.yml")
    if (!itemFile.exists()) {
        fail("File not found: $namespace:$path")
    }

    val key = Key.key(namespace, path)
    val relPath = itemFile.toPath()
    val loader = buildYamlConfigLoader {
        withDefaults()
        serializers { registerAll(ItemTypeRegistryLoader.SERIALIZERS) }
    }
    val rootNode = loader.buildAndLoadString(itemFile.readText())
    return Triple(key, relPath, rootNode)
}

/**
 * 从指定的文件读取 [NekoItem]. 需要先加载好依赖, 否则会抛出异常!
 */
fun KoinTest.readVanillaPrototype(path: String): NekoItem {
    val (key, relPath, node) = readItemNode("minecraft", path)
    return NekoItemFactory.VANILLA(key, node)
}

/**
 * 从指定的文件读取 [NekoItem]. 需要先加载好依赖, 否则会抛出异常!
 */
fun KoinTest.readCustomPrototype(namespace: String, path: String): NekoItem {
    val (key, relPath, node) = readItemNode(namespace, path)
    return NekoItemFactory.STANDARD(key, node)
}

/**
 * 一个适用于任何物品组件的测试流程.
 */
class ItemComponentLifecycleTest<T, S : ItemTemplate<T>>(
    val namespace: String,
    val path: String,
    val templateType: ItemTemplateType<S>,
    val componentType: ItemComponentType<T>,
) : KoinTest {

    private val logger: Logger by inject()
    private val lifecycle: LifecycleImpl<T, S> = LifecycleImpl(Key.key(namespace, path))

    /**
     * 配置测试流程.
     */
    fun configure(block: Lifecycle<T, S>.() -> Unit) {
        block(lifecycle)
    }

    /**
     * 开始测试流程.
     */
    fun start() {
        val prototype = readCustomPrototype(namespace, path)
        val nekoStack = MockNekoStack(prototype)
        val template = prototype.templates.get(templateType)

        // 消费 ItemTemplate
        lifecycle.consumeSerialization(template)

        if (template == null) {
            return // 模板为空的话就不需要做接下来的测试了, 直接返回
        }

        // 构建 GenerationContext
        val context = lifecycle.bootstrap.newContext()

        // 消费 GenerationContext
        lifecycle.consumeGenerationContext(context)

        val result = template.generate(context)

        // 消费 GenerationResult
        lifecycle.consumeGenerationResult(result)

        var unboxed: T? = null
        if (componentType != ItemComponentTypes.EMPTY) {
            // 只有当模板拥有对应的组件时, 才会进行下面的测试

            unboxed = result.value
            nekoStack.components.set(componentType, unboxed)
            nekoStack.components.get(componentType) ?: fail("Failed to get the component from the map")
        }

        // 消费 GenerationResult 所封装的值
        unboxed?.let { lifecycle.consumeUnboxed(it) }

        // 打印物品生成的结果
        logger.info("")
        logger.info(prototype.toString())
        logger.info("")
        logger.info(nekoStack.toString())
        logger.info("")
        unboxed?.let { logger.info(it.toString()) }
    }

    interface Lifecycle<T, S : ItemTemplate<T>> {
        fun serialization(block: (S?) -> Unit)
        fun bootstrap(block: LifecycleBootstrap.() -> Unit)
        fun context(block: (ItemGenerationContext) -> Unit)
        fun result(block: (ItemGenerationResult<T>) -> Unit)
        fun unboxed(block: (T) -> Unit)
    }

    interface LifecycleBootstrap {
        fun createContext(block: () -> ItemGenerationContext)
    }

    private class LifecycleImpl<T, S : ItemTemplate<T>>(
        val target: Key,
    ) : Lifecycle<T, S> {
        var bootstrap: LifecycleBootstrapImpl = LifecycleBootstrapImpl(target)
        var consumeGenerationContext: (ItemGenerationContext) -> Unit = {}
        var consumeSerialization: (S?) -> Unit = {}
        var consumeGenerationResult: (ItemGenerationResult<T>) -> Unit = {}
        var consumeUnboxed: (T) -> Unit = {}

        override fun bootstrap(block: LifecycleBootstrap.() -> Unit) {
            block(bootstrap)
        }

        override fun serialization(block: (S?) -> Unit) {
            consumeSerialization = block
        }

        override fun context(block: (ItemGenerationContext) -> Unit) {
            consumeGenerationContext = block
        }

        override fun result(block: (ItemGenerationResult<T>) -> Unit) {
            consumeGenerationResult = block
        }

        override fun unboxed(block: (T) -> Unit) {
            consumeUnboxed = block
        }
    }

    private class LifecycleBootstrapImpl(
        val target: Key,
    ) : LifecycleBootstrap {
        // 初始为默认的 GenerationContext 生成函数
        var newContext: () -> ItemGenerationContext = {
            val target = this.target
            val trigger = ItemGenerationTriggers.direct(10)
            MockGenerationContext.create(target, trigger)
        }

        override fun createContext(block: () -> ItemGenerationContext) {
            newContext = block
        }
    }
}