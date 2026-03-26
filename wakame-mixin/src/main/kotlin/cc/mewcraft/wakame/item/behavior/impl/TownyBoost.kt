package cc.mewcraft.wakame.item.behavior.impl

import cc.mewcraft.wakame.adventure.translator.TranslatableMessages
import cc.mewcraft.wakame.integration.townyboost.TownyBoost
import cc.mewcraft.wakame.item.behavior.InteractionResult
import cc.mewcraft.wakame.item.behavior.UseContext
import cc.mewcraft.wakame.item.getProp
import cc.mewcraft.wakame.item.property.ItemPropTypes
import cc.mewcraft.wakame.util.adventure.BukkitSound
import cc.mewcraft.wakame.util.adventure.SoundSource
import cc.mewcraft.wakame.util.adventure.playSound
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component

object TownyBoost : SimpleInteract {

    override fun handleSimpleUse(context: UseContext): InteractionResult {
        val itemstack = context.itemstack
        val townyBoost = itemstack.getProp(ItemPropTypes.TOWNY_BOOST) ?: return InteractionResult.PASS
        val player = context.player
        when (val result = TownyBoost.activate(player)) {
            is TownyBoost.ActivateResult.Success -> {
                player.sendMessage(TranslatableMessages.MSG_TOWNY_BOOST_ACTIVATE_SUCCESS.arguments(Component.text(result.townName)))
                player.playSound(Sound.Emitter.self()) {
                    type(BukkitSound.BLOCK_BEACON_ACTIVATE)
                    source(SoundSource.PLAYER)
                }
            }

            is TownyBoost.ActivateResult.NotInTown -> {
                player.sendMessage(TranslatableMessages.MSG_ERR_NOT_INSIDE_TOWN)
                player.playSound(Sound.Emitter.self()) {
                    type(BukkitSound.BLOCK_BEACON_DEACTIVATE)
                    source(SoundSource.PLAYER)
                }
            }

            is TownyBoost.ActivateResult.NoVipGroup -> {
                player.sendMessage(TranslatableMessages.MSG_TOWNY_BOOST_ACTIVATE_NO_VIP_GROUP)
                player.playSound(Sound.Emitter.self()) {
                    type(BukkitSound.BLOCK_BEACON_DEACTIVATE)
                    source(SoundSource.PLAYER)
                }
            }

            is TownyBoost.ActivateResult.AlreadyActivated -> {
                player.sendMessage(TranslatableMessages.MSG_TOWNY_BOOST_ACTIVATE_ALREADY_ACTIVATED.arguments(Component.text(result.townName)))
                player.playSound(Sound.Emitter.self()) {
                    type(BukkitSound.BLOCK_BEACON_AMBIENT)
                    source(SoundSource.PLAYER)
                    pitch(1.2f)
                }
            }
        }
        return InteractionResult.SUCCESS
    }
}