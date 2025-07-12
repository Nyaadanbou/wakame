package cc.mewcraft.wakame.util

import net.kyori.adventure.text.Component.text
import kotlin.test.Test
import kotlin.test.assertEquals

class SlotDisplayNameTest {
    @Test
    fun `simple case 1`() {
        val name = SlotDisplayNameData("<foo>")
        val actual = name.resolve { unparsed("foo", "eee") }
        val expected = text("eee")
        assertEquals(expected, actual)
    }
}
