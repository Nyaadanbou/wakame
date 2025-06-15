package display2

import cc.mewcraft.wakame.KoishDataPaths
import cc.mewcraft.wakame.display2.ItemRenderers
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import testEnv
import kotlin.test.Test

class ItemRendererInitTest : KoinTest {
    companion object {
        @JvmStatic
        @BeforeAll
        fun setup() {
            startKoin {
                modules(
                    testEnv()
                )

                // this module
                modules(

                )
            }

            KoishDataPaths.initialize()
        }

        @JvmStatic
        @AfterAll
        fun shutdown() {
            stopKoin()
        }
    }

    @Test
    fun `initialize item renderers`() {
        ItemRenderers.SIMPLE.loadDataFromConfigs()
        ItemRenderers.STANDARD.loadDataFromConfigs()
        ItemRenderers.CRAFTING_STATION.loadDataFromConfigs()
        ItemRenderers.MERGING_TABLE.loadDataFromConfigs()
        ItemRenderers.MODDING_TABLE.loadDataFromConfigs()
        ItemRenderers.REROLLING_TABLE.loadDataFromConfigs()
        ItemRenderers.REPAIRING_TABLE.loadDataFromConfigs()
    }
}
