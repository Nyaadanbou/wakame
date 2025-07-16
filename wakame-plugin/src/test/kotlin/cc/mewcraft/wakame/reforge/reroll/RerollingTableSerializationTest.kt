package cc.mewcraft.wakame.reforge.reroll

import cc.mewcraft.wakame.KoishDataPaths
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.util.test.TestOnly
import cc.mewcraft.wakame.util.test.TestPath
import org.junit.jupiter.api.BeforeAll
import kotlin.test.Test

class RerollingTableSerializationTest {
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
        val tables = RerollingTableSerializer.loadAll()
        for (table in tables) {
            LOGGER.info(table.toString())
        }
    }
}