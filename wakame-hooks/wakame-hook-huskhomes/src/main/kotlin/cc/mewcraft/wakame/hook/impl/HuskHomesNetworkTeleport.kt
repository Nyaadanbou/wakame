package cc.mewcraft.wakame.hook.impl

import cc.mewcraft.wakame.integration.teleport.NetworkTeleport
import net.kyori.adventure.key.Key
import net.william278.huskhomes.api.HuskHomesAPI
import net.william278.huskhomes.position.Position
import net.william278.huskhomes.position.World
import net.william278.huskhomes.teleport.Teleport
import org.bukkit.entity.Player
import java.util.*

class HuskHomesNetworkTeleport : NetworkTeleport {

    private val api: HuskHomesAPI
        get() = HuskHomesAPI.getInstance()

    override fun server(): Result<String> {
        return Result.success(api.server)
    }

    override fun execute(player: Player, server: String, world: Key, x: Double, y: Double, z: Double, yaw: Float, pitch: Float): Result<Unit> {
        val user = api.adaptUser(player)
        val target = Position.at(x, y, z, yaw, pitch, World.from(world.value(), UUID.randomUUID()), server)
        api.teleportBuilder()
            .executor(user)
            .teleporter(user)
            .type(Teleport.Type.TELEPORT)
            .target(target)
            .buildAndComplete(false)
        return Result.success(Unit)
    }

    override fun execute(player: Player, server: String, world: String, x: Double, y: Double, z: Double, yaw: Float, pitch: Float): Result<Unit> {
        val user = api.adaptUser(player)
        val target = Position.at(x, y, z, yaw, pitch, World.from(world, UUID.randomUUID()), server)
        api.teleportBuilder()
            .executor(user)
            .teleporter(user)
            .type(Teleport.Type.TELEPORT)
            .target(target)
            .buildAndComplete(false)
        return Result.success(Unit)
    }
}