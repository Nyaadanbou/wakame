package recipe

import cc.mewcraft.wakame.core.ItemXBootstrap
import cc.mewcraft.wakame.recipe.*
import cc.mewcraft.wakame.util.Key
import core.ItemXMock
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

            ItemXBootstrap.init()
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
        assertIs<MultiRecipeChoice>(input)
        assertContentEquals(
            expected = listOf(
                ItemXMock("minecraft:glass"),
                ItemXMock("minecraft:white_stained_glass"),
                ItemXMock("minecraft:orange_stained_glass"),
                ItemXMock("minecraft:magenta_stained_glass"),
                ItemXMock("minecraft:light_blue_stained_glass"),
                ItemXMock("minecraft:yellow_stained_glass"),
                ItemXMock("minecraft:lime_stained_glass"),
                ItemXMock("minecraft:pink_stained_glass"),
                ItemXMock("minecraft:gray_stained_glass"),
                ItemXMock("minecraft:light_gray_stained_glass"),
                ItemXMock("minecraft:cyan_stained_glass"),
                ItemXMock("minecraft:purple_stained_glass"),
                ItemXMock("minecraft:blue_stained_glass"),
                ItemXMock("minecraft:brown_stained_glass"),
                ItemXMock("minecraft:green_stained_glass"),
                ItemXMock("minecraft:red_stained_glass"),
                ItemXMock("minecraft:black_stained_glass")
            ),
            actual = input.choices
        )

        val cookingTime = recipe.cookingTime
        assertEquals(40, cookingTime)

        val exp = recipe.exp
        assertEquals(495F, exp)

        val result = recipe.result
        assertIs<SingleRecipeResult>(result)
        assertEquals(ItemXMock("minecraft:quartz"), result.result)
        assertEquals(1, result.amount)
    }

    @Test
    fun `campfire recipe serialization`() {
        key = Key("test:campfire")

        val recipe = VanillaRecipeRegistry.raw[key]
        assertNotNull(recipe)
        assertIs<CampfireRecipe>(recipe)

        val input = recipe.input
        assertIs<SingleRecipeChoice>(input)
        assertEquals(ItemXMock("minecraft:poisonous_potato"), input.choice)

        val cookingTime = recipe.cookingTime
        assertEquals(1, cookingTime)

        val exp = recipe.exp
        assertEquals(1000F, exp)

        val result = recipe.result
        assertIs<SingleRecipeResult>(result)
        assertEquals(ItemXMock("minecraft:diamond"), result.result)
        assertEquals(1, result.amount)
    }

    @Test
    fun `furnace recipe serialization`() {
        key = Key("test:furnace")

        val recipe = VanillaRecipeRegistry.raw[key]
        assertNotNull(recipe)
        assertIs<FurnaceRecipe>(recipe)

        val input = recipe.input
        assertIs<SingleRecipeChoice>(input)
        assertEquals(ItemXMock("minecraft:gravel"), input.choice)

        val cookingTime = recipe.cookingTime
        assertEquals(200, cookingTime)

        val exp = recipe.exp
        assertEquals(0F, exp)

        val result = recipe.result
        assertIs<SingleRecipeResult>(result)
        assertEquals(ItemXMock("minecraft:sand"), result.result)
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
        assertIs<MultiRecipeChoice>(recipeChoiceA)
        assertIs<SingleRecipeChoice>(recipeChoiceB)
        assertContentEquals(
            listOf(
                ItemXMock("minecraft:gold_block"),
                ItemXMock("minecraft:diamond_block")
            ), recipeChoiceA.choices
        )
        assertEquals(ItemXMock("minecraft:apple"), recipeChoiceB.choice)

        val result = recipe.result
        assertIs<SingleRecipeResult>(result)
        assertEquals(ItemXMock("minecraft:enchanted_golden_apple"), result.result)
        assertEquals(1, result.amount)
    }

    @Test
    fun `shapeless recipe serialization`() {
        key = Key("test:shapeless")

        val recipe = VanillaRecipeRegistry.raw[key]
        assertNotNull(recipe)
        assertIs<ShapelessRecipe>(recipe)
        val ingredients = recipe.ingredients
        assertContains(ingredients, SingleRecipeChoice(ItemXMock("minecraft", "poppy")))
        assertContains(
            ingredients,
            MultiRecipeChoice(
                listOf(
                    ItemXMock("minecraft:red_dye"),
                    ItemXMock("minecraft:pink_dye"),
                )
            )
        )
        assertEquals(4, ingredients
            .filterIsInstance<SingleRecipeChoice>()
            .count { it.choice == ItemXMock("minecraft:poppy") }
        )
        assertEquals(1, ingredients
            .filterIsInstance<MultiRecipeChoice>()
            .count {
                it.choices == listOf(
                    ItemXMock("minecraft:red_dye"),
                    ItemXMock("minecraft:pink_dye")
                )
            }
        )
        val result = recipe.result
        assertIs<SingleRecipeResult>(result)
        assertEquals(ItemXMock("minecraft:rose_bush"), result.result)
        assertEquals(2, result.amount)
    }

    @Test
    fun `smithing transform recipe serialization`() {
        key = Key("test:smithing_transform")

        val recipe = VanillaRecipeRegistry.raw[key]
        assertNotNull(recipe)
        assertIs<SmithingTransformRecipe>(recipe)

        val base = recipe.base
        assertIs<MultiRecipeChoice>(base)
        assertContentEquals(
            listOf(
                ItemXMock("minecraft:cobblestone"),
                ItemXMock("minecraft:stone"),
            ), base.choices
        )

        val addition = recipe.addition
        assertIs<SingleRecipeChoice>(addition)
        assertEquals(ItemXMock("minecraft:ender_pearl"), addition.choice)

        val template = recipe.template
        assertIs<EmptyRecipeChoice>(template)
        assertEquals(EmptyRecipeChoice, template)


        val result = recipe.result
        assertIs<SingleRecipeResult>(result)
        assertEquals(ItemXMock("minecraft:end_stone"), result.result)
        assertEquals(1, result.amount)
    }

    @Test
    fun `smithing trim recipe serialization`() {
        key = Key("test:smithing_trim")

        val recipe = VanillaRecipeRegistry.raw[key]
        assertNotNull(recipe)
        assertIs<SmithingTrimRecipe>(recipe)

        val base = recipe.base
        assertIs<SingleRecipeChoice>(base)
        assertEquals(ItemXMock("wakame:armor/bronze_helmet"), base.choice)

        val addition = recipe.addition
        assertIs<MultiRecipeChoice>(addition)
        assertContentEquals(
            listOf(
                ItemXMock("minecraft:amethyst_shard"),
                ItemXMock("minecraft:copper_ingot"),
                ItemXMock("minecraft:diamond"),
                ItemXMock("minecraft:emerald"),
                ItemXMock("minecraft:gold_ingot"),
                ItemXMock("minecraft:iron_ingot"),
                ItemXMock("minecraft:lapis_lazuli"),
                ItemXMock("minecraft:netherite_ingot"),
                ItemXMock("minecraft:quartz"),
                ItemXMock("minecraft:redstone")
            ), addition.choices
        )

        val template = recipe.template
        assertIs<MultiRecipeChoice>(template)
        assertContentEquals(
            listOf(
                ItemXMock("minecraft:coast_armor_trim_smithing_template"),
                ItemXMock("minecraft:dune_armor_trim_smithing_template"),
                ItemXMock("minecraft:eye_armor_trim_smithing_template"),
                ItemXMock("minecraft:host_armor_trim_smithing_template"),
                ItemXMock("minecraft:raiser_armor_trim_smithing_template"),
                ItemXMock("minecraft:rib_armor_trim_smithing_template"),
                ItemXMock("minecraft:sentry_armor_trim_smithing_template"),
                ItemXMock("minecraft:shaper_armor_trim_smithing_template"),
                ItemXMock("minecraft:silence_armor_trim_smithing_template"),
                ItemXMock("minecraft:snout_armor_trim_smithing_template"),
                ItemXMock("minecraft:spire_armor_trim_smithing_template"),
                ItemXMock("minecraft:tide_armor_trim_smithing_template"),
                ItemXMock("minecraft:vex_armor_trim_smithing_template"),
                ItemXMock("minecraft:ward_armor_trim_smithing_template"),
                ItemXMock("minecraft:wayfinder_armor_trim_smithing_template"),
                ItemXMock("minecraft:wild_armor_trim_smithing_template"),
                ItemXMock("minecraft:bolt_armor_trim_smithing_template"),
                ItemXMock("minecraft:flow_armor_trim_smithing_template")
            ), template.choices
        )


        val result = recipe.result
        assertIs<EmptyRecipeResult>(result)
    }

    @Test
    fun `smoking recipe serialization`() {
        key = Key("test:smoking")

        val recipe = VanillaRecipeRegistry.raw[key]
        assertNotNull(recipe)
        assertIs<SmokingRecipe>(recipe)

        val input = recipe.input
        assertIs<SingleRecipeChoice>(input)
        assertEquals(ItemXMock("minecraft:cobblestone"), input.choice)

        val cookingTime = recipe.cookingTime
        assertEquals(100, cookingTime)

        val exp = recipe.exp
        assertEquals(2F, exp)

        val result = recipe.result
        assertIs<SingleRecipeResult>(result)
        assertEquals(ItemXMock("minecraft:netherrack"), result.result)
        assertEquals(1, result.amount)
    }

    @Test
    fun `stonecutting recipe serialization`() {
        key = Key("test:stonecutting")

        val recipe = VanillaRecipeRegistry.raw[key]
        assertNotNull(recipe)
        assertIs<StonecuttingRecipe>(recipe)

        val input = recipe.input
        assertIs<MultiRecipeChoice>(input)
        assertContentEquals(
            listOf(
                ItemXMock("minecraft:oak_planks"),
                ItemXMock("minecraft:spruce_planks"),
                ItemXMock("minecraft:birch_planks"),
                ItemXMock("minecraft:jungle_planks"),
                ItemXMock("minecraft:acacia_planks"),
                ItemXMock("minecraft:dark_oak_planks"),
                ItemXMock("minecraft:mangrove_planks"),
                ItemXMock("minecraft:cherry_planks"),
                ItemXMock("minecraft:bamboo_planks"),
                ItemXMock("minecraft:crimson_planks"),
                ItemXMock("minecraft:warped_planks")
            ), input.choices
        )

        val result = recipe.result
        assertIs<SingleRecipeResult>(result)
        assertEquals(ItemXMock("minecraft:stick"), result.result)
        assertEquals(2, result.amount)
    }
}