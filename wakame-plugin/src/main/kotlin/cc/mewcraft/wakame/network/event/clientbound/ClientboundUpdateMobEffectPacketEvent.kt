package cc.mewcraft.wakame.network.event.clientbound

import cc.mewcraft.wakame.network.event.PlayerPacketEvent
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket
import net.minecraft.world.effect.MobEffectInstance
import org.bukkit.entity.Player

class ClientboundUpdateMobEffectPacketEvent(
    player: Player,
    packet: ClientboundUpdateMobEffectPacket,
) : PlayerPacketEvent<ClientboundUpdateMobEffectPacket>(player, packet) {

    var entityId: Int = packet.entityId
        set(value) {
            field = value
            changed = true
        }

    var effect: MobEffectInstance = MobEffectInstance(
        packet.effect,
        packet.effectDurationTicks,
        packet.effectAmplifier,
        packet.isEffectAmbient,
        packet.effectShowsIcon()
    )
        set(value) {
            field = value
            changed = true
        }

    var blend: Boolean = packet.shouldBlend()
        set(value) {
            field = value
            changed = true
        }

    override fun buildChangedPacket(): ClientboundUpdateMobEffectPacket {
        return ClientboundUpdateMobEffectPacket(entityId, effect, blend)
    }

}
