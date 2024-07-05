import cc.mewcraft.wakame.lookup.ItemModelDataLookup
import cc.mewcraft.wakame.lookup.lookupModule
import cc.mewcraft.wakame.util.Key
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
        val itemModelData1 = itemModelDataLookup[Key("short_sword:demo"), 0]
        val itemModelData2 = itemModelDataLookup[Key("long_sword:demo"), 0]
        val itemModelData3 = itemModelDataLookup[Key("short_sword:demo"), 1]

        assertEquals(10000, itemModelData1)
        assertEquals(10000, itemModelData2)
        assertEquals(10001, itemModelData3)
    }
}