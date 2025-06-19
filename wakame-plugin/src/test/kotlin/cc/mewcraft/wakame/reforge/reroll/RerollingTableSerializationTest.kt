package cc.mewcraft.wakame.reforge.reroll

import cc.mewcraft.wakame.KoishDataPaths
import cc.mewcraft.wakame.testEnv
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.inject
import org.slf4j.Logger
import kotlin.collections.iterator
import kotlin.test.Test

class RerollingTableSerializationTest : KoinTest {
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

    private val logger by inject<Logger>()

    @Test
    fun `test serialization`() {
        val tables = RerollingTableSerializer.loadAll()
        for (table in tables) {
            logger.info(table.toString())
        }
    }
}