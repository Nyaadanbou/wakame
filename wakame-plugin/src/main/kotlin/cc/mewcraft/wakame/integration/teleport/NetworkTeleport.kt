package cc.mewcraft.wakame.integration.teleport

import net.kyori.adventure.key.Key
import org.bukkit.entity.Player

/**
 * 该接口提供跨服务器传送玩家的功能.
 */
interface NetworkTeleport {

    fun server(): Result<String>
    fun execute(player: Player, server: String, world: Key, x: Double, y: Double, z: Double, yaw: Float, pitch: Float): Result<Unit>
    fun execute(player: Player, server: String, world: String, x: Double, y: Double, z: Double, yaw: Float, pitch: Float): Result<Unit>

    companion object Impl : NetworkTeleport {

        private var implementation: NetworkTeleport = object : NetworkTeleport {
            override fun server(): Result<String> = Result.failure(NotImplementedError())
            override fun execute(player: Player, server: String, world: String, x: Double, y: Double, z: Double, yaw: Float, pitch: Float): Result<Unit> = Result.failure(NotImplementedError())
            override fun execute(player: Player, server: String, world: Key, x: Double, y: Double, z: Double, yaw: Float, pitch: Float): Result<Unit> = Result.failure(NotImplementedError())
        }

        fun setImplementation(impl: NetworkTeleport) {
            implementation = impl
        }

        override fun server(): Result<String> = implementation.server()
        override fun execute(player: Player, server: String, world: String, x: Double, y: Double, z: Double, yaw: Float, pitch: Float): Result<Unit> = implementation.execute(player, server, world, x, y, z, yaw, pitch)
        override fun execute(player: Player, server: String, world: Key, x: Double, y: Double, z: Double, yaw: Float, pitch: Float): Result<Unit> = implementation.execute(player, server, world, x, y, z, yaw, pitch)
    }
}