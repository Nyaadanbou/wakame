package cc.mewcraft.wakame.packet

import cc.mewcraft.wakame.attribute.Attributes
import cc.mewcraft.wakame.user.asNekoUser
import cc.mewcraft.wakame.util.toStableFloat
import com.github.retrooper.packetevents.event.PacketListenerAbstract
import com.github.retrooper.packetevents.event.PacketSendEvent
import com.github.retrooper.packetevents.protocol.packettype.PacketType
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerAbilities
import org.bukkit.GameMode
import org.bukkit.entity.Player

private const val DEFAULT_FOV_MODIFIER = 0.1f

private const val SETTING_MAX_SPEED = 0.15f // TODO: 玩家可设置

class FOVPacketHandler : PacketListenerAbstract() {

    override fun onPacketSend(event: PacketSendEvent) {
        if (event.isCancelled)
            return
        val bukkitPlayer = event.player as? Player ?: return // 不是玩家
        val nekoUser = bukkitPlayer.asNekoUser()
        if (event.packetType != PacketType.Play.Server.UPDATE_ATTRIBUTES)
            return
        val currentSpeed = nekoUser.attributeMap.getValue(Attributes.MOVEMENT_SPEED_RATE).toStableFloat()

        val flying = bukkitPlayer.isFlying
        val sprinting = bukkitPlayer.isSprinting
        var defaultSpeed = 1.0f
        if (sprinting) defaultSpeed *= 1.3f
        if (flying) defaultSpeed *= 1.1f
        val maxFov = defaultSpeed * (SETTING_MAX_SPEED / DEFAULT_FOV_MODIFIER) /* 原始公式：defaultSpeed * (SETTING_MAX_SPEED / BASE_SPEED + 1.0f) / 2.0f */

        // 计算 FovModifier
        /*
            此处变量提供的速度为玩家的 FovModifier，
            当玩家的速度为当前的 FovModifier 时，玩家的 FOV 为正常大小。
            当玩家的速度超过当前的 FovModifier时，玩家的 FOV 会被增大，反之则会被缩小
            能改变 Fov 的 FovModifier 范围为 0.0f ~ currentSpeed ~ currentSpeed * 2。
            客户端的最终 Fov 的公式为 f *= (getAttributeValue(Attributes.MOVEMENT_SPEED) / getAbilities().getWalkingSpeed() + 1.0F) / 2.0F;
        */
        val fovModifier = if (currentSpeed <= SETTING_MAX_SPEED) {
            // 当前速度未超过 settingMaxSpeed，FovModifier 为默认值，玩家客户端会根据此值线性调整 FOV
            DEFAULT_FOV_MODIFIER
        } else {
            // 当前速度超过 settingMaxSpeed，就将 FovModifier 为当前速度，这样玩家客户端会永远保持默认 FOV
            // 但是我们不想要将玩家 Fov 恢复到默认，需要的是能继承原有的 FOV 调整，所以我们将 FovModifier 设置成 currentSpeed 减去某个值
            // 通过客户端公式已知 f 与 getAttributeValue(Attributes.MOVEMENT_SPEED) 的关系，可以通过这个关系来计算 getAbilities().getWalkingSpeed() (即 FovModifier) 的值
            // 公式: WALKING_SPEED = currentSpeed / fov * 2 - 1
            currentSpeed / (maxFov) /* 原始公式：currentSpeed / (2 * maxFov - 1) */
        }

        val abilities = WrapperPlayServerPlayerAbilities(
            bukkitPlayer.isInvulnerable,
            flying,
            bukkitPlayer.allowFlight,
            bukkitPlayer.gameMode == GameMode.CREATIVE,
            bukkitPlayer.flySpeed / 2f,
            fovModifier
        )

        event.user.sendPacketSilently(abilities)
    }
}