package cc.mewcraft.wakame.item.behavior.impl

import cc.mewcraft.messaging2.ServerInfoProvider
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.adventure.translator.TranslatableMessages
import cc.mewcraft.wakame.item.behavior.BehaviorResult
import cc.mewcraft.wakame.item.behavior.ConsumeContext
import cc.mewcraft.wakame.item.behavior.ItemBehavior
import cc.mewcraft.wakame.item.getProp
import cc.mewcraft.wakame.item.property.ItemPropTypes
import com.destroystokyo.paper.ParticleBuilder
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.sound.SoundStop
import org.bukkit.Particle
import cc.mewcraft.wakame.integration.teleport.RandomTeleport.Impl as RandomTeleportIntegration

/**
 * 物品消耗后, 将玩家按照预定的参数随机传送到一个位置.
 */
object RandomTeleport : ItemBehavior {

    override fun handleConsume(context: ConsumeContext): BehaviorResult {
        val itemstack = context.itemstack
        val randomTeleport = itemstack.getProp(ItemPropTypes.RANDOM_TELEPORT) ?: return BehaviorResult.PASS
        val player = context.player
        if (ServerInfoProvider.serverKey !in randomTeleport.allowedServers) {
            player.sendMessage(TranslatableMessages.MSG_ERR_CANNOT_USE_RANDOM_TELEPORT_IN_CURRENT_SERVER)
            return BehaviorResult.FINISH_AND_CANCEL
        }
        val world = context.world
        if (world.key !in randomTeleport.allowedDimensions) {
            player.sendMessage(TranslatableMessages.MSG_ERR_CANNOT_USE_RANDOM_TELEPORT_IN_CURRENT_DIMENSION)
            return BehaviorResult.FINISH_AND_CANCEL
        }
        val center = player.location
        val searchRadius = randomTeleport.searchRadius
        val searchHeight = randomTeleport.searchHeight
        val startingSound = randomTeleport.startingSound
        val successSound = randomTeleport.successSound
        val failureSound = randomTeleport.failureSound
        player.sendMessage(TranslatableMessages.MSG_SEARCHING_SAFE_LOCATION_FOR_RANDOM_TELEPORT)
        player.playSound(Sound.sound(startingSound, Sound.Source.PLAYER, .5f, 1f))
        ParticleBuilder(Particle.PORTAL)
            .location(player.location)
            .offset(-2.0, 0.5, 2.0)
            .count(256)
            .receivers(32, false)
            .spawn()
        RandomTeleportIntegration
            .execute(player, world, center, searchRadius, searchHeight)
            .thenAccept { success ->
                if (!success) {
                    player.sendMessage(TranslatableMessages.MSG_ERR_RANDOM_TELEPORT_FOUND_NO_SAFE_LOCATION)
                    player.playSound(Sound.sound(failureSound, Sound.Source.PLAYER, 1f, 1f))
                    player.stopSound(SoundStop.namedOnSource(startingSound, Sound.Source.PLAYER))
                } else {
                    player.playSound(Sound.sound(successSound, Sound.Source.PLAYER, 1f, 1f))
                }
            }
            .exceptionally { ex ->
                LOGGER.error("Failed to execute random teleport integration", ex)
                player.sendMessage(TranslatableMessages.MSG_ERR_INTERNAL_ERROR)
                player.playSound(Sound.sound(failureSound, Sound.Source.PLAYER, 1f, 1f))
                player.stopSound(SoundStop.namedOnSource(startingSound, Sound.Source.PLAYER))
                null
            }
        return BehaviorResult.FINISH
    }
}