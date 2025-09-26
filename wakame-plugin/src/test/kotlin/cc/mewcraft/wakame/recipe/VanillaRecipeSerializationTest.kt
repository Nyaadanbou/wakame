package cc.mewcraft.wakame.recipe

import cc.mewcraft.wakame.KoishDataPaths
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.core.ItemRefMock
import cc.mewcraft.wakame.item.ItemRef
import cc.mewcraft.wakame.item.ItemRefBootstrap
import cc.mewcraft.wakame.util.Identifier
import cc.mewcraft.wakame.util.test.TestOnly
import cc.mewcraft.wakame.util.test.TestPath
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkObject
import net.kyori.adventure.key.Key
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import kotlin.test.*

class VanillaRecipeSerializationTest {
    companion object {
        @OptIn(TestOnly::class)
        @JvmStatic
        @BeforeAll
        fun setup() {
            KoishDataPaths.initializeForTest(TestPath.TEST)

            mockkObject(ItemRef)
            every { ItemRef.create(any<Identifier>()) } answers { ItemRefMock(firstArg<Identifier>()) }

            ItemRefBootstrap.init()
            MinecraftRecipeRegistryLoader.load()
        }

        @JvmStatic
        @AfterAll
        fun teardown() {
            unmockkObject(ItemRef)
        }
    }

    private lateinit var key: Key

    @AfterTest
    fun afterTest() {
        LOGGER.info(key.asString())
    }

    @Test
    fun `blasting recipe serialization`() {
        key = Key.key("test:blasting")

        val recipe = MinecraftRecipeRegistryLoader.uncheckedRecipes[key]
        assertNotNull(recipe)
        assertIs<BlastingRecipe>(recipe)

        val input = recipe.input
        assertIs<MultiRecipeChoice>(input)
        assertContentEquals(
            expected = listOf(
                ItemRefMock("minecraft:glass"),
                ItemRefMock("minecraft:white_stained_glass"),
                ItemRefMock("minecraft:orange_stained_glass"),
                ItemRefMock("minecraft:magenta_stained_glass"),
                ItemRefMock("minecraft:light_blue_stained_glass"),
                ItemRefMock("minecraft:yellow_stained_glass"),
                ItemRefMock("minecraft:lime_stained_glass"),
                ItemRefMock("minecraft:pink_stained_glass"),
                ItemRefMock("minecraft:gray_stained_glass"),
                ItemRefMock("minecraft:light_gray_stained_glass"),
                ItemRefMock("minecraft:cyan_stained_glass"),
                ItemRefMock("minecraft:purple_stained_glass"),
                ItemRefMock("minecraft:blue_stained_glass"),
                ItemRefMock("minecraft:brown_stained_glass"),
                ItemRefMock("minecraft:green_stained_glass"),
                ItemRefMock("minecraft:red_stained_glass"),
                ItemRefMock("minecraft:black_stained_glass")
            ),
            actual = input.items
        )

        val cookingTime = recipe.cookingTime
        assertEquals(40, cookingTime)

        val exp = recipe.exp
        assertEquals(495F, exp)

        val result = recipe.result
        assertIs<SingleRecipeResult>(result)
        assertEquals(ItemRefMock("minecraft:quartz"), result.item)
        assertEquals(1, result.amount)
    }

    @Test
    fun `campfire recipe serialization`() {
        key = Key.key("test:campfire")

        val recipe = MinecraftRecipeRegistryLoader.uncheckedRecipes[key]
        assertNotNull(recipe)
        assertIs<CampfireRecipe>(recipe)

        val input = recipe.input
        assertIs<SingleRecipeChoice>(input)
        assertEquals(ItemRefMock("minecraft:poisonous_potato"), input.item)

        val cookingTime = recipe.cookingTime
        assertEquals(1, cookingTime)

        val exp = recipe.exp
        assertEquals(1000F, exp)

        val result = recipe.result
        assertIs<SingleRecipeResult>(result)
        assertEquals(ItemRefMock("minecraft:diamond"), result.item)
        assertEquals(1, result.amount)
    }

    @Test
    fun `furnace recipe serialization`() {
        key = Key.key("test:furnace")

        val recipe = MinecraftRecipeRegistryLoader.uncheckedRecipes[key]
        assertNotNull(recipe)
        assertIs<FurnaceRecipe>(recipe)

        val input = recipe.input
        assertIs<SingleRecipeChoice>(input)
        assertEquals(ItemRefMock("minecraft:gravel"), input.item)

        val cookingTime = recipe.cookingTime
        assertEquals(200, cookingTime)

        val exp = recipe.exp
        assertEquals(0F, exp)

        val result = recipe.result
        assertIs<SingleRecipeResult>(result)
        assertEquals(ItemRefMock("minecraft:sand"), result.item)
        assertEquals(1, result.amount)
    }

    @Test
    fun `shaped recipe serialization`() {
        key = Key.key("test:shaped")

        val recipe = MinecraftRecipeRegistryLoader.uncheckedRecipes[key]
        assertNotNull(recipe)
        assertIs<ShapedRecipe>(recipe)

        val pattern = recipe.pattern
        assertEquals("XAX", pattern[0])
        assertEquals("ABA", pattern[1])
        assertEquals("XAX", pattern[2])

        val ingredients = recipe.ingredients
        assertContains(ingredients, 'A')
        assertContains(ingredients, 'B')

        val recipeChoiceA = ingredients['A']
        val recipeChoiceB = ingredients['B']
        assertNotNull(recipeChoiceA)
        assertNotNull(recipeChoiceB)
        assertIs<MultiRecipeChoice>(recipeChoiceA)
        assertIs<SingleRecipeChoice>(recipeChoiceB)
        assertContentEquals(
            listOf(
                ItemRefMock("minecraft:gold_block"),
                ItemRefMock("minecraft:diamond_block")
            ), recipeChoiceA.items
        )
        assertEquals(ItemRefMock("minecraft:apple"), recipeChoiceB.item)

        val result = recipe.result
        assertIs<SingleRecipeResult>(result)
        assertEquals(ItemRefMock("minecraft:enchanted_golden_apple"), result.item)
        assertEquals(1, result.amount)
    }

    @Test
    fun `shapeless recipe serialization`() {
        key = Key.key("test:shapeless")

        val recipe = MinecraftRecipeRegistryLoader.uncheckedRecipes[key]
        assertNotNull(recipe)
        assertIs<ShapelessRecipe>(recipe)
        val ingredients = recipe.ingredients
        assertContains(ingredients, SingleRecipeChoice(ItemRefMock("minecraft", "poppy")))
        assertContains(
            ingredients,
            MultiRecipeChoice(
                listOf(
                    ItemRefMock("minecraft:red_dye"),
                    ItemRefMock("minecraft:pink_dye"),
                )
            )
        )
        assertEquals(
            4, ingredients
                .filterIsInstance<SingleRecipeChoice>()
                .count { it.item == ItemRefMock("minecraft:poppy") }
        )
        assertEquals(
            1, ingredients
                .filterIsInstance<MultiRecipeChoice>()
                .count {
                    it.items == listOf(
                        ItemRefMock("minecraft:red_dye"),
                        ItemRefMock("minecraft:pink_dye")
                    )
                }
        )
        val result = recipe.result
        assertIs<SingleRecipeResult>(result)
        assertEquals(ItemRefMock("minecraft:rose_bush"), result.item)
        assertEquals(2, result.amount)
    }

    @Test
    fun `smithing transform recipe serialization`() {
        key = Key.key("test:smithing_transform")

        val recipe = MinecraftRecipeRegistryLoader.uncheckedRecipes[key]
        assertNotNull(recipe)
        assertIs<SmithingTransformRecipe>(recipe)

        val base = recipe.base
        assertIs<MultiRecipeChoice>(base)
        assertContentEquals(
            listOf(
                ItemRefMock("minecraft:cobblestone"),
                ItemRefMock("minecraft:stone"),
            ), base.items
        )

        val addition = recipe.addition
        assertIs<SingleRecipeChoice>(addition)
        assertEquals(ItemRefMock("minecraft:ender_pearl"), addition.item)

        val template = recipe.template
        assertIs<EmptyRecipeChoice>(template)
        assertEquals(EmptyRecipeChoice, template)


        val result = recipe.result
        assertIs<SingleRecipeResult>(result)
        assertEquals(ItemRefMock("minecraft:end_stone"), result.item)
        assertEquals(1, result.amount)
    }

    @Test
    fun `smithing trim recipe serialization`() {
        key = Key.key("test:smithing_trim")

        val recipe = MinecraftRecipeRegistryLoader.uncheckedRecipes[key]
        assertNotNull(recipe)
        assertIs<SmithingTrimRecipe>(recipe)

        val base = recipe.base
        assertIs<SingleRecipeChoice>(base)
        assertEquals(ItemRefMock("armor/bronze_helmet"), base.item)

        val addition = recipe.addition
        assertIs<MultiRecipeChoice>(addition)
        assertContentEquals(
            listOf(
                ItemRefMock("minecraft:amethyst_shard"),
                ItemRefMock("minecraft:copper_ingot"),
                ItemRefMock("minecraft:diamond"),
                ItemRefMock("minecraft:emerald"),
                ItemRefMock("minecraft:gold_ingot"),
                ItemRefMock("minecraft:iron_ingot"),
                ItemRefMock("minecraft:lapis_lazuli"),
                ItemRefMock("minecraft:netherite_ingot"),
                ItemRefMock("minecraft:quartz"),
                ItemRefMock("minecraft:redstone")
            ), addition.items
        )

        val template = recipe.template
        assertIs<MultiRecipeChoice>(template)
        assertContentEquals(
            listOf(
                ItemRefMock("minecraft:coast_armor_trim_smithing_template"),
                ItemRefMock("minecraft:dune_armor_trim_smithing_template"),
                ItemRefMock("minecraft:eye_armor_trim_smithing_template"),
                ItemRefMock("minecraft:host_armor_trim_smithing_template"),
                ItemRefMock("minecraft:raiser_armor_trim_smithing_template"),
                ItemRefMock("minecraft:rib_armor_trim_smithing_template"),
                ItemRefMock("minecraft:sentry_armor_trim_smithing_template"),
                ItemRefMock("minecraft:shaper_armor_trim_smithing_template"),
                ItemRefMock("minecraft:silence_armor_trim_smithing_template"),
                ItemRefMock("minecraft:snout_armor_trim_smithing_template"),
                ItemRefMock("minecraft:spire_armor_trim_smithing_template"),
                ItemRefMock("minecraft:tide_armor_trim_smithing_template"),
                ItemRefMock("minecraft:vex_armor_trim_smithing_template"),
                ItemRefMock("minecraft:ward_armor_trim_smithing_template"),
                ItemRefMock("minecraft:wayfinder_armor_trim_smithing_template"),
                ItemRefMock("minecraft:wild_armor_trim_smithing_template"),
                ItemRefMock("minecraft:bolt_armor_trim_smithing_template"),
                ItemRefMock("minecraft:flow_armor_trim_smithing_template")
            ), template.items
        )


        val result = recipe.result
        assertIs<EmptyRecipeResult>(result)
    }

    @Test
    fun `smoking recipe serialization`() {
        key = Key.key("test:smoking")

        val recipe = MinecraftRecipeRegistryLoader.uncheckedRecipes[key]
        assertNotNull(recipe)
        assertIs<SmokingRecipe>(recipe)

        val input = recipe.input
        assertIs<SingleRecipeChoice>(input)
        assertEquals(ItemRefMock("minecraft:cobblestone"), input.item)

        val cookingTime = recipe.cookingTime
        assertEquals(100, cookingTime)

        val exp = recipe.exp
        assertEquals(2F, exp)

        val result = recipe.result
        assertIs<SingleRecipeResult>(result)
        assertEquals(ItemRefMock("minecraft:netherrack"), result.item)
        assertEquals(1, result.amount)
    }

    @Test
    fun `stonecutting recipe serialization`() {
        key = Key.key("test:stonecutting")

        val recipe = MinecraftRecipeRegistryLoader.uncheckedRecipes[key]
        assertNotNull(recipe)
        assertIs<StonecuttingRecipe>(recipe)

        val input = recipe.input
        assertIs<MultiRecipeChoice>(input)
        assertContentEquals(
            listOf(
                ItemRefMock("minecraft:oak_planks"),
                ItemRefMock("minecraft:spruce_planks"),
                ItemRefMock("minecraft:birch_planks"),
                ItemRefMock("minecraft:jungle_planks"),
                ItemRefMock("minecraft:acacia_planks"),
                ItemRefMock("minecraft:dark_oak_planks"),
                ItemRefMock("minecraft:mangrove_planks"),
                ItemRefMock("minecraft:cherry_planks"),
                ItemRefMock("minecraft:bamboo_planks"),
                ItemRefMock("minecraft:crimson_planks"),
                ItemRefMock("minecraft:warped_planks")
            ), input.items
        )

        val result = recipe.result
        assertIs<SingleRecipeResult>(result)
        assertEquals(ItemRefMock("minecraft:stick"), result.item)
        assertEquals(2, result.amount)
    }
}