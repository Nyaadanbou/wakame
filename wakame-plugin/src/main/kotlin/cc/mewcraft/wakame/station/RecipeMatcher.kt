package cc.mewcraft.wakame.station

import it.unimi.dsi.fastutil.objects.Reference2BooleanArrayMap
import me.lucko.helper.text3.mini
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

/**
 * [StationRecipe] 的匹配器.
 * 检查一特定玩家某配方的各项要求是否分别满足.
 */
object RecipeMatcher {
    fun match(recipe: StationRecipe, player: Player): RecipeMatcherResult {
        val choices = recipe.input

        // 创建ChoiceCheckerContextMap
        val checkerContextMap = ChoiceCheckerContextMap(player)
        // 提取出同类的ChoiceChecker
        val checkers: List<ChoiceChecker<*>> =
            choices.map { it.checker }.distinct()
        // 同类的ChoiceChecker使用同一个上下文
        // 初始化每一类ChoiceChecker的上下文
        checkers.forEach { checker ->
            val context = checker.initializeContext(player)
            checkerContextMap[checker] = context
        }

        // 检查每个StationChoice
        // 组装后返回配方匹配结果
        val matcherResult = RecipeMatcherResult()
        choices.forEach {
            matcherResult.choiceCheckerFlags.put(it, it.check(checkerContextMap))
        }
        return matcherResult
    }
}

/**
 * 工作站配方匹配器的匹配结果的封装.
 */
class RecipeMatcherResult {
    companion object {
        const val SUFFICIENT_PREFIX = "<green>✔</green>"
        const val INSUFFICIENT_PREFIX = "<red>✖</red>"
    }

    val choiceCheckerFlags: Reference2BooleanArrayMap<StationChoice> = Reference2BooleanArrayMap()

    /**
     * 获取展示此结果的lore格式
     */
    fun lore(): List<Component> {
        return choiceCheckerFlags.map {
            val prefix = if (it.value) SUFFICIENT_PREFIX else INSUFFICIENT_PREFIX
            val choice = it.key
            // 构建格式: "✔ 材料 *1"
            "<!i>$prefix ${choice.description()}".mini
        }
    }

    /**
     * 判断另一个 [RecipeMatcherResult] 与本 [RecipeMatcherResult] 是否相同
     */
    fun isSame(other: RecipeMatcherResult?): Boolean {
        if (other == null) return false
        if (this.choiceCheckerFlags.size != other.choiceCheckerFlags.size) {
            return false
        }
        other.choiceCheckerFlags.forEach { (choice, flag) ->
            if (!this.choiceCheckerFlags.containsKey(choice)) return false
            if (this.choiceCheckerFlags.getBoolean(choice) != flag) return false
        }
        return true
    }
}