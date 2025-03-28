@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.ecs.data

import io.papermc.paper.math.Position
import org.bukkit.block.BlockFace
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * 代表了一个粒子路径. 用于控制粒子的图案.
 */
interface ParticlePath {
    /**
     * 根据进度返回粒子的位置.
     */
    fun positionAtProgress(progress: Double): Position
}

/**
 * 固定点路径: 粒子在固定位置运动
 */
data class FixedPath(
    val position: Position
) : ParticlePath {
    override fun positionAtProgress(progress: Double): Position {
        return position
    }
}

/**
 * 直线路径: 粒子沿着两点之间的直线运动
 */
data class LinePath(
    val start: Position,
    val end: Position
) : ParticlePath {
    override fun positionAtProgress(progress: Double): Position {
        val x = start.x() + (end.x() - start.x()) * progress
        val y = start.y() + (end.y() - start.y()) * progress
        val z = start.z() + (end.z() - start.z()) * progress
        return Position.fine(x, y, z)
    }
}

/**
 * 圆形路径: 粒子沿着圆形轨迹运动
 */
data class CirclePath(
    val center: Position,        // 圆心
    val radius: Double,          // 半径
    val axis: BlockFace          // 旋转轴
) : ParticlePath {
    override fun positionAtProgress(progress: Double): Position {
        val angle = 2 * PI * progress
        return when (axis) {
            BlockFace.UP, BlockFace.DOWN -> {
                // 绕Y轴旋转
                val x = center.x() + cos(angle) * radius
                val z = center.z() + sin(angle) * radius
                Position.fine(x, center.y(), z)
            }
            BlockFace.EAST, BlockFace.WEST -> {
                // 绕X轴旋转
                val y = center.y() + cos(angle) * radius
                val z = center.z() + sin(angle) * radius
                Position.fine(center.x(), y, z)
            }
            BlockFace.NORTH, BlockFace.SOUTH -> {
                // 绕Z轴旋转
                val x = center.x() + cos(angle) * radius
                val y = center.y() + sin(angle) * radius
                Position.fine(x, y, center.z())
            }
            else -> throw IllegalArgumentException("Unsupported axis: $axis")
        }
    }
}

/**
 * 螺旋路径: 粒子沿着螺旋路径运动
 */
data class SpiralPath(
    val center: Position,
    val radius: Double,
    val height: Double,
    val rotations: Int
) : ParticlePath {
    override fun positionAtProgress(progress: Double): Position {
        val angle = PI * 2 * progress * rotations
        val x = center.x() + cos(angle) * radius
        val z = center.z() + sin(angle) * radius
        val y = center.y() + progress * height
        return Position.fine(x, y, z)
    }
}
