package cc.mewcraft.wakame.display2

import cc.mewcraft.wakame.KoishDataPaths
import cc.mewcraft.wakame.util.test.TestOnly
import cc.mewcraft.wakame.util.test.TestPath
import org.junit.jupiter.api.BeforeAll
import kotlin.test.Test

class ItemRendererInitTest {
    companion object {
        @OptIn(TestOnly::class)
        @JvmStatic
        @BeforeAll
        fun setup() {
            KoishDataPaths.initializeForTest(TestPath.TEST)
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
