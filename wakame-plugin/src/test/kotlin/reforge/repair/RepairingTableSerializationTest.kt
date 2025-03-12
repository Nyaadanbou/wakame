package reforge.repair

import cc.mewcraft.wakame.KoishDataPaths
import cc.mewcraft.wakame.adventure.adventureModule
import cc.mewcraft.wakame.reforge.repair.RepairingTableSerializer
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import testEnv
import kotlin.test.Test

class RepairingTableSerializationTest : KoinTest {
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
        val items = RepairingTableSerializer.loadAllItems()
        val tables = RepairingTableSerializer.loadAllTables()
        items.forEach(::println)
        tables.forEach(::println)
    }
}