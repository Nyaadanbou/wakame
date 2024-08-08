package reforge.reroll

import cc.mewcraft.wakame.adventure.adventureModule
import cc.mewcraft.wakame.reforge.reroll.RerollingTableSerializer
import cc.mewcraft.wakame.registry.registryModule
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.inject
import org.slf4j.Logger
import testEnv
import kotlin.test.Test

class RerollingTableSerializationTest : KoinTest {
    companion object {
        @JvmStatic
        @BeforeAll
        fun setup() {
            startKoin {
                modules(
                    testEnv(),
                    adventureModule(),
                    registryModule(),
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
        val tables = RerollingTableSerializer.loadAll()
        for (table in tables) {
            logger.info(table.toString())
        }
    }
}