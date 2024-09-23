package cc.mewcraft.wakame.packet

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEffect
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerRemoveEntityEffect
import io.github.retrooper.packetevents.util.SpigotConversionUtil
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import kotlin.experimental.or

fun Player.addFakePotionEffect(effect: PotionEffect) {
    val user = PacketEvents.getAPI().playerManager.getUser(this)

    /*
        0x01: Is ambient - was the effect spawned from a beacon? All beacon-generated effects are ambient. Ambient effects use a different icon in the HUD (blue border rather than gray). If all effects on an entity are ambient, the "Is potion effect ambient" living metadata field should be set to true. Usually should not be enabled.
        0x02: Show particles - should all particles from this effect be hidden? Effects with particles hidden are not included in the calculation of the effect color, and are not rendered on the HUD (but are still rendered within the inventory). Usually should be enabled.
        0x04: Show icon - should the icon be displayed on the client? Usually should be enabled.
        0x08: Blend - should the effect's hard-coded blending be applied? Currently only used in the DARKNESS effect to apply extra void fog and adjust the gamma value for lighting.
     */
    var flags: Byte = 0

    if (effect.isAmbient) {
        flags = flags or 0x01
    }
    if (effect.hasParticles()) {
        flags = flags or 0x02
    }
    if (effect.hasIcon()) {
        flags = flags or 0x04
    }
    if (effect.type == PotionEffectType.DARKNESS) {
        flags = flags or 0x08
    }

    val packet = WrapperPlayServerEntityEffect(
        this.entityId,
        SpigotConversionUtil.fromBukkitPotionEffectType(effect.type),
        effect.amplifier,
        effect.duration,
        flags,
    )

    user.sendPacketSilently(packet)
}

fun Player.removeFakePotionEffect(effectType: PotionEffectType) {
    val user = PacketEvents.getAPI().playerManager.getUser(this)
    val packet = WrapperPlayServerRemoveEntityEffect(
        this.entityId,
        SpigotConversionUtil.fromBukkitPotionEffectType(effectType),
    )

    user.sendPacketSilently(packet)
}
