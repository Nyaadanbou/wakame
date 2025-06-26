package cc.mewcraft.wakame.reforge.recycle

import cc.mewcraft.wakame.KoishDataPaths
import cc.mewcraft.wakame.util.test.TestOnly
import cc.mewcraft.wakame.util.test.TestPath
import org.junit.jupiter.api.BeforeAll
import kotlin.test.Test

class RecyclingStationSerializationTest {
    companion object {
        @OptIn(TestOnly::class)
        @JvmStatic
        @BeforeAll
        fun setup() {
            KoishDataPaths.initializeForTest(TestPath.TEST)
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