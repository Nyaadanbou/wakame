package station

import cc.mewcraft.wakame.core.ItemXBootstrap
import cc.mewcraft.wakame.station.ExpChoice
import cc.mewcraft.wakame.station.ItemChoice
import cc.mewcraft.wakame.station.ItemResult
import cc.mewcraft.wakame.station.StationRecipeRegistry
import cc.mewcraft.wakame.util.Key
import core.ItemXMock
import net.kyori.adventure.key.Key
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.inject
import org.slf4j.Logger
import testEnv
import kotlin.test.*

class StationSerializationTest : KoinTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun setup() {
            startKoin {
                modules(
                    testEnv(),
                )
            }

            ItemXBootstrap.init()
            StationRecipeRegistry.loadConfig()
        }

        @JvmStatic
        @AfterAll
        fun shutdown() {
            stopKoin()
        }
    }

    private val logger: Logger by inject()
    private lateinit var key: Key

    @BeforeTest
    fun beforeEach() {

    }

    @AfterTest
    fun afterTest() {
        logger.info(key.asString())
    }

    @Test
    fun `station recipe serialization`() {
        key = Key("test_station:raw_bronze")

        val recipe = StationRecipeRegistry.raw[key]
        assertNotNull(recipe)

        val input = recipe.input
        assertContentEquals(
            listOf(
                ItemChoice(ItemXMock("minecraft:raw_copper"), 3),
                ItemChoice(ItemXMock("wakame:material/raw_tin"), 1),
                ExpChoice(495F)
            ), input
        )

        val output = recipe.output
        assertContentEquals(
            listOf(
                ItemResult(ItemXMock("wakame:material/raw_bronze"), 4)
            ), output
        )
    }
}