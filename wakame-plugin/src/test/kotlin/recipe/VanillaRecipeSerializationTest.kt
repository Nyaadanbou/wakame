package recipe

import cc.mewcraft.wakame.recipe.*
import cc.mewcraft.wakame.util.Key
import net.kyori.adventure.key.Key
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.inject
import org.slf4j.Logger
import testEnv
import kotlin.test.*

class VanillaRecipeSerializationTest : KoinTest {
    companion object {
        @JvmStatic
        @BeforeAll
        fun setup() {
            startKoin {
                modules(
                    testEnv(),
                )
            }

            VanillaRecipeRegistry.loadConfig()
        }

        @JvmStatic
        @AfterAll
        fun shutdown() {
            stopKoin()
        }
    }

    private val logger: Logger by inject()
    private lateinit var key: Key

    @BeforeTest
    fun beforeEach() {

    }

    @AfterTest
    fun afterTest() {
        logger.info(key.asString())
    }

    @Test
    fun `blasting recipe serialization`() {
        key = Key("test:blasting")

        val recipe = VanillaRecipeRegistry.raw[key]
        assertNotNull(recipe)
        assertIs<BlastingRecipe>(recipe)

        val input = recipe.input
        assertEquals(
            MultiRecipeChoice(
                listOf(
                    Key("minecraft:glass"),
                    Key("minecraft:white_stained_glass"),
                    Key("minecraft:orange_stained_glass"),
                    Key("minecraft:magenta_stained_glass"),
                    Key("minecraft:light_blue_stained_glass"),
                    Key("minecraft:yellow_stained_glass"),
                    Key("minecraft:lime_stained_glass"),
                    Key("minecraft:pink_stained_glass"),
                    Key("minecraft:gray_stained_glass"),
                    Key("minecraft:light_gray_stained_glass"),
                    Key("minecraft:cyan_stained_glass"),
                    Key("minecraft:purple_stained_glass"),
                    Key("minecraft:blue_stained_glass"),
                    Key("minecraft:brown_stained_glass"),
                    Key("minecraft:green_stained_glass"),
                    Key("minecraft:red_stained_glass"),
                    Key("minecraft:black_stained_glass")
                )
            ), input
        )

        val cookingTime = recipe.cookingTime
        assertEquals(40, cookingTime)

        val exp = recipe.exp
        assertEquals(495F, exp)

        val result = recipe.result
        assertIs<SingleRecipeResult>(result)
        assertEquals(Key("minecraft:quartz"), result.result)
        assertEquals(1, result.amount)
    }

    @Test
    fun `campfire recipe serialization`() {
        key = Key("test:campfire")

        val recipe = VanillaRecipeRegistry.raw[key]
        assertNotNull(recipe)
        assertIs<CampfireRecipe>(recipe)

        val input = recipe.input
        assertEquals(SingleRecipeChoice(Key("minecraft:poisonous_potato")), input)

        val cookingTime = recipe.cookingTime
        assertEquals(1, cookingTime)

        val exp = recipe.exp
        assertEquals(1000F, exp)

        val result = recipe.result
        assertIs<SingleRecipeResult>(result)
        assertEquals(Key("minecraft:diamond"), result.result)
        assertEquals(1, result.amount)
    }

    @Test
    fun `furnace recipe serialization`() {
        key = Key("test:furnace")

        val recipe = VanillaRecipeRegistry.raw[key]
        assertNotNull(recipe)
        assertIs<FurnaceRecipe>(recipe)

        val input = recipe.input
        assertEquals(SingleRecipeChoice(Key("minecraft:gravel")), input)

        val cookingTime = recipe.cookingTime
        assertEquals(200, cookingTime)

        val exp = recipe.exp
        assertEquals(0F, exp)

        val result = recipe.result
        assertIs<SingleRecipeResult>(result)
        assertEquals(Key("minecraft:sand"), result.result)
        assertEquals(1, result.amount)
    }

