package cc.mewcraft.wakame.item2.behavior.impl

import cc.mewcraft.wakame.adventure.translator.TranslatableMessages
import cc.mewcraft.wakame.brewery.BrewRecipeManager
import cc.mewcraft.wakame.brewery.BrewRecipeRenderer
import cc.mewcraft.wakame.item2.behavior.InteractionHand
import cc.mewcraft.wakame.item2.behavior.InteractionResult
import cc.mewcraft.wakame.item2.behavior.UseContext
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
     * 当玩家手持一个未学习的配方并右键时, 学习该配方.
     *
     * 这里的“学习”指的是修改物品上的酒酿配方数据 - 将其修改为“已学习”, 跟玩家自身的状态没有关系.
     * 也就是说玩家可以无限的学习酒酿配方, 无论他先前有没有学习过这个酒酿配方.
     * 一个带有酒酿配方的物品, 其“已学习”的状态会影响其最终的物品渲染效果.
     */
    override fun handleSimpleUse(context: UseContext): InteractionResult {
        val itemStack = context.itemStack
        val player = context.player
        val itemBrewRecipe = itemStack.getData(ItemDataTypes.BREW_RECIPE)
        if (itemBrewRecipe == null) return InteractionResult.FAIL
        if (itemBrewRecipe.learned) {
            player.sendMessage(TranslatableMessages.MSG_ALREADY_REVEALED_BREW_RECIPE)
            return InteractionResult.SUCCESS
        }
        if (context.hand != InteractionHand.MAIN_HAND) return InteractionResult.FAIL

        itemStack.setData(ItemDataTypes.BREW_RECIPE, itemBrewRecipe.copy(learned = true))

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