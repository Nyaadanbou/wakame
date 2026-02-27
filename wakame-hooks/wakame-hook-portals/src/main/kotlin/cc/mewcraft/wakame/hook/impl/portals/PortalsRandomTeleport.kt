package cc.mewcraft.wakame.hook.impl.portals

import cc.mewcraft.wakame.integration.teleport.RandomTeleport
import io.papermc.paper.math.Position
import net.kyori.adventure.key.Key
import net.thenextlvl.portals.bounds.Bounds
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.entity.Entity
import java.util.*
import java.util.concurrent.CompletableFuture

class PortalsRandomTeleport : RandomTeleport {

    private fun randomTeleportInBounds(entity: Entity, bounds: Bounds): CompletableFuture<Boolean> {
        return try {
            bounds.searchSafeLocation(Random())
                .thenApply { location ->
                    if (location != null) {
                        entity.teleportAsync(location)
                        true
                    } else {
                        false
                    }
                }
                .exceptionally { ex ->
                    false
                }
        } catch (_: Exception) {
            CompletableFuture.completedFuture(false)
        }
    }

    private fun worldKeyToWorld(world: Key): World? {
        return Bukkit.getWorld(world)
    }

    override fun execute(entity: Entity, world: Key, minX: Double, minY: Double, minZ: Double, maxX: Double, maxY: Double, maxZ: Double): CompletableFuture<Boolean> {
        val bukkitWorld = worldKeyToWorld(world) ?: return CompletableFuture.completedFuture(false)
        return execute(entity, bukkitWorld, minX, minY, minZ, maxX, maxY, maxZ)
    }

    override fun execute(entity: Entity, world: World, minX: Double, minY: Double, minZ: Double, maxX: Double, maxY: Double, maxZ: Double): CompletableFuture<Boolean> {
        val bounds = Bounds.factory().of(world, minX.toInt(), minY.toInt(), minZ.toInt(), maxX.toInt(), maxY.toInt(), maxZ.toInt())
        return randomTeleportInBounds(entity, bounds)
    }

    override fun execute(entity: Entity, world: Key, min: Position, max: Position): CompletableFuture<Boolean> {
        val bukkitWorld = worldKeyToWorld(world) ?: return CompletableFuture.completedFuture(false)
        return execute(entity, bukkitWorld, min, max)
    }

    override fun execute(entity: Entity, world: World, min: Position, max: Position): CompletableFuture<Boolean> {
        val bounds = Bounds.factory().of(world, min, max)
        return randomTeleportInBounds(entity, bounds)
    }

    override fun execute(entity: Entity, world: Key, x: Double, y: Double, z: Double, radius: Double, height: Double): CompletableFuture<Boolean> {
        val bukkitWorld = worldKeyToWorld(world) ?: return CompletableFuture.completedFuture(false)
        return execute(entity, bukkitWorld, x, y, z, radius, height)
    }

    override fun execute(entity: Entity, world: World, x: Double, y: Double, z: Double, radius: Double, height: Double): CompletableFuture<Boolean> {
        val center = Position.block(x.toInt(), y.toInt(), z.toInt())
        val bounds = Bounds.factory().radius(world, center, radius.toInt(), height.toInt())
        return randomTeleportInBounds(entity, bounds)
    }

    override fun execute(entity: Entity, world: Key, center: Position, radius: Double, height: Double): CompletableFuture<Boolean> {
        val bukkitWorld = worldKeyToWorld(world) ?: return CompletableFuture.completedFuture(false)
        return execute(entity, bukkitWorld, center, radius, height)
    }

    override fun execute(entity: Entity, world: World, center: Position, radius: Double, height: Double): CompletableFuture<Boolean> {
        val bounds = Bounds.factory().radius(world, center, radius.toInt(), height.toInt())
        return randomTeleportInBounds(entity, bounds)
    }
}