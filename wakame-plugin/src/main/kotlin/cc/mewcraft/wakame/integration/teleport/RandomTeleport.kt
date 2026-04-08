package cc.mewcraft.wakame.integration.teleport

import io.papermc.paper.math.Position
import net.kyori.adventure.key.Key
import org.bukkit.World
import org.bukkit.entity.Entity
import java.util.concurrent.CompletableFuture


/**
 * 该接口提供随机传送实体的功能.
 */
interface RandomTeleport {

    fun execute(entity: Entity, world: Key, minX: Double, minY: Double, minZ: Double, maxX: Double, maxY: Double, maxZ: Double): CompletableFuture<Boolean>
    fun execute(entity: Entity, world: World, minX: Double, minY: Double, minZ: Double, maxX: Double, maxY: Double, maxZ: Double): CompletableFuture<Boolean>
    fun execute(entity: Entity, world: Key, min: Position, max: Position): CompletableFuture<Boolean>
    fun execute(entity: Entity, world: World, min: Position, max: Position): CompletableFuture<Boolean>
    fun execute(entity: Entity, world: Key, x: Double, y: Double, z: Double, radius: Double, height: Double): CompletableFuture<Boolean>
    fun execute(entity: Entity, world: World, x: Double, y: Double, z: Double, radius: Double, height: Double): CompletableFuture<Boolean>
    fun execute(entity: Entity, world: Key, center: Position, radius: Double, height: Double): CompletableFuture<Boolean>
    fun execute(entity: Entity, world: World, center: Position, radius: Double, height: Double): CompletableFuture<Boolean>

    companion object Impl : RandomTeleport {

        private var implementation: RandomTeleport = object : RandomTeleport {
            override fun execute(entity: Entity, world: Key, minX: Double, minY: Double, minZ: Double, maxX: Double, maxY: Double, maxZ: Double): CompletableFuture<Boolean> = CompletableFuture.completedFuture(false)
            override fun execute(entity: Entity, world: World, minX: Double, minY: Double, minZ: Double, maxX: Double, maxY: Double, maxZ: Double): CompletableFuture<Boolean> = CompletableFuture.completedFuture(false)
            override fun execute(entity: Entity, world: Key, min: Position, max: Position): CompletableFuture<Boolean> = CompletableFuture.completedFuture(false)
            override fun execute(entity: Entity, world: World, min: Position, max: Position): CompletableFuture<Boolean> = CompletableFuture.completedFuture(false)
            override fun execute(entity: Entity, world: Key, x: Double, y: Double, z: Double, radius: Double, height: Double): CompletableFuture<Boolean> = CompletableFuture.completedFuture(false)
            override fun execute(entity: Entity, world: World, x: Double, y: Double, z: Double, radius: Double, height: Double): CompletableFuture<Boolean> = CompletableFuture.completedFuture(false)
            override fun execute(entity: Entity, world: Key, center: Position, radius: Double, height: Double): CompletableFuture<Boolean> = CompletableFuture.completedFuture(false)
            override fun execute(entity: Entity, world: World, center: Position, radius: Double, height: Double): CompletableFuture<Boolean> = CompletableFuture.completedFuture(false)
        }

        fun setImplementation(impl: RandomTeleport) {
            implementation = impl
        }

        override fun execute(entity: Entity, world: Key, minX: Double, minY: Double, minZ: Double, maxX: Double, maxY: Double, maxZ: Double): CompletableFuture<Boolean> {
            return implementation.execute(entity, world, minX, minY, minZ, maxX, maxY, maxZ)
        }

        override fun execute(entity: Entity, world: World, minX: Double, minY: Double, minZ: Double, maxX: Double, maxY: Double, maxZ: Double): CompletableFuture<Boolean> {
            return implementation.execute(entity, world, minX, minY, minZ, maxX, maxY, maxZ)
        }

        override fun execute(entity: Entity, world: Key, min: Position, max: Position): CompletableFuture<Boolean> {
            return implementation.execute(entity, world, min, max)
        }

        override fun execute(entity: Entity, world: World, min: Position, max: Position): CompletableFuture<Boolean> {
            return implementation.execute(entity, world, min, max)
        }

        override fun execute(entity: Entity, world: Key, x: Double, y: Double, z: Double, radius: Double, height: Double): CompletableFuture<Boolean> {
            return implementation.execute(entity, world, x, y, z, radius, height)
        }

        override fun execute(entity: Entity, world: World, x: Double, y: Double, z: Double, radius: Double, height: Double): CompletableFuture<Boolean> {
            return implementation.execute(entity, world, x, y, z, radius, height)
        }

        override fun execute(entity: Entity, world: Key, center: Position, radius: Double, height: Double): CompletableFuture<Boolean> {
            return implementation.execute(entity, world, center, radius, height)
        }

        override fun execute(entity: Entity, world: World, center: Position, radius: Double, height: Double): CompletableFuture<Boolean> {
            return implementation.execute(entity, world, center, radius, height)
        }
    }
}