package cc.mewcraft.wakame.station

import it.unimi.dsi.fastutil.objects.Reference2BooleanArrayMap
import me.lucko.helper.text3.mini
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

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
        val choiceCheckerFlags: Reference2BooleanArrayMap<StationChoice> = Reference2BooleanArrayMap()
        choices.forEach {
            choiceCheckerFlags.put(it, it.check(checkerContextMap))
        }
        return RecipeMatcherResult(recipe, choiceCheckerFlags)
    }
}

/**
 * 合成站配方匹配器的匹配结果的封装.
 */
data class RecipeMatcherResult(
    val recipe: StationRecipe,
    val choiceCheckerFlags: Reference2BooleanArrayMap<StationChoice>
) {

    val canCraft: Boolean = !choiceCheckerFlags.values.contains(false)

    /**
     * 获取展示该配方的Gui物品的name
     */
    private fun name(stationLayout: StationLayout): Component {
        return stationLayout.recipeNameLang.replace("<result>", recipe.output.description(stationLayout)).mini
    }

    /**
     * 获取展示该配方的Gui物品的lore
     */
    private fun lore(stationLayout: StationLayout): List<Component> {
        val choices = choiceCheckerFlags.map {
            val prefix = if (it.value) stationLayout.sufficientPrefixLang else stationLayout.insufficientPrefixLang
            val choice = it.key
            choice.description(stationLayout).replace("<prefix>", prefix)
        }
        return stationLayout.recipeLoreLang
            .flatMap { if (it == "<choices>") choices else listOf(it) }
            .map { it.mini }
    }

    /**
     * 获取展示该配方的Gui物品
     */
    fun guiItemStack(stationLayout: StationLayout): ItemStack {
        val itemStack = recipe.output.guiItemStack()
        itemStack.editMeta {
            it.itemName(name(stationLayout))
            it.lore(lore(stationLayout))
        }
        return itemStack
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