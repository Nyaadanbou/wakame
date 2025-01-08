@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.ecs.data

import io.papermc.paper.math.Position
import org.bukkit.util.Vector
import kotlin.math.cos
import kotlin.math.sin

interface ParticlePath {
    fun positionAtProgress(progress: Double): Position
}

/**
 * 直线路径：粒子沿着两点之间的直线运动
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
 * 圆形路径：粒子沿着圆形轨迹运动
 */
data class CirclePath(
    val center: Position,        // 圆心
    val radius: Double,          // 半径
    val axis: Vector,            // 旋转轴（单位向量）
    val startAngle: Double = 0.0, // 起始角度
    val endAngle: Double = Math.PI * 2 // 默认一个完整的圆
) : ParticlePath {

    init {
        // 检查 radius 是否为有效值
        require(radius > 0) { "Radius must be greater than 0." }

        // 检查 axis 是否是一个有效的单位向量
        require(axis.lengthSquared() == 1.0) { "Axis must be a unit vector." }

        // 检查角度是否有效
        require(!startAngle.isNaN() && !endAngle.isNaN()) { "Angles must be valid numbers." }
    }

    // 用 Rodrigues' Rotation Formula 计算旋转后的点
    private fun rotatePoint(p: Vector, axis: Vector, theta: Double): Vector {
        // 如果输入无效（NaN 或无穷大），直接返回原始点
        if (axis.isZero || p.isZero || theta.isNaN() || theta.isInfinite()) {
            return p
        }

        val cosTheta = cos(theta)
        val sinTheta = sin(theta)

        // 使用 Rodrigues' 旋转公式
        return p.clone().apply {
            val dot = this.dot(axis)
            val cross = this.crossProduct(axis)

            // p_rotated = p * cos(theta) + (axis x p) * sin(theta) + axis * (axis . p) * (1 - cos(theta))
            this.multiply(cosTheta)
            cross.multiply(sinTheta)
            axis.multiply(dot * (1 - cosTheta))
            add(cross)
            add(axis)
        }
    }

    override fun positionAtProgress(progress: Double): Position {
        // 计算当前的角度
        val angle = startAngle + (endAngle - startAngle) * progress
        val x = cos(angle) * radius
        val z = sin(angle) * radius
        val y = 0.0

        // 初始点
        val point = Vector(x, y, z)

        // 旋转点，假设旋转轴为单位向量
        val rotatedPoint = rotatePoint(point, axis.normalize(), angle)

        // 最终位置是圆心加上旋转后的点
        val finalX = center.x() + rotatedPoint.x
        val finalY = center.y() + rotatedPoint.y
        val finalZ = center.z() + rotatedPoint.z

        // 返回最终的 Position
        return Position.fine(finalX, finalY, finalZ)
    }
}


/**
 * 螺旋路径：粒子沿着螺旋路径运动
 */
data class SpiralPath(
    val center: Position,
    val radius: Double,
    val height: Double,
    val rotations: Int
) : ParticlePath {
    override fun positionAtProgress(progress: Double): Position {
        val angle = Math.PI * 2 * progress * rotations
        val x = center.x() + cos(angle) * radius
        val z = center.z() + sin(angle) * radius
        val y = center.y() + progress * height
        return Position.fine(x, y, z)
    }
}
