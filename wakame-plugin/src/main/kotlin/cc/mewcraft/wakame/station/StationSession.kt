package cc.mewcraft.wakame.station

import cc.mewcraft.wakame.station.recipe.RecipeMatcherResult
import cc.mewcraft.wakame.station.recipe.StationRecipe
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
import org.bukkit.entity.Player

/**
 * 当玩家打开合成站时创建的会话
 * 封装了 打开合成站 到 关闭合成站 这期间所需要的所有状态
 */
class StationSession(
    val station: Station,
    val player: Player,
) : Iterable<RecipeMatcherResult> {
    private val recipeMatcherResults: Reference2ObjectOpenHashMap<StationRecipe, RecipeMatcherResult> = Reference2ObjectOpenHashMap()

    /**
     * 初始化时遍历所有配方并进行匹配.
     */
    init {
        station.forEach {
            recipeMatcherResults[it] = it.match(player)
        }
    }

    /**
     * 遍历所有配方并进行匹配.
     * 对于匹配结果不变的配方，不进行map写入操作.
     */
    fun updateRecipeMatcherResults() {
        station.forEach {
            val newResult = it.match(player)
            if (!newResult.isSame(recipeMatcherResults[it])) {
                recipeMatcherResults[it] = newResult
            }
        }
    }

    /**
     * [RecipeMatcherResult] 的迭代器.
     * 获取时会先进行排序，可合成的配方会排在前面.
     */
    override fun iterator(): Iterator<RecipeMatcherResult> {
        return recipeMatcherResults.values.sortedBy {
            it.canCraft
        }.iterator()
    }
}