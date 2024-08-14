package cc.mewcraft.wakame.station

import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

object StationRecipeMatcher {
    fun check(recipe: StationRecipe, player: Player): StationRecipeMatcherResult {
        val recipeInput = recipe.input
        val contextMap = StationChoiceMatcherContextMap(player)

        // 填充上下文
        recipe.populateContext(contextMap)

        val states = recipeInput.map { choice ->
            choice.checkState(contextMap)
        }

        return StationRecipeMatcherResult(
            states, states.map { it.description }
        )
    }
}

data class StationRecipeMatcherResult(
    val states: List<StationChoiceCheckState>,
    val description: List<Component>,
)