package display2

import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import cc.mewcraft.wakame.adventure.adventureModule
import cc.mewcraft.wakame.display2.ItemRenderer
import cc.mewcraft.wakame.display2.implementation.*
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.qualifier.named
import org.koin.test.KoinTest
import org.koin.test.get
import testEnv
import java.nio.file.Path
import kotlin.test.Test

class RendererFormatSerializationTest : KoinTest {
    companion object {
        @JvmStatic
        @BeforeAll
        fun setup() {
            startKoin {
                modules(
                    testEnv()
                )

                // this module
                modules(

                )

                // dependencies
                modules(
                    adventureModule()
                )
            }
        }

        @JvmStatic
        @AfterAll
        fun shutdown() {
            stopKoin()
        }

        private const val LAYOUT_FILE = "layout.yml"
        private const val FORMATS_FILE = "formats.yml"

        private const val STANDARD = "standard"
        private const val CRAFTING_STATION = "crafting_station"
        private const val MERGING_TABLE = "merging_table"
        private const val MODDING_TABLE = "modding_table"
        private const val RECYCLING_STATION = "recycling_station"
        private const val REROLLING_TABLE = "rerolling_table"
    }

    private val renderersDirectory = get<Path>(named(PLUGIN_DATA_DIR)).resolve("renderers")

    @Test
    fun `deserialize renderer format 1`() {
        initialize(StandardItemRenderer, STANDARD)
        initialize(CraftingStationItemRenderer, CRAFTING_STATION)
        initialize(MergingTableItemRenderer, MERGING_TABLE)
        initialize(ModdingTableItemRenderer, MODDING_TABLE)
        initialize(RecyclingStationItemRenderer, RECYCLING_STATION)
        initialize(RerollingTableItemRenderer, REROLLING_TABLE)
    }

    private fun initialize(renderer: ItemRenderer<*, *>, id: String) {
        renderer.initialize(
            layoutPath = renderersDirectory.resolve(id).resolve(LAYOUT_FILE),
            formatPath = renderersDirectory.resolve(id).resolve(FORMATS_FILE)
        )
    }
}

private data class Foo(val a: Int, val b: String)