package cc.mewcraft.wakame.item2

import cc.mewcraft.wakame.KoishDataPaths
import cc.mewcraft.wakame.commonEnv
import cc.mewcraft.wakame.config.Configs
import cc.mewcraft.wakame.config.node
import cc.mewcraft.wakame.mainEnv
import cc.mewcraft.wakame.registry2.BuiltInRegistries
import cc.mewcraft.wakame.testEnv
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.spongepowered.configurate.kotlin.extensions.contains
import kotlin.test.Test
import kotlin.test.fail

/**
 * 用于检查所有物品组件的配置是否已经出现在 main/items.yml 或 test/items.yml.
 */
class CheckMissingComponentConfigTest : KoinTest {
    companion object {
        const val CONFIG_ID = "cc/mewcraft/wakame/mewcraft/wakame/item2"
        const val NODE_COMPONENTS = "components"
    }

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
        KoishDataPaths.initialize()
        checkMissingConfigs()
    }

    // 检查所有物品组件类型的配置文件已经出现在 test/items.yml
    @Test
    fun `check all item component config's at test`() {
        loadKoinModules(testEnv())
        KoishDataPaths.initialize()
        checkMissingConfigs()
    }

    // 用于检查配置文件有缺失的物品组件类型
    private fun checkMissingConfigs() {
        // ConfigurationNode: `components`.
        // 必须完全重新构建 ConfigProvider 的实例,
        // 否则会一直使用 main/test 之一的配置文件
        val config = Configs[CONFIG_ID].node(NODE_COMPONENTS)
        // 所有已注册的物品组件类型
        val types = BuiltInRegistries.ITEM_DATA_TYPE
        // 未在配置文件中的物品组件类型
        val missingConfigs = mutableListOf<String>()

        for (type in types) {
            val typeId = BuiltInRegistries.ITEM_DATA_TYPE.getKey(type)!!.value.value()
            if (!config.get().contains(typeId)) {
                missingConfigs.add(typeId)
            }
        }

        if (missingConfigs.isNotEmpty()) {
            fail("Missing item component configs for: ${missingConfigs.joinToString(", ")}")
        }
    }
}