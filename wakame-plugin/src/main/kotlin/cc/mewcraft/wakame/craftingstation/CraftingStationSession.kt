package cc.mewcraft.wakame.craftingstation

import cc.mewcraft.wakame.craftingstation.recipe.Recipe
import cc.mewcraft.wakame.craftingstation.recipe.RecipeMatcherResult
import it.unimi.dsi.fastutil.objects.Reference2ObjectLinkedOpenHashMap
import org.bukkit.entity.Player

/**
 * 代表一个玩家使用合成站的会话.
 * 每当玩家打开合成站时, 会创建一个 [CraftingStationSession] 对象.
 * 封装了 *打开合成站* 到 *关闭合成站* 这期间所需的所有状态.
 */
internal class CraftingStationSession(
    val station: CraftingStation,
    val player: Player,
) {

    /**
     * 该会话中所有配方的匹配结果.
     */
    private val recipe2MatcherResult = Reference2ObjectLinkedOpenHashMap<Recipe, RecipeMatcherResult>().apply {
        // 初始化时遍历所有配方并进行匹配
        station.forEach { recipe: Recipe ->
            this[recipe] = recipe.match(player)
        }
    }

    /**
     * 获取 [station] 中所有合成配方的匹配结果, 可以合成的会排在前面.
     */
    fun getRecipeMatcherResults(): List<RecipeMatcherResult> {
        return recipe2MatcherResult.values.sortedByDescending(RecipeMatcherResult::isAllowed)
    }

    /**
     * 重新检查 [station] 中的所有合成配方, 可用于刷新合成站的界面.
     */
    fun updateRecipeMatcherResults() {
        station.forEach { recipe: Recipe ->
            val newResult = recipe.match(player)
            // 本函数不会更新结果没有发生任何变化的合成配方
            if (!newResult.isSameResult(recipe2MatcherResult[recipe])) {
                recipe2MatcherResult[recipe] = newResult
            }
        }
    }
}