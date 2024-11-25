package cc.mewcraft.wakame.util

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.util.Vector

fun Location.isBetweenXZ(min: Location, max: Location): Boolean =
    x in min.x.rangeTo(max.x)
            && z in min.z.rangeTo(max.z)

fun Location.getTargetLocation(distance: Int, ignoreBlocks: Boolean = false): Location {
    val direction: Vector = direction
    world ?: return this

    for (i in 1..distance) {
        val checkLocation = clone().add(direction.clone().multiply(i))
        if (!ignoreBlocks && world.getBlockAt(checkLocation).type != Material.AIR) {
            return checkLocation
        }
    }

    return clone().add(direction.multiply(distance))
}

fun Location.getFirstBlockBelow(): Block? {
    val world = world ?: throw IllegalArgumentException("Location must be in a world")
    var currentY = blockY
    while (currentY >= world.minHeight) {
        val block = world.getBlockAt(blockX, currentY, blockZ)
        if (block.type != Material.AIR || !block.isReplaceable) {
            return block
        }
        currentY--
    }
    return null
}