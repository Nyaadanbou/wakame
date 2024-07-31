package recipe

import cc.mewcraft.wakame.craft.recipe.*
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

class RecipeSerializationTest : KoinTest {
    companion object {
        @JvmStatic
        @BeforeAll
        fun setup() {
            startKoin {
                modules(
                    testEnv(),
                )
            }

            RecipeRegistry.loadConfig()
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
    fun `shaped recipe serialization`() {
        key = Key("test:shaped")

        val recipe = RecipeRegistry.raw[key]
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
        assertEquals(Key("minecraft:enchant_golden_apple"), result.result)
        assertEquals(1, result.amount)
    }

    @Test
    fun `shapeless recipe serialization`() {
        key = Key("test:shapeless")

        val recipe = RecipeRegistry.raw[key]
        assertNotNull(recipe)
        assertIs<ShapelessRecipe>(recipe)
        val ingredients = recipe.ingredients
        assertContains(ingredients, SingleRecipeChoice(Key("minecraft:poppy")))
        assertContains(ingredients, SingleRecipeChoice(Key("minecraft:red_dye")))
        assertEquals(4, ingredients
            .filterIsInstance<SingleRecipeChoice>()
            .count { it.choice == Key("minecraft:poppy") }
        )
        assertEquals(1, ingredients
            .filterIsInstance<SingleRecipeChoice>()
            .count { it.choice == Key("minecraft:red_dye") }
        )
        val result = recipe.result
        assertIs<SingleRecipeResult>(result)
        assertEquals(Key("minecraft:rose_bush"), result.result)
        assertEquals(2, result.amount)
    }
}