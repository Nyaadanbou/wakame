package cc.mewcraft.wakame.util.math

import org.bukkit.Location
import org.bukkit.World
import org.joml.Vector3f
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

object Vec3f {
    // 获取常用向量的一系列方法, 每次调用都返回新对象防止污染

    /**
     * 获取零向量.
     * 每次调用都返回新对象.
     */
    fun zero() = Vector3f(0f, 0f, 0f)

    /**
     * 获取单位向量.
     * 每次调用都返回新对象.
     */
    fun one() = Vector3f(1f, 1f, 1f)

    /**
     * 获取X轴正方向单位向量.
     * 每次调用都返回新对象.
     */
    fun unitX() = Vector3f(1f, 0f, 0f)

    /**
     * 获取Y轴正方向单位向量.
     * 每次调用都返回新对象.
     */
    fun unitY() = Vector3f(0f, 1f, 0f)

    /**
     * 获取Z轴正方向单位向量.
     * 每次调用都返回新对象.
     */
    fun unitZ() = Vector3f(0f, 0f, 1f)

    /**
     * 检查三个向量是否构成标准正交基.
     * 即三个向量两两垂直且均为单位向量.
     * 标准高可读性版本.
     */
    fun isOrthonormalBasis(a: Vector3f, b: Vector3f, c: Vector3f, epsilon: Float = 1e-6f): Boolean {
        return a.isUnit(epsilon) && b.isUnit(epsilon) && c.isUnit(epsilon) &&
                a.isOrthogonalTo(b, epsilon) && a.isOrthogonalTo(c, epsilon) && b.isOrthogonalTo(c, epsilon)
    }

    /**
     * 检查三个向量是否构成标准正交基.
     * 即三个向量两两垂直且均为单位向量.
     * 纯标量快速计算版本.
     */
    fun isOrthonormalBasis(
        x1: Float, y1: Float, z1: Float,
        x2: Float, y2: Float, z2: Float,
        x3: Float, y3: Float, z3: Float,
        epsilon: Float = 1e-6f,
    ): Boolean {
        // 单位向量判定
        val aLengthSquared = x1 * x1 + y1 * y1 + z1 * z1
        val bLengthSquared = x2 * x2 + y2 * y2 + z2 * z2
        val cLengthSquared = x3 * x3 + y3 * y3 + z3 * z3
        if (abs(aLengthSquared - 1f) > epsilon) return false
        if (abs(bLengthSquared - 1f) > epsilon) return false
        if (abs(cLengthSquared - 1f) > epsilon) return false

        // 正交性判定
        val ab = x1 * x2 + y1 * y2 + z1 * z2
        val ac = x1 * x3 + y1 * y3 + z1 * z3
        val bc = x2 * x3 + y2 * y3 + z2 * z3
        if (abs(ab) > epsilon) return false
        if (abs(ac) > epsilon) return false
        if (abs(bc) > epsilon) return false

        return true
    }

    /**
     * **返回值将原地修改**.
     * 将向量绕轴 [axis] 旋转 [angleRad] 角度.
     * [axis] 要求为单位向量.
     * 基于罗德里格斯旋转公式 (Rodrigues' rotation formula).
     */
    fun rotateAroundAxis(dest: Vector3f, axis: Vector3f, angleRad: Float): Vector3f {
        val cos = cos(angleRad)
        val sin = sin(angleRad)

        // v
        val vx = dest.x
        val vy = dest.y
        val vz = dest.z

        // k
        val kx = axis.x
        val ky = axis.y
        val kz = axis.z

        // k × v
        val crossX = ky * vz - kz * vy
        val crossY = kz * vx - kx * vz
        val crossZ = kx * vy - ky * vx

        // k · v
        val dot = kx * vx + ky * vy + kz * vz

        val oneMinusCos = 1f - cos

        dest.x = vx * cos + crossX * sin + kx * dot * oneMinusCos
        dest.y = vy * cos + crossY * sin + ky * dot * oneMinusCos
        dest.z = vz * cos + crossZ * sin + kz * dot * oneMinusCos

        return dest
    }

    fun Location.toVector3f(): Vector3f {
        return Vector3f(x.toFloat(), y.toFloat(), z.toFloat())
    }

    fun Vector3f.toLocation(world: World? = null): Location {
        return Location(world, x.toDouble(), y.toDouble(), z.toDouble())
    }
}

fun Vector3f.copy(): Vector3f {
    return Vector3f(this)
}

/**
 * 返回此向量是否为单位向量.
 */
fun Vector3f.isUnit(epsilon: Float = 1e-6f): Boolean {
    return abs(lengthSquared() - 1f) < epsilon
}

/**
 * 返回此向量是否垂直(正交)于 [other] 向量.
 */
fun Vector3f.isOrthogonalTo(other: Vector3f, epsilon: Float = 1e-6f): Boolean {
    return abs(this.dot(other)) < epsilon
}

/**
 * **返回值将原地修改**.
 * 将向量绕轴 [axis] 旋转 [angleRad] 角度.
 * [axis] 要求为单位向量.
 * 基于罗德里格斯旋转公式 (Rodrigues' rotation formula).
 */
fun Vector3f.rotateAroundAxis(axis: Vector3f, angleRad: Float): Vector3f {
    return Vec3f.rotateAroundAxis(this, axis, angleRad)
}

/**
 * 返回此向量绝对值最大的分量的值
 */
fun Vector3f.maxComponentValue(): Float {
    val ax = abs(this.x)
    val ay = abs(this.y)
    val az = abs(this.z)

    val maxAbs = if (ax > ay) {
        if (ax > az) ax else az
    } else {
        if (ay > az) ay else az
    }

    return maxAbs
}