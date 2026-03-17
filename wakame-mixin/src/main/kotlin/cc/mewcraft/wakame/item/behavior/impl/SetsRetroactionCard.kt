package cc.mewcraft.wakame.item.behavior.impl

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.adventure.translator.TranslatableMessages
import cc.mewcraft.wakame.integration.signin.SignInIntegration
import cc.mewcraft.wakame.item.behavior.BehaviorResult
import cc.mewcraft.wakame.item.behavior.ConsumeContext
import cc.mewcraft.wakame.item.behavior.ItemBehavior
import cc.mewcraft.wakame.item.getProp
import cc.mewcraft.wakame.item.property.ItemPropTypes
import cc.mewcraft.wakame.item.property.impl.SetsRetroactiveCard
import net.kyori.adventure.text.TranslationArgument

object SetsRetroactionCard : ItemBehavior {

    override fun handleConsume(context: ConsumeContext): BehaviorResult {
        val itemstack = context.itemstack
        val setsRetroactiveCard = itemstack.getProp(ItemPropTypes.SETS_RETROACTIVE_CARD) ?: return BehaviorResult.PASS
        val action = setsRetroactiveCard.action
        val amount = setsRetroactiveCard.amount
        val player = context.player
        val playerId = player.uniqueId

        fun shouldCancel(result: Result<*>): Boolean {
            val ex = result.exceptionOrNull()
            if (ex is UnsupportedOperationException) {
                player.sendMessage(TranslatableMessages.MSG_ERR_SIGN_IN_NOT_AVAILABLE)
                return true
            }
            if (ex != null) {
                player.sendMessage(TranslatableMessages.MSG_ERR_INTERNAL_ERROR)
                LOGGER.error("An error occurred while trying to set retroactive card for player ${player.name} ($playerId)", ex)
                return true
            }
            return false
        }

        when (action) {
            SetsRetroactiveCard.Action.ADD -> {
                val result = SignInIntegration.giveRetroactiveCard(playerId, setsRetroactiveCard.amount)
                if (shouldCancel(result)) return BehaviorResult.FINISH_AND_CANCEL
                player.sendMessage(TranslatableMessages.MSG_SIGN_IN_RETROACTIVE_CARD_ADDED.arguments(TranslationArgument.numeric(amount)))
                return BehaviorResult.FINISH
            }

            SetsRetroactiveCard.Action.TAKE -> {
                val result = SignInIntegration.takeRetroactiveCard(playerId, setsRetroactiveCard.amount)
                if (shouldCancel(result)) return BehaviorResult.FINISH_AND_CANCEL
                player.sendMessage(TranslatableMessages.MSG_SIGN_IN_RETROACTIVE_CARD_TAKEN.arguments(TranslationArgument.numeric(amount)))
                return BehaviorResult.FINISH
            }

            SetsRetroactiveCard.Action.SET -> {
                val result = SignInIntegration.setRetroactiveCard(playerId, setsRetroactiveCard.amount)
                if (shouldCancel(result)) return BehaviorResult.FINISH_AND_CANCEL
                player.sendMessage(TranslatableMessages.MSG_SIGN_IN_RETROACTIVE_CARD_SET.arguments(TranslationArgument.numeric(amount)))
                return BehaviorResult.FINISH
            }
        }
    }
}