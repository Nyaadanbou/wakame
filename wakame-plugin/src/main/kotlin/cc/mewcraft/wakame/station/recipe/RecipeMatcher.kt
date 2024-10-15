package cc.mewcraft.wakame.station.recipe

import cc.mewcraft.wakame.gui.MenuLayout
import it.unimi.dsi.fastutil.objects.Reference2BooleanArrayMap
import me.lucko.helper.text3.mini
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * [StationRecipe] 的匹配器.
 * 检查一特定玩家某配方的各项要求是否分别满足.
 */
internal object RecipeMatcher {
    fun match(recipe: StationRecipe, player: Player): RecipeMatcherResult {
        val choices = recipe.input

        // 创建 ChoiceCheckerContextMap
        val checkerContextMap = ChoiceCheckerContextMap(player)
        // 提取出同类的 ChoiceChecker
        val checkers = choices.map(StationChoice::checker).distinct()
        // 同类的 ChoiceChecker 使用同一个上下文
        // 初始化每一类 ChoiceChecker 的上下文
        checkers.forEach { checker ->
            val context = checker.initializeContext(player)
            checkerContextMap[checker] = context
        }

        // 检查每个 StationChoice
        // 组装后返回配方匹配结果
        val choiceCheckerFlags = Reference2BooleanArrayMap<StationChoice>()
        choices.forEach { choice -> choiceCheckerFlags.put(choice, choice.check(checkerContextMap)) }
        return RecipeMatcherResult(recipe, choiceCheckerFlags)
    }
}

/**
 * 合成站配方匹配器的匹配结果的封装.
 */
internal data class RecipeMatcherResult(
    val recipe: StationRecipe,
    val choiceCheckerFlags: Reference2BooleanArrayMap<StationChoice>,
) {
    val canCraft: Boolean = !choiceCheckerFlags.values.contains(false)

    /**
     * 获取展示该配方的 Gui 物品的 name.
     */
    private fun recipeItemName(layout: MenuLayout): Component {
        // 缺省构建格式: "合成: <result>"
        return (layout.getLang("recipe.name") ?: "合成: <result>")
            .replace("<result>", recipe.output.description(layout))
            .mini
    }

    /**
     * 获取展示该配方的 Gui 物品的 lore.
     */
    private fun recipeItemLore(layout: MenuLayout): List<Component> {
        val sufficientPrefix = layout.getLang("prefix.sufficient") ?: "✔"
        val insufficientPrefix = layout.getLang("prefix.insufficient") ?: "✖"
        val choices = choiceCheckerFlags.map {
            val prefix = if (it.value) sufficientPrefix else insufficientPrefix
            val choice = it.key
            choice.description(layout).replace("<prefix>", prefix)
        }
        val loreStrings = (layout.getLang("recipe.lore") ?: "合成所需材料:\n<choices>")
            .split('\n')
        return loreStrings
            .flatMap { if (it == "<choices>") choices else listOf(it) }
            .map(String::mini)
    }

    /**
     * 获取展示该配方的 Gui 物品.
     */
    fun displayItemStack(layout: MenuLayout): ItemStack {
        val itemStack = recipe.output.displayItemStack()
        itemStack.editMeta { itemMeta ->
            itemMeta.itemName(recipeItemName(layout))
            itemMeta.lore(recipeItemLore(layout))
        }
        return itemStack
    }

    /**
     * 判断另一个 [RecipeMatcherResult] 与本 [RecipeMatcherResult] 是否相同
     */
    fun isSame(other: RecipeMatcherResult?): Boolean {
        if (other == null)
            return false
        if (this.choiceCheckerFlags.size != other.choiceCheckerFlags.size)
            return false
        other.choiceCheckerFlags.forEach { (choice, flag) ->
            if (!this.choiceCheckerFlags.containsKey(choice))
                return false
            if (this.choiceCheckerFlags.getBoolean(choice) != flag)
                return false
        }
        return true
    }
}