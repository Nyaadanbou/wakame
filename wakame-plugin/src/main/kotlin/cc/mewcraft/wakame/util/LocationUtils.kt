package cc.mewcraft.wakame.util

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.util.Vector
import kotlin.random.Random

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

/**
 * 获取一个随机偏移的 Location。
 *
 * @receiver 原始 Location
 * @param maxOffsetX X 轴最大偏移
 * @param maxOffsetY Y 轴最大偏移
 * @param maxOffsetZ Z 轴最大偏移
 * @return 随机偏移后的 Location
 */
fun Location.randomOffset(maxOffsetX: Double, maxOffsetY: Double, maxOffsetZ: Double): Location {
    // 生成随机偏移量
    val offsetX: Double = (Random.nextDouble() * 2 - 1) * maxOffsetX
    val offsetY: Double = (Random.nextDouble() * 2 - 1) * maxOffsetY
    val offsetZ: Double = (Random.nextDouble() * 2 - 1) * maxOffsetZ

    // 创建并返回新的偏移后的 Location
    return clone().add(offsetX, offsetY, offsetZ)
}