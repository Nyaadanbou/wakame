package util

import cc.mewcraft.wakame.adventure.adventureModule
import cc.mewcraft.wakame.util.menuLore
import commonEnv
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import kotlin.test.Test
import kotlin.test.assertEquals

class MenuLoreTest : KoinTest {
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
    fun `simple case`() {
        // Define a MenuLore instance
        val lore = menuLore {
            standard("welcome to <world_name>!")
            folded("choice_list", "*prefix* <choice_list>")
            standard("----")
        }

        // Resolve the MenuLore
        val resolvedComponents = lore.resolve {
            standard(Placeholder.component("world_name", text("overworld")))
            folded("choice_list", listOf(text("choice 1"), text("choice 2"), text("choice 3")))
        }

        // Expected components after resolving
        val expected = listOf(
            text("welcome to overworld!"),
            text("*prefix* choice 1"),
            text("*prefix* choice 2"),
            text("*prefix* choice 3"),
            text("----"),
        )

        // Verify results
        assertEquals(expected.size, resolvedComponents.size)
        resolvedComponents.forEachIndexed { index, component ->
            assertEquals(expected[index], component)
        }
    }
}
