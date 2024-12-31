package display2

import cc.mewcraft.wakame.adventure.adventureModule
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

                // dependencies
                modules(
                    adventureModule()
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
    fun `initialize item renderers`() {
        ItemRenderers.SIMPLE.initialize0()
        ItemRenderers.STANDARD.initialize0()
        ItemRenderers.CRAFTING_STATION.initialize0()
        ItemRenderers.MERGING_TABLE.initialize0()
        ItemRenderers.MODDING_TABLE.initialize0()
        ItemRenderers.REROLLING_TABLE.initialize0()
        ItemRenderers.REPAIRING_TABLE.initialize0()
    }
}
