package cc.mewcraft.wakame.reforge.blacksmith

import cc.mewcraft.wakame.KoishDataPaths
import cc.mewcraft.wakame.reforge.recycle.RecyclingStationRegistry
import cc.mewcraft.wakame.reforge.repair.RepairingTableRegistry
import cc.mewcraft.wakame.testEnv
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import kotlin.test.Test

class BlacksmithStationSerializationTest : KoinTest {
    companion object {
        @JvmStatic
        @BeforeAll
        fun setup() {
            startKoin {
                modules(
                    testEnv(),
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
    fun `test serialization`() {
        // 载入依赖的实例
        RepairingTableRegistry.load()
        RecyclingStationRegistry.load()

        // 载入 Blacksmith!
        BlacksmithStationSerializer.loadAllStations().forEach(::println)
    }
}