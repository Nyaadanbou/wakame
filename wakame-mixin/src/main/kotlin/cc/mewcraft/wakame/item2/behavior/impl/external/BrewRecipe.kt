package cc.mewcraft.wakame.item2.behavior.impl.external

import cc.mewcraft.wakame.adventure.translator.TranslatableMessages
import cc.mewcraft.wakame.brewery.BrewRecipeManager
import cc.mewcraft.wakame.brewery.BrewRecipeRenderer
import cc.mewcraft.wakame.item2.behavior.InteractionResult
import cc.mewcraft.wakame.item2.behavior.UseContext
import cc.mewcraft.wakame.item2.behavior.impl.SimpleInteract
import cc.mewcraft.wakame.item2.data.ItemDataTypes
import cc.mewcraft.wakame.item2.getData
import cc.mewcraft.wakame.item2.setData
import cc.mewcraft.wakame.util.adventure.SoundSource
import cc.mewcraft.wakame.util.adventure.playSound
import cc.mewcraft.wakame.util.text.arguments
import io.papermc.paper.registry.keys.SoundEventKeys
import net.kyori.adventure.sound.Sound

object BrewRecipe : SimpleInteract {

    /** TODO 简化
     * 当玩家手持一个未揭示的配方并进行使用交互时, 揭示该配方.
     *
     * 这里的“揭示”指的是修改物品上的酒酿配方数据, 将其修改为“已学习”.
     * 注意! 这与玩家自身的状态没有关系, 并非指玩家学会了新的可用配方.
     * 玩家可以无限次数的揭示酒酿配方, 无论他先前有没有揭示过这个酒酿配方.
     * 一个带有酒酿配方的物品, 其“已学习”的状态会影响其最终的物品渲染效果.
     */
    override fun handleSimpleUse(context: UseContext): InteractionResult {
        val itemstack = context.itemstack
        val itemBrewRecipe = itemstack.getData(ItemDataTypes.BREW_RECIPE) ?: return InteractionResult.FAIL

        // 物品上不存在配方数据 - 不处理
        // 配方已被学习 - 返回交互成功
        val player = context.player
        if (itemBrewRecipe.learned) {
            player.sendMessage(TranslatableMessages.MSG_ALREADY_REVEALED_BREW_RECIPE)
            return InteractionResult.SUCCESS
        }

        // 设置配方数据为已被学习
        itemstack.setData(ItemDataTypes.BREW_RECIPE, itemBrewRecipe.copy(learned = true))

        val recipeId = itemBrewRecipe.recipeId
        val recipe = BrewRecipeManager.INSTANCE.get(recipeId)
        if (recipe != null) {
            val lore = BrewRecipeRenderer.INSTANCE.render(recipe)
            lore.forEach { line -> player.sendMessage(line) }
        }

        player.sendMessage(TranslatableMessages.MSG_REVEALED_BREW_RECIPE.arguments(itemBrewRecipe.recipeId))
        player.playSound(Sound.Emitter.self()) {
            type(SoundEventKeys.ITEM_BOOK_PAGE_TURN)
            source(SoundSource.PLAYER)
            volume(2f)
        }
        return InteractionResult.SUCCESS
    }
}