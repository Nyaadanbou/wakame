package recipe

import cc.mewcraft.wakame.craft.recipe.RecipeRegistry
import cc.mewcraft.wakame.craft.recipe.ShapelessRecipe
import cc.mewcraft.wakame.craft.recipe.SingleRecipeChoice
import cc.mewcraft.wakame.craft.recipe.SingleRecipeResult
import cc.mewcraft.wakame.util.Key
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
        }

        @JvmStatic
        @AfterAll
        fun shutdown() {
            stopKoin()
        }
    }

    private val logger by inject<Logger>()

    @Test
    fun `shaped recipe serialization`() {
        RecipeRegistry.onPostWorld()
        logger.info(RecipeRegistry.ALL[Key("test:shaped")].toString())
    }

    @Test
    fun `shapeless recipe serialization`() {
        RecipeRegistry.onPostWorld()
        val key = Key("test:shapeless")
        val recipe = RecipeRegistry.ALL.find(key)
        assertNotNull(recipe)
        assertIs<ShapelessRecipe>(recipe)
        val ingredients = recipe.ingredients
        assertContains(ingredients, SingleRecipeChoice(Key("minecraft:poppy")))
        assertContains(ingredients, SingleRecipeChoice(Key("minecraft:red_dye")))
        assertEquals(4, ingredients.filterIsInstance<SingleRecipeChoice>()
            .count { it.choice == Key("minecraft:poppy") })
        assertEquals(1, ingredients.filterIsInstance<SingleRecipeChoice>()
            .count { it.choice == Key("minecraft:red_dye") })
        val result = recipe.result
        assertIs<SingleRecipeResult>(result)
        assertEquals(Key("minecraft:rose_bush"), result.result)
        assertEquals(2, result.amount)
    }
}