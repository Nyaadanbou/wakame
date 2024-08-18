package station

import cc.mewcraft.wakame.core.ItemXBootstrap
import cc.mewcraft.wakame.eventbus.PluginEventBus
import cc.mewcraft.wakame.station.*
import cc.mewcraft.wakame.util.Key
import core.ItemXMock
import kotlinx.coroutines.runBlocking
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
        }

        @JvmStatic
        @AfterAll
        fun shutdown() {
            stopKoin()
        }
    }

    private val logger: Logger by inject()

//    @BeforeTest
//    fun beforeEach() {
//
//    }
//
//    @AfterTest
//    fun afterTest() {
//
//    }

    @Test
    fun `simple station serialization`() = runBlocking {
        StationRecipeRegistry.loadConfig() // 单元测试时跳过合成站配方的有效性验证
        PluginEventBus.get().post(StationRecipeLoadEvent)

        val key1 = Key("test_station:raw_bronze")

        val recipe1 = StationRecipeRegistry.raw[key1]
        assertNotNull(recipe1)

        val input1 = recipe1.input
        assertContentEquals(
            listOf(
                ItemChoice(ItemXMock("minecraft:raw_copper"), 3),
                ItemChoice(ItemXMock("wakame:material/raw_tin"), 1),
                ExpChoice(495)
            ), input1
        )

        val output1 = recipe1.output
        assertEquals(ItemResult(ItemXMock("wakame:material/raw_bronze"), 4), output1)


        val key2 = Key("test_station:amethyst_dust")

        val recipe2 = StationRecipeRegistry.raw[key2]
        assertNotNull(recipe2)

        val input2 = recipe2.input
        assertContentEquals(
            listOf(
                ItemChoice(ItemXMock("minecraft:amethyst_shard"), 1),
            ), input2
        )

        val output2 = recipe2.output
        assertEquals(ItemResult(ItemXMock("wakame:material/amethyst_dust"), 2), output2)


        val id = "simple_station"

        val station = StationRegistry.find(id)
        assertNotNull(station)
        assertIs<SimpleStation>(station)
        assertContentEquals(
            listOf(
                recipe1,
                recipe2
            ), station
        )
    }
}