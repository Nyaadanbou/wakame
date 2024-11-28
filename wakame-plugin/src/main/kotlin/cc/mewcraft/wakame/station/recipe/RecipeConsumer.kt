package cc.mewcraft.wakame.station.recipe

import org.bukkit.entity.Player

/**
 * 用于从玩家身上消耗掉 [StationRecipe] 的所有要求 (例如: 物品堆叠).
 */
internal object RecipeConsumer {
    fun consume(recipe: StationRecipe, player: Player) {
        val choices = recipe.input

        // 创建 ChoiceConsumerContextMap
        val contextMap = ChoiceConsumerContextMap(player)

        // 提取出同类的 ChoiceConsumer
        val consumers: List<ChoiceConsumer<*>> = choices.map(StationChoice::consumer).distinct()

        // 同类的 ChoiceConsumer 使用同一个上下文.
        // 这里初始化每一类 ChoiceConsumer 的上下文.
        consumers.forEach { consumer: ChoiceConsumer<*> ->
            contextMap[consumer] = consumer.initializeContext(player)
        }

        // 将每个 StationChoice 的消耗添加到对应的上下文
        choices.forEach { choice: StationChoice ->
            choice.consume(contextMap)
        }

        // 遍历每一类 ChoiceConsumer, 执行消耗
        consumers.forEach { consumer: ChoiceConsumer<*> ->
            consumer.applyConsume(player, contextMap)
        }
    }
}