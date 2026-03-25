package cc.mewcraft.wakame.item.behavior.impl

import cc.mewcraft.wakame.adventure.translator.TranslatableMessages
import cc.mewcraft.wakame.integration.townyboost.TownyBoost
import cc.mewcraft.wakame.item.behavior.InteractionResult
import cc.mewcraft.wakame.item.behavior.UseContext
import cc.mewcraft.wakame.item.getProp
import cc.mewcraft.wakame.item.property.ItemPropTypes

object TownyBoost : SimpleInteract {

    override fun handleSimpleUse(context: UseContext): InteractionResult {
        val itemstack = context.itemstack
        val townyBoost = itemstack.getProp(ItemPropTypes.TOWNY_BOOST) ?: return InteractionResult.PASS
        val player = context.player
        val result = TownyBoost.activate(player)
        when (result) {
            TownyBoost.ActivateResult.SUCCESS -> player.sendMessage(TranslatableMessages.MSG_TOWNY_BOOST_ACTIVATE_SUCCESS)
            TownyBoost.ActivateResult.NOT_IN_TOWN -> player.sendMessage(TranslatableMessages.MSG_ERR_NOT_INSIDE_TOWN)
            TownyBoost.ActivateResult.NO_VIP_GROUP -> player.sendMessage(TranslatableMessages.MSG_TOWNY_BOOST_ACTIVATE_NO_VIP_GROUP)
            TownyBoost.ActivateResult.ALREADY_ACTIVATED -> player.sendMessage(TranslatableMessages.MSG_TOWNY_BOOST_ACTIVATE_ALREADY_ACTIVATED)
        }
        return InteractionResult.SUCCESS
    }
}