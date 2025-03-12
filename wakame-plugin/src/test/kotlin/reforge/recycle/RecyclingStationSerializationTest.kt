package reforge.recycle

import cc.mewcraft.wakame.KoishDataPaths
import cc.mewcraft.wakame.adventure.adventureModule
import cc.mewcraft.wakame.reforge.recycle.RecyclingStationSerializer
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import testEnv
import kotlin.test.Test

class RecyclingStationSerializationTest : KoinTest {
    companion object {
        @JvmStatic
        @BeforeAll
        fun setup() {
            startKoin {
                modules(
                    testEnv(),
                    adventureModule(),
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
        val items = RecyclingStationSerializer.loadAllItems()
        val tables = RecyclingStationSerializer.loadAllStations()
        items.forEach(::println)
        tables.forEach(::println)
    }
}