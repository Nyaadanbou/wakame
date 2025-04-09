package station

import cc.mewcraft.wakame.KoishDataPaths
import cc.mewcraft.wakame.core.ItemXBootstrap
import cc.mewcraft.wakame.craftingstation.CraftingStationRecipeRegistry
import cc.mewcraft.wakame.craftingstation.CraftingStationRegistry
import cc.mewcraft.wakame.craftingstation.SimpleCraftingStation
import cc.mewcraft.wakame.craftingstation.recipe.ExpChoice
import cc.mewcraft.wakame.craftingstation.recipe.ItemChoice
import cc.mewcraft.wakame.craftingstation.recipe.ItemResult
import core.ItemXMock
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.key.Key
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
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

            KoishDataPaths.initialize()

            ItemXBootstrap.init()
        }

        @JvmStatic
        @AfterAll
        fun shutdown() {
            stopKoin()
        }
    }

    @Test
    fun `simple station serialization`() = runBlocking {
        CraftingStationRecipeRegistry.loadDataIntoRegistry() // 单元测试时跳过合成站配方的有效性验证
        CraftingStationRegistry.loadDataIntoRegistry()

        val key1 = Key.key("test:raw_bronze")

        val recipe1 = CraftingStationRecipeRegistry.raw[key1]
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


        val key2 = Key.key("test:amethyst_dust")

        val recipe2 = CraftingStationRecipeRegistry.raw[key2]
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

        val station = CraftingStationRegistry.get(id)
        assertNotNull(station)
        assertIs<SimpleCraftingStation>(station)
        assertContentEquals(
            listOf(
                recipe1,
                recipe2
            ), station
        )
    }
}