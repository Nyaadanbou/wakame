import cc.mewcraft.wakame.lookup.ItemModelDataLookup
import cc.mewcraft.wakame.lookup.lookupModule
import cc.mewcraft.wakame.registry.ItemRegistry
import cc.mewcraft.wakame.registry.ItemRegistry.get
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.koin.core.component.get
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import kotlin.test.assertEquals

class ItemModelDataLookupTest : KoinTest {
    companion object {
        @JvmStatic
        @BeforeAll
        fun setup() {
            startKoin {
                modules(
                    testEnvironment()
                )

                // this module
                modules(
                    // itemModule()
                )

                // dependencies
                modules(
                    lookupModule()
                )
            }
        }

        @JvmStatic
        @AfterAll
        fun shutdown() {
            stopKoin()
        }
    }

    @Test
    fun `test get item model data`() {
        val itemModelDataLookup = get<ItemModelDataLookup>()
        itemModelDataLookup.onPrePack()
        val demoItem = ItemRegistry.INSTANCES.get("short_sword:demo")
        val itemModelData = itemModelDataLookup[demoItem.key, 0]

        assertEquals(10000, itemModelData)
    }
}