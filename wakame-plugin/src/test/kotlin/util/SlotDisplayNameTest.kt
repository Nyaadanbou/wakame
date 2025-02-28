package util

import cc.mewcraft.wakame.adventure.adventureModule
import cc.mewcraft.wakame.util.SlotDisplayNameData
import commonEnv
import net.kyori.adventure.text.Component.text
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SlotDisplayNameTest : KoinTest {
    @BeforeEach
    fun beforeEach() {
        startKoin {
            modules(
                commonEnv(),
                adventureModule(),
            )
        }
    }

    @AfterEach
    fun afterEach() {
        stopKoin()
    }

    @Test
    fun `simple case 1`() {
        val name = SlotDisplayNameData("<foo>")
        val actual = name.resolve { unparsed("foo", "eee") }
        val expected = text("eee")
        assertEquals(expected, actual)
    }
}
