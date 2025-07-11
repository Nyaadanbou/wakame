package cc.mewcraft.wakame.attribute

import cc.mewcraft.wakame.KoishDataPaths
import cc.mewcraft.wakame.config.Configs
import cc.mewcraft.wakame.element.ElementRegistryLoader
import cc.mewcraft.wakame.entity.attribute.AttributeFacadeRegistryLoader
import cc.mewcraft.wakame.mainEnv
import cc.mewcraft.wakame.registry2.BuiltInRegistries
import cc.mewcraft.wakame.testEnv
import cc.mewcraft.wakame.util.Identifiers
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import kotlin.test.Test
import kotlin.test.fail

/**
 * 用于检查所有属性的配置是否已经出现在 main/attributes.yml 或 test/attributes.yml.
 */
class CheckMissingAttributeConfigTest : KoinTest {
    @BeforeEach
    fun beforeEach() {
        startKoin {
            modules(

            )
        }
    }

    @AfterEach
    fun afterEach() {
        Configs.cleanup() // 清理所有已缓存的实例
        stopKoin()
    }

    @Test
    fun `check all attribute config's at main`() {
        loadKoinModules(mainEnv())
        KoishDataPaths.initialize()
        checkMissingConfigs()
    }

    @Test
    fun `check all attribute config's at test`() {
        loadKoinModules(testEnv())
        KoishDataPaths.initialize()
        checkMissingConfigs()
    }

    private fun checkMissingConfigs() {
        ElementRegistryLoader.init()
        AttributeFacadeRegistryLoader.init()

        val config = Configs["attributes"]

        val rootNode = config.get()
        val idsPresentInRegistry = BuiltInRegistries.ATTRIBUTE_FACADE.ids
        val idsPresentInConfig = rootNode.childrenMap().keys.map(Any::toString).map(Identifiers::of)
        val missingIdsInConfig = idsPresentInRegistry subtract idsPresentInConfig.toSet()

        if (missingIdsInConfig.isNotEmpty()) {
            fail("Missing attribute configs for: ${missingIdsInConfig.joinToString(", ")}")
        }
    }
}