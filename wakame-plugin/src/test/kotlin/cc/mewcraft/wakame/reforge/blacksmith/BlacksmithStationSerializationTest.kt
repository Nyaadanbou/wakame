package cc.mewcraft.wakame.reforge.blacksmith

import cc.mewcraft.wakame.KoishDataPaths
import cc.mewcraft.wakame.reforge.recycle.RecyclingStationRegistry
import cc.mewcraft.wakame.reforge.repair.RepairingTableRegistry
import cc.mewcraft.wakame.util.test.TestOnly
import cc.mewcraft.wakame.util.test.TestPath
import org.junit.jupiter.api.BeforeAll
import kotlin.test.Test

class BlacksmithStationSerializationTest {
    companion object {
        @OptIn(TestOnly::class)
        @JvmStatic
        @BeforeAll
        fun setup() {
            KoishDataPaths.initializeForTest(TestPath.TEST)
        }
    }

    @Test
    fun `test serialization`() {
        // 载入依赖的实例
        RepairingTableRegistry.load()
        RecyclingStationRegistry.load()

        // 载入 Blacksmith!
        BlacksmithStationSerializer.loadAllStations().forEach(::println)
    }
}