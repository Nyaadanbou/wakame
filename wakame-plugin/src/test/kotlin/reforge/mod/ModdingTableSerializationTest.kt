package reforge.mod

import cc.mewcraft.wakame.adventure.adventureModule
import cc.mewcraft.wakame.rarity.rarityModule
import cc.mewcraft.wakame.reforge.mod.ModdingTableSerializer
import cc.mewcraft.wakame.registry.RarityRegistry
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

class ModdingTableSerializationTest : KoinTest {
    companion object {
        @JvmStatic
        @BeforeAll
        fun setup() {
            startKoin {
                modules(
                    testEnv(),
                    adventureModule(),
                    rarityModule(),
                    registryModule(),
                )
            }

            RarityRegistry.onPreWorld()
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
        val tables = ModdingTableSerializer.loadAll()
        for (table in tables) {
            logger.info(table.toString())
        }
    }
}