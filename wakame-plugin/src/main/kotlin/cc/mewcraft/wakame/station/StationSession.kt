package cc.mewcraft.wakame.station

import cc.mewcraft.wakame.station.recipe.RecipeMatcherResult
import cc.mewcraft.wakame.station.recipe.StationRecipe
import it.unimi.dsi.fastutil.objects.Reference2ObjectLinkedOpenHashMap
import org.bukkit.entity.Player

/**
 * 当玩家打开合成站时创建的会话
 * 封装了 打开合成站 到 关闭合成站 这期间所需要的所有状态
 */
internal class StationSession(
    val station: Station,
    val player: Player,
) : Iterable<RecipeMatcherResult> {

    /**
     * 该会话中所有配方的匹配结果.
     */
    private val stationRecipe2MatchResult = Reference2ObjectLinkedOpenHashMap<StationRecipe, RecipeMatcherResult>().apply {
        // 初始化时遍历所有配方并进行匹配
        station.forEach { recipe: StationRecipe ->
            this[recipe] = recipe.match(player)
        }
    }

    /**
     * 遍历所有配方并进行匹配.
     * 对于匹配结果不变的配方，不进行map写入操作.
     */
    fun updateRecipeMatcherResults() {
        station.forEach { recipe: StationRecipe ->
            val newResult = recipe.match(player)
            if (!newResult.isSameResult(stationRecipe2MatchResult[recipe])) {
                stationRecipe2MatchResult[recipe] = newResult
            }
        }
    }

    /**
     * [RecipeMatcherResult] 的迭代器.
     * 获取时会先进行排序，可合成的配方会排在前面.
     */
    override fun iterator(): Iterator<RecipeMatcherResult> {
        return stationRecipe2MatchResult.values
            .sortedByDescending(RecipeMatcherResult::canCraft)
            .iterator()
    }
}