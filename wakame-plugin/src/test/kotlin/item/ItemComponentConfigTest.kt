package item

import cc.mewcraft.wakame.config.Configs
import cc.mewcraft.wakame.config.derive
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.registry.ITEM_GLOBAL_CONFIG_FILE
import cc.mewcraft.wakame.registry.ItemComponentRegistry
import commonEnv
import mainEnv
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.spongepowered.configurate.kotlin.extensions.contains
import testEnv
import kotlin.test.Test
import kotlin.test.fail

/**
 * 用于检查所有物品组件的配置是否已经出现在 main/items.yml 或 test/items.yml.
 */
class ItemComponentConfigTest : KoinTest {
    @BeforeEach
    fun beforeEach() {
        startKoin {
            modules(
                commonEnv(),
            )
        }
    }

    @AfterEach
    fun afterEach() {
        Configs.cleanup() // 清理所有已缓存的实例
        stopKoin()
    }

    // 检查所有物品组件类型的配置文件已经出现在 main/items.yml
    @Test
    fun `check all item component config's at main`() {
        loadKoinModules(mainEnv())
        checkMissingConfigs()
    }

    // 检查所有物品组件类型的配置文件已经出现在 test/items.yml
    @Test
    fun `check all item component config's at test`() {
        loadKoinModules(testEnv())
        checkMissingConfigs()
    }

    // 用于检查配置文件有缺失的物品组件类型
    private fun checkMissingConfigs() {
        ItemComponentTypes // 初始化物品组件类型

        // ConfigurationNode: `components`.
        // 必须完全重新构建 ConfigProvider 的实例,
        // 否则会一直使用 main/test 之一的配置文件
        val config = Configs.YAML[ITEM_GLOBAL_CONFIG_FILE].derive(ItemComponentRegistry.NODE_COMPONENTS)
        // 所有已注册的物品组件类型
        val types = ItemComponentRegistry.TYPES
        // 未在配置文件中的物品组件类型
        val missingConfigs = mutableListOf<String>()

        for ((_, type) in types) {
            if (!config.get().contains(type.id)) {
                missingConfigs.add(type.id)
            }
        }

        if (missingConfigs.isNotEmpty()) {
            fail("Missing item component configs for: ${missingConfigs.joinToString(", ")}")
        }
    }
}