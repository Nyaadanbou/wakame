package cc.mewcraft.wakame.packet

import cc.mewcraft.wakame.util.toStableFloat
import com.github.retrooper.packetevents.event.PacketListenerAbstract
import com.github.retrooper.packetevents.event.PacketSendEvent
import com.github.retrooper.packetevents.protocol.packettype.PacketType
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerAbilities
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateAttributes
import org.bukkit.GameMode
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import org.slf4j.Logger

private const val BASE_SPEED = 0.1f

class FOVPacketHandler(
    private val logger: Logger,
) : PacketListenerAbstract() {
    override fun onPacketSend(event: PacketSendEvent) {
        if (event.packetType != PacketType.Play.Server.UPDATE_ATTRIBUTES)
            // 不是更新属性包
            return
        val bukkitPlayer = event.player as? Player ?: return // 不是玩家
        val origin = WrapperPlayServerUpdateAttributes(event)
        logger.info("Properties: ${origin.properties.joinToString { it.key }}")
        val allSpeedProperty = origin.properties
            .filter { it.key == Attribute.GENERIC_MOVEMENT_SPEED.key.asString() }

        if (allSpeedProperty.isEmpty())
            // 未找到移动速度属性
            return
        logger.info("Found movement speed property")
        val currentSpeed = allSpeedProperty.sumOf { it.value }.toStableFloat()
        /*
            设定的最大速度阈值，即当玩家的速度超过此值时，玩家的 FovModifier 会被固定为一个值。
            该值是为了防止玩家的 FOV 过大，导致玩家无法正常游戏。
        */
        val settingMaxSpeed = 0.15f

        // 计算 FovModifier
        /*
            此处变量提供的速度为玩家的 FovModifier，
            当玩家的速度为当前的 FovModifier 时，玩家的 FOV 为正常大小。
            当玩家的速度超过当前的 FovModifier时，玩家的 FOV 会被增大，反之则会被缩小
            能改变 Fov 的 FovModifier 范围为 0.0f ~ currentSpeed ~ currentSpeed * 2。
        */
        val fovModifier = if (currentSpeed <= settingMaxSpeed) {
            // 当前速度未超过settingMaxSpeed，FovModifier 为默认值，玩家客户端会根据此值线性调整 FOV
            BASE_SPEED
        } else {
            // 当前速度超过settingMaxSpeed，就将FovModifier为当前速度，这样玩家客户端会永远保持默认 FOV
            // 但是我们不想要将玩家 Fov 恢复到默认，需要的是能继承原有的 FOV 调整，所以我们将 FovModifier 设置成 currentSpeed 减去某个值
            // TODO: 写出一个公式，使得 FovModifier 能够继承原有的 FOV 调整
            currentSpeed // 目前的实现是直接将 FovModifier 设置为当前速度，这样玩家的 FOV 会直接固定为默认
        }

        val abilities = WrapperPlayServerPlayerAbilities(
            bukkitPlayer.isInvulnerable,
            bukkitPlayer.isFlying,
            bukkitPlayer.allowFlight,
            bukkitPlayer.gameMode == GameMode.CREATIVE,
            bukkitPlayer.flySpeed / 2f,
            fovModifier
        )
        event.user.sendPacketSilently(abilities).also { logger.info("Your speed: $currentSpeed, Base speed: ${abilities.fovModifier}") }
    }
}