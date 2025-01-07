package attribute

import cc.mewcraft.wakame.adventure.adventureModule
import cc.mewcraft.wakame.attribute.attributeModule
import cc.mewcraft.wakame.config.Configs
import cc.mewcraft.wakame.element.ElementRegistryConfigStorage
import cc.mewcraft.wakame.registry.ATTRIBUTE_GLOBAL_CONFIG_FILE
import cc.mewcraft.wakame.registry.AttributeRegistry
import cc.mewcraft.wakame.registry.registryModule
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
 * 用于检查所有属性的配置是否已经出现在 main/attributes.yml 或 test/attributes.yml.
 */
class CheckMissingAttributeConfigTest : KoinTest {
    @BeforeEach
    fun beforeEach() {
        startKoin {
            modules(
                adventureModule(),
                attributeModule(),
                registryModule(),
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
        checkMissingConfigs()
    }

    @Test
    fun `check all attribute config's at test`() {
        loadKoinModules(testEnv())
        checkMissingConfigs()
    }

    private fun checkMissingConfigs() {
        ElementRegistryConfigStorage.init()
        AttributeRegistry.init()

        val config = Configs.YAML[ATTRIBUTE_GLOBAL_CONFIG_FILE]

        val missingConfigs = mutableListOf<String>()

        for ((key, _) in AttributeRegistry.FACADES) {
            if (!config.get().contains(key)) {
                missingConfigs.add(key)
            }
        }

        if (missingConfigs.isNotEmpty()) {
            fail("Missing attribute configs for: ${missingConfigs.joinToString(", ")}")
        }
    }
}