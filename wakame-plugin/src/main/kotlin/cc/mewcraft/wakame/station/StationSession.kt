package cc.mewcraft.wakame.station

import cc.mewcraft.wakame.user.User
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
import org.bukkit.entity.Player

/**
 * 当玩家打开合成站时创建的会话
 * 封装了 打开合成站 到 关闭合成站 这期间所需要的所有状态
 */
class StationSession(
    val station: Station,
    val user: User<Player>,
) {
    val recipeMatcher = StationRecipeMatcher

    val recipeMatcherResults: Reference2ObjectOpenHashMap<StationRecipe, StationRecipeMatcherResult> = Reference2ObjectOpenHashMap()

    fun update() {
        station.forEach {
            val newResult = recipeMatcher.check(it, user.player)
            recipeMatcherResults[it] = newResult
        }
    }
}