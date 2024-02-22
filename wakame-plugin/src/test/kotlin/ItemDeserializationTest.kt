import cc.mewcraft.wakame.element.elementModule
import cc.mewcraft.wakame.item.itemModule
import cc.mewcraft.wakame.kizami.kizamiModule
import cc.mewcraft.wakame.rarity.rarityModule
import cc.mewcraft.wakame.reference.referenceModule
import cc.mewcraft.wakame.registry.*
import cc.mewcraft.wakame.skin.skinModule
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import kotlin.test.Test
import kotlin.test.assertNotNull

class ItemDeserializationTest : KoinTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun setup() {
            startKoin {
                modules(testEnvironment())

                // this module
                modules(itemModule())

                // dependencies
                modules(
                    elementModule(),
                    kizamiModule(),
                    referenceModule(),
                    registryModule(),
                    rarityModule(),
                    skinModule()
                )
            }

            // initialize attribute facades
            AttributeRegistry.onPreWorld()

            // initialize necessary registry
            ElementRegistry.onPreWorld()
            KizamiRegistry.onPreWorld()
            RarityRegistry.onPreWorld()
            LevelMappingRegistry.onPreWorld()
            NekoItemRegistry.onPreWorld()
        }

        @JvmStatic
        @AfterAll
        fun shutdown() {
            stopKoin()
        }
    }

    @Test
    fun `item deserialization test`() {
        val demo = NekoItemRegistry.get("short_sword:demo")
        assertNotNull(demo)
    }

    // we can't do the test as it requires server runtime environment
    fun `item generation test`() {
        val demo = NekoItemRegistry.get("short_sword:demo")
        assertNotNull(demo)
        demo.createItemStack(null)
    }
}