package cc.mewcraft.wakame.attribute

import cc.mewcraft.lazyconfig.access.ConfigAccess
import cc.mewcraft.wakame.KoishDataPaths
import cc.mewcraft.wakame.config.Configs
import cc.mewcraft.wakame.element.ElementRegistryLoader
import cc.mewcraft.wakame.entity.attribute.AttributeFacadeRegistryLoader
import cc.mewcraft.wakame.registry.BuiltInRegistries
import cc.mewcraft.wakame.util.Identifiers
import cc.mewcraft.wakame.util.test.TestOnly
import cc.mewcraft.wakame.util.test.TestPath
import org.junit.jupiter.api.AfterEach
import kotlin.test.Test
import kotlin.test.fail

/**
 * 用于检查所有属性的配置是否已经出现在 main/attributes.yml 或 test/attributes.yml.
 */
@OptIn(TestOnly::class)
class CheckMissingAttributeConfigTest {
    @AfterEach
    fun afterEach() {
        Configs.cleanup() // 清理所有已缓存的实例
    }

    @Test
    fun `check all attribute config's at main`() {
        KoishDataPaths.initializeForTest(TestPath.MAIN)
        ConfigAccess.setImplementation(Configs)
        checkMissingConfigs()
    }

    @Test
    fun `check all attribute config's at test`() {
        KoishDataPaths.initializeForTest(TestPath.TEST)
        ConfigAccess.setImplementation(Configs)
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