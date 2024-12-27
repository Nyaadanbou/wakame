package cc.mewcraft.wakame.craftingstation.recipe

import org.bukkit.entity.Player

/**
 * 用于从玩家身上消耗掉 [Recipe] 的所有要求 (例如: 物品堆叠).
 */
internal object RecipeConsumer {
    fun consume(recipe: Recipe, player: Player) {
        val choices = recipe.input

        // 创建 ChoiceConsumerContextMap
        val contextMap = ChoiceConsumerContextMap(player)

        // 提取出同类的 ChoiceConsumer
        val consumers: List<ChoiceConsumer<*>> = choices.map(RecipeChoice::consumer).distinct()

        // 同类的 ChoiceConsumer 使用同一个上下文.
        // 这里初始化每类 ChoiceConsumer 的上下文.
        consumers.forEach { consumer: ChoiceConsumer<*> ->
            contextMap[consumer] = consumer.initCtx(player)
        }

        // 将每个 StationChoice 的消耗添加到对应的上下文
        choices.forEach { choice: RecipeChoice ->
            choice.consume(contextMap)
        }

        // 遍历每一类 ChoiceConsumer, 执行消耗
        consumers.forEach { consumer: ChoiceConsumer<*> ->
            consumer.consume(player, contextMap)
        }
    }
}