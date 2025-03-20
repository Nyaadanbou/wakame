package attribute

import cc.mewcraft.wakame.KoishDataPaths
import cc.mewcraft.wakame.adventure.adventureModule
import cc.mewcraft.wakame.config.Configs
import cc.mewcraft.wakame.element.ElementTypeRegistryLoader
import cc.mewcraft.wakame.entity.attribute.AttributeBundleFacadeRegistryLoader
import cc.mewcraft.wakame.registry2.KoishRegistries
import cc.mewcraft.wakame.util.Identifiers
import mainEnv
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import testEnv
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
                adventureModule(),
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
        ElementTypeRegistryLoader.init()
        AttributeBundleFacadeRegistryLoader.init()

        val config = Configs[AttributeBundleFacadeRegistryLoader.CONFIG_ID]

        val rootNode = config.get()
        val idsPresentInRegistry = KoishRegistries.ATTRIBUTE_BUNDLE_FACADE.ids
        val idsPresentInConfig = rootNode.childrenMap().keys.map(Any::toString).map(Identifiers::of)
        val missingIdsInConfig = idsPresentInRegistry subtract idsPresentInConfig.toSet()

        if (missingIdsInConfig.isNotEmpty()) {
            fail("Missing attribute configs for: ${missingIdsInConfig.joinToString(", ")}")
        }
    }
}