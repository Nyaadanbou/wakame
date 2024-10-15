package display2

import cc.mewcraft.wakame.adventure.adventureModule
import cc.mewcraft.wakame.display2.implementation.*
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
        StandardItemRenderer.initialize0()
        CraftingStationItemRenderer.initialize0()
        MergingTableItemRenderer.initialize0()
        ModdingTableItemRenderer.initialize0()
        RecyclingStationItemRenderer.initialize0()
        RerollingTableItemRenderer.initialize0()
    }
}
