package cc.mewcraft.wakame.util

import cc.mewcraft.lazyconfig.configurate.register
import cc.mewcraft.lazyconfig.configurate.require
import net.kyori.adventure.text.Component.text
import org.spongepowered.configurate.BasicConfigurationNode
import org.spongepowered.configurate.ConfigurationOptions
import kotlin.test.Test
import kotlin.test.assertEquals

class ItemSlotDisplayNameTest {
    @Test
    fun `simple case 1`() {
        // Define a MenuLore instance
        val lore = SlotDisplayLoreData.configureAndBuild {
            standard("welcome to <world_name>!")
            folded("choice_list", "*prefix* <choice_list>")
            standard("a bottom line")
        }

        // Resolve the MenuLore
        val actual = lore.resolve {
            standard { component("world_name", text("overworld")) }
            folded("choice_list", listOf(text("choice 1"), text("choice 2"), text("choice 3")))
        }

        // Expected components after resolving
        val expected = listOf(
            text("welcome to overworld!"),
            text("*prefix* choice 1"),
            text("*prefix* choice 2"),
            text("*prefix* choice 3"),
            text("a bottom line"),
        )

        // Verify results
        assertEquals(expected.size, actual.size)
        actual.forEachIndexed { index, component ->
            assertEquals(expected[index], component)
        }
    }

    @Test
    fun `simple case 2`() {
        // Define a MenuLore instance
        val lore = SlotDisplayLoreData.configureAndBuild {
            standard("this is <landmark> made by <author>!")
            folded("member_list", "*prefix* <member_list>")
            standard("a bottom line")
        }

        // Resolve the MenuLore
        val actual = lore.resolve {
            standard {
                unparsed("landmark", "foo")
                unparsed("author", "bar")
            }
            folded("member_list", text("member 1"), text("member 2"), text("member 3"))
        }

        // Expected components after resolving
        val expected = listOf(
            text("this is foo made by bar!"),
            text("*prefix* member 1"),
            text("*prefix* member 2"),
            text("*prefix* member 3"),
            text("a bottom line"),
        )

        // Verify results
        assertEquals(expected.size, actual.size)
        actual.forEachIndexed { index, component ->
            assertEquals(expected[index], component)
        }
    }

    @Test
    fun `serialization case 1`() {
        // Deserialize a configuration node
        val node = BasicConfigurationNode.root<RuntimeException>(ConfigurationOptions.defaults().serializers {
            it.register<SlotDisplayLoreData>(SlotDisplayLoreDataSerializer)
        }) { node ->
            node.set(
                listOf(
                    "welcome to <world_name>!",
                    "*prefix* {choice_list} <choice_mark>",
                    "a bottom line",
                )
            )
        }

        val lore = node.require<SlotDisplayLoreData>()

        // Resolve the MenuLore
        val actual = lore.resolve {
            standard {
                component("world_name", text("overworld"))
                unparsed("choice_mark", "x")
            }
            folded("choice_list", listOf(text("choice 1"), text("choice 2"), text("choice 3")))
        }

        // Expected components after resolving
        val expected = listOf(
            text("welcome to overworld!"),
            text("*prefix* choice 1 x"),
            text("*prefix* choice 2 x"),
            text("*prefix* choice 3 x"),
            text("a bottom line"),
        )

        // Verify results
        assertEquals(expected.size, actual.size)
        actual.forEachIndexed { index, component ->
            assertEquals(expected[index], component)
        }
    }
}
