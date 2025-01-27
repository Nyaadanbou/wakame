package cc.mewcraft.wakame.craftingstation.recipe

import cc.mewcraft.wakame.gui.BasicMenuSettings
import cc.mewcraft.wakame.util.itemNameOrType
import it.unimi.dsi.fastutil.objects.Reference2BooleanArrayMap
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
        // 这里初始化每类 ChoiceChecker 的上下文.
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
 * 代表一个合成站的配方匹配器的匹配结果.
 *
 * @param recipe 与该结果所关联的合成配方
 * @param matchResultMap 匹配的结果映射: [合成配方的输入材料][RecipeChoice] -> [输入材料是否满足][Boolean]
 */
internal class RecipeMatcherResult(
    val recipe: Recipe,
    matchResultMap: Map<RecipeChoice, Boolean>,
) {
    // explicit copy
    private val fastMatchResultMap: Reference2BooleanArrayMap<RecipeChoice> = Reference2BooleanArrayMap(matchResultMap)

    /**
     * 检查该匹配结果是否全部通过? 如果返回 `true` 则允许玩家使用该配方.
     */
    val isAllowed: Boolean = !this.fastMatchResultMap.containsValue(false)

    /**
     * 获取展示该配方的 Gui 物品堆叠.
     */
    fun getListingDisplay(settings: BasicMenuSettings): ItemStack {
        // 使用配方的 [输出物品堆叠] 作为 [展示物品堆叠]
        val itemStack = recipe.output.displayItemStack(settings)

        // 解析展示用的所有占位符
        val slotDisplayResolved = settings.getSlotDisplay("listing").resolveEverything {

            // 适用于整个物品堆叠的占位符
            standard {
                component("item_name", itemStack.itemNameOrType)
                parsed("ok", dict("ok"))
                parsed("bad", dict("bad"))
            }

            // 解析折叠占位符 (choice_list)
            folded("choice_list") {
                for ((choice, flag) in fastMatchResultMap) {
                    when (choice) {
                        is ExpChoice -> {
                            // 以字典里的 "choice_exp" 为模板生成最终的解析结果, 并添加到折叠占位符中
                            resolve("choice_exp") {
                                preprocess { replace("requirement_mark", if (flag) "ok" else "bad") }
                                component("amount", Component.text(choice.amount))
                            }
                        }

                        is ItemChoice -> {
                            // 类似上面的 "choice_exp", 只不过这里是 "choice_item"
                            resolve("choice_item") {
                                preprocess { replace("requirement_mark", if (flag) "ok" else "bad") }
                                parsed("item", choice.item.displayName())
                                component("amount", Component.text(choice.amount))
                            }
                        }
                    }
                }
            }
        }

        // 应用解析结果到物品堆叠上, 并返回
        return slotDisplayResolved.applyTo(itemStack)
    }

    /**
     * 判断另一个 [RecipeMatcherResult] 与本 [RecipeMatcherResult] 是否相同.
     */
    fun isSameResult(other: RecipeMatcherResult?): Boolean {
        if (other == null)
            return false
        if (this.fastMatchResultMap.size != other.fastMatchResultMap.size)
            return false
        other.fastMatchResultMap.forEach { (choice, flag) ->
            if (!this.fastMatchResultMap.containsKey(choice))
                return false
            if (this.fastMatchResultMap.getBoolean(choice) != flag)
                return false
        }
        return true
    }
}