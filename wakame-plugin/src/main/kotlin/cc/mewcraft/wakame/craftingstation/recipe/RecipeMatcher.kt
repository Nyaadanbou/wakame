package cc.mewcraft.wakame.craftingstation.recipe

import cc.mewcraft.wakame.gui.MenuLayout
import cc.mewcraft.wakame.util.itemName
import cc.mewcraft.wakame.util.lore0
import it.unimi.dsi.fastutil.objects.Reference2BooleanArrayMap
import me.lucko.helper.text3.mini
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * 用于检查一个玩家 [Player] 是否满足 [Recipe] 的所有要求.
 */
internal object RecipeMatcher {
    fun match(recipe: Recipe, player: Player): RecipeMatcherResult {
        val choices = recipe.input

        // 创建 ChoiceCheckerContextMap
        val contextMap = ChoiceCheckerContextMap(player)

        // 提取出同类的 ChoiceChecker
        val checkers: List<ChoiceChecker<*>> = choices.map(RecipeChoice::checker).distinct()

        // 同类的 ChoiceChecker 使用同一个上下文.
        // 这里初始化每一类 ChoiceChecker 的上下文.
        checkers.forEach { checker: ChoiceChecker<*> ->
            contextMap[checker] = checker.initCtx(player)
        }

        // 检查每个 StationChoice.
        // 组装后, 返回配方的匹配结果.
        val result = Reference2BooleanArrayMap<RecipeChoice>().apply {
            choices.forEach { choice: RecipeChoice -> put(choice, choice.check(contextMap)) }
        }

        return RecipeMatcherResult(recipe, result)
    }
}

/**
 * 合成站配方匹配器的匹配结果的封装.
 *
 * @param recipe 与该结果所关联的 [Recipe]
 * @param result 匹配的结果映射, 映射关系为: 配方中的材料 ([Recipe]) -> 是否匹配 ([Boolean])
 */
internal class RecipeMatcherResult(
    recipe: Recipe,
    result: Map<RecipeChoice, Boolean>,
) {
    private val result: Reference2BooleanArrayMap<RecipeChoice> = Reference2BooleanArrayMap(result) // explicit copy

    val recipe: Recipe = recipe
    val canCraft: Boolean = !this.result.values.contains(false)

    /**
     * 获取展示该配方的 Gui 物品.
     */
    fun displayItemStack(layout: MenuLayout): ItemStack {
        val itemStack = recipe.output.displayItemStack()
        itemStack.itemName = getRecipeIconName(layout)
        itemStack.lore0 = getRecipeIconLore(layout)
        return itemStack
    }

    /**
     * 判断另一个 [RecipeMatcherResult] 与本 [RecipeMatcherResult] 是否相同.
     */
    fun isSameResult(other: RecipeMatcherResult?): Boolean {
        if (other == null)
            return false
        if (this.result.size != other.result.size)
            return false
        other.result.forEach { (choice, flag) ->
            if (!this.result.containsKey(choice))
                return false
            if (this.result.getBoolean(choice) != flag)
                return false
        }
        return true
    }

    /**
     * 获取展示该配方的 Gui 物品的 `minecraft:item_name`.
     */
    private fun getRecipeIconName(layout: MenuLayout): Component {
        // 缺省构建格式: "合成: <result>"
        return (layout.getLang("recipe.name") ?: "合成: <result>")
            .replace("<result>", recipe.output.description(layout))
            .mini
    }

    /**
     * 获取展示该配方的 Gui 物品的 `minecraft:lore`.
     */
    private fun getRecipeIconLore(layout: MenuLayout): List<Component> {
        val sufficientPrefix = layout.getLang("prefix.sufficient") ?: "✔"
        val insufficientPrefix = layout.getLang("prefix.insufficient") ?: "✖"
        val choices = result.map { (choice, flag) ->
            val prefix = if (flag) sufficientPrefix else insufficientPrefix
            choice.description(layout).replace("<prefix>", prefix)
        }
        val loreStrings = (layout.getLang("recipe.lore") ?: "合成所需材料:\n<choices>")
            .split('\n')
        return loreStrings
            .flatMap { if (it == "<choices>") choices else listOf(it) }
            .map(String::mini)
    }
}