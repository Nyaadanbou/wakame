package cc.mewcraft.wakame.reforge.repair

import cc.mewcraft.wakame.KoishDataPaths
import cc.mewcraft.wakame.util.test.TestOnly
import cc.mewcraft.wakame.util.test.TestPath
import org.junit.jupiter.api.BeforeAll
import kotlin.test.Test

class RepairingTableSerializationTest {
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
        val items = RepairingTableSerializer.loadAllItems()
        val tables = RepairingTableSerializer.loadAllTables()
        items.forEach(::println)
        tables.forEach(::println)
    }
}