    @Test
    fun `shaped recipe serialization`() {
        key = Key("test:shaped")

        val recipe = VanillaRecipeRegistry.raw[key]
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
        assertEquals(
            MultiRecipeChoice(
                listOf(Key("minecraft:gold_block"), Key("minecraft:diamond_block"))
            ), recipeChoiceA
        )
        assertEquals(SingleRecipeChoice(Key("minecraft:apple")), recipeChoiceB)

        val result = recipe.result
        assertIs<SingleRecipeResult>(result)
        assertEquals(Key("minecraft:enchanted_golden_apple"), result.result)
        assertEquals(1, result.amount)
    }

    @Test
    fun `shapeless recipe serialization`() {
        key = Key("test:shapeless")

        val recipe = VanillaRecipeRegistry.raw[key]
        assertNotNull(recipe)
        assertIs<ShapelessRecipe>(recipe)
        val ingredients = recipe.ingredients
        assertContains(ingredients, SingleRecipeChoice(Key("minecraft:poppy")))
        assertContains(
            ingredients,
            MultiRecipeChoice(
                listOf(
                    Key("minecraft:red_dye"),
                    Key("minecraft:pink_dye"),
                )
            )
        )
        assertEquals(4, ingredients
            .filterIsInstance<SingleRecipeChoice>()
            .count { it.choice == Key("minecraft:poppy") }
        )
        assertEquals(1, ingredients
            .filterIsInstance<MultiRecipeChoice>()
            .count {
                it.choices.contains(Key("minecraft:red_dye"))
                        && it.choices.contains(Key("minecraft:pink_dye"))
            }
        )
        val result = recipe.result
        assertIs<SingleRecipeResult>(result)
        assertEquals(Key("minecraft:rose_bush"), result.result)
        assertEquals(2, result.amount)
    }

    @Test
    fun `smithing transform recipe serialization`() {
        key = Key("test:smithing_transform")

        val recipe = VanillaRecipeRegistry.raw[key]
        assertNotNull(recipe)
        assertIs<SmithingTransformRecipe>(recipe)

        val base = recipe.base
        assertEquals(
            MultiRecipeChoice(
                listOf(
                    Key("minecraft:cobblestone"),
                    Key("minecraft:stone"),
                )
            ), base
        )

        val addition = recipe.addition
        assertEquals(SingleRecipeChoice(Key("minecraft:ender_pearl")), addition)

        val template = recipe.template
        assertEquals(EmptyRecipeChoice, template)


        val result = recipe.result
        assertIs<SingleRecipeResult>(result)
        assertEquals(Key("minecraft:end_stone"), result.result)
        assertEquals(1, result.amount)
    }

    @Test
    fun `smoking recipe serialization`() {
        key = Key("test:smoking")

        val recipe = VanillaRecipeRegistry.raw[key]
        assertNotNull(recipe)
        assertIs<SmokingRecipe>(recipe)

        val input = recipe.input
        assertEquals(SingleRecipeChoice(Key("minecraft:cobblestone")), input)

        val cookingTime = recipe.cookingTime
        assertEquals(100, cookingTime)

        val exp = recipe.exp
        assertEquals(2F, exp)

        val result = recipe.result
        assertIs<SingleRecipeResult>(result)
        assertEquals(Key("minecraft:netherrack"), result.result)
        assertEquals(1, result.amount)
    }

    @Test
    fun `stonecutting recipe serialization`() {
        key = Key("test:stonecutting")

        val recipe = VanillaRecipeRegistry.raw[key]
        assertNotNull(recipe)
        assertIs<StonecuttingRecipe>(recipe)

        val input = recipe.input
        assertEquals(
            MultiRecipeChoice(
                listOf(
                    Key("minecraft:oak_planks"),
                    Key("minecraft:spruce_planks"),
                    Key("minecraft:birch_planks"),
                    Key("minecraft:jungle_planks"),
                    Key("minecraft:acacia_planks"),
                    Key("minecraft:dark_oak_planks"),
                    Key("minecraft:mangrove_planks"),
                    Key("minecraft:cherry_planks"),
                    Key("minecraft:bamboo_planks"),
                    Key("minecraft:crimson_planks"),
                    Key("minecraft:warped_planks")
                )
            ), input
        )

        val result = recipe.result
        assertIs<SingleRecipeResult>(result)
        assertEquals(Key("minecraft:stick"), result.result)
        assertEquals(2, result.amount)
    }
}