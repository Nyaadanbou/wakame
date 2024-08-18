package cc.mewcraft.wakame.station.recipe

import org.bukkit.entity.Player

/**
 * [StationRecipe] 的消耗器.
 * 消耗一特定玩家某配方的各项要求.
 */
object RecipeConsumer {
    fun consume(recipe: StationRecipe, player: Player) {
        val choices = recipe.input

        // 创建ChoiceConsumerContextMap
        val consumerContextMap = ChoiceConsumerContextMap(player)
        // 提取出同类的ChoiceConsumer
        val consumers: List<ChoiceConsumer<*>> =
            choices.map { it.consumer }.distinct()
        // 同类的ChoiceConsumer使用同一个上下文
        // 初始化每一类ChoiceConsumer的上下文
        consumers.forEach { consumer ->
            val context = consumer.initializeContext(player)
            consumerContextMap[consumer] = context
        }

        // 将每个StationChoice的消耗添加到对应的上下文
        choices.forEach {
            it.consume(consumerContextMap)
        }

        // 遍历每一类ChoiceConsumer, 执行消耗
        consumers.forEach {
            it.applyConsume(player, consumerContextMap)
        }
    }
}