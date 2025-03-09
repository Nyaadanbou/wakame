package reforge.merge

import cc.mewcraft.wakame.adventure.adventureModule
import cc.mewcraft.wakame.reforge.merge.MergingTableSerializer
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.inject
import org.slf4j.Logger
import testEnv

class MergingTableSerializationTest : KoinTest {
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
        }

        @JvmStatic
        @AfterAll
        fun shutdown() {
            stopKoin()
        }
    }

    private val logger by inject<Logger>()

    @Test
    fun `test serialization`() {
        val tables = MergingTableSerializer.loadAll()
        for (table in tables) {
            logger.info(table.toString())
        }
    }
}