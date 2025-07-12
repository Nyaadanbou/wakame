package cc.mewcraft.wakame.reforge.merge

import cc.mewcraft.wakame.KoishDataPaths
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.util.test.TestOnly
import cc.mewcraft.wakame.util.test.TestPath
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class MergingTableSerializationTest {
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
        val tables = MergingTableSerializer.loadAll()
        for (table in tables) {
            LOGGER.info(table.toString())
        }
    }
}