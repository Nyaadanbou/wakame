package cc.mewcraft.wakame.util.math

import cc.mewcraft.wakame.particle.LinePath
import cc.mewcraft.wakame.particle.ParticleConfiguration
import cc.mewcraft.wakame.particle.ParticleEffect
import cc.mewcraft.wakame.particle.ParticleManager
import cc.mewcraft.wakame.util.math.Vec3f.toLocation
import com.destroystokyo.paper.ParticleBuilder
import org.bukkit.Particle
import org.bukkit.entity.Player
import org.bukkit.util.BoundingBox
import org.joml.Vector3f
import org.joml.plus
import kotlin.math.abs

/**
 * 有向包围盒 (Oriented Bounding Box).
 */
data class OBB(
    private val centerX: Float,
    private val centerY: Float,
    private val centerZ: Float,
    private val axisXX: Float,
    private val axisXY: Float,
    private val axisXZ: Float,
    private val axisYX: Float,
    private val axisYY: Float,
    private val axisYZ: Float,
    private val axisZX: Float,
    private val axisZY: Float,
    private val axisZZ: Float,
    private val halfExtentX: Float,
    private val halfExtentY: Float,
    private val halfExtentZ: Float,
) {
    /**
     * 该 OBB 的中心点位置向量.
     * 每次调用都返回新对象.
     */
    val center: Vector3f
        get() = Vector3f(centerX, centerY, centerZ)

    /**
     * 该 OBB 所在局部坐标系 X 方向的单位向量.
     * 构造时已保证构成标准正交基.
     * 每次调用都返回新对象.
     */
    val axisX: Vector3f
        get() = Vector3f(axisXX, axisXY, axisXZ)

    /**
     * 该 OBB 所在局部坐标系 Y 方向的单位向量.
     * 构造时已保证构成标准正交基.
     * 每次调用都返回新对象.
     */
    val axisY: Vector3f
        get() = Vector3f(axisYX, axisYY, axisYZ)

    /**
     * 该 OBB 所在局部坐标系 Z 方向的单位向量.
     * 构造时已保证构成标准正交基.
     * 每次调用都返回新对象.
     */
    val axisZ: Vector3f
        get() = Vector3f(axisZX, axisZY, axisZZ)

    /**
     * 该 OBB 在局部坐标系三轴上的半长向量.
     * 每次调用都返回新对象.
     */
    val halfExtents: Vector3f
        get() = Vector3f(halfExtentX, halfExtentY, halfExtentZ)

    init {
        require(halfExtentX > 0 && halfExtentY > 0 && halfExtentZ > 0) { "The half extents of obb must more than 0." }
        require(
            Vec3f.isOrthonormalBasis(
                axisXX, axisXY, axisXZ,
                axisYX, axisYY, axisYZ,
                axisZX, axisZY, axisZZ,
            )
        ) { "The axes must be orthonormal basis." }
    }

    constructor(
        center: Vector3f,
        axisX: Vector3f,
        axisY: Vector3f,
        axisZ: Vector3f,
        halfExtents: Vector3f,
    ) : this(
        center.x, center.y, center.z,
        axisX.x, axisX.y, axisX.z,
        axisY.x, axisY.y, axisY.z,
        axisZ.x, axisZ.y, axisZ.z,
        halfExtents.x, halfExtents.y, halfExtents.z,
    )

    constructor(
        center: Vector3f, axes: Triple<Vector3f, Vector3f, Vector3f>, halfExtents: Vector3f,
    ) : this(
        center, axes.first, axes.second, axes.third, halfExtents
    )

    /**
     * 检查该 OBB 是否与 [other] 碰撞.
     * 基于分离轴定理 (Separating Axis Theorem, SAT).
     * 纯标量快速计算版本.
     */
    fun isCollideFast(other: OBB, epsilon: Float = 1e-6f): Boolean {
        // OBB A 的轴 (Ai)
        val a0x = this.axisXX;
        val a0y = this.axisXY;
        val a0z = this.axisXZ
        val a1x = this.axisYX;
        val a1y = this.axisYY;
        val a1z = this.axisYZ
        val a2x = this.axisZX;
        val a2y = this.axisZY;
        val a2z = this.axisZZ

        // OBB B 的轴 (Bj)
        val b0x = other.axisXX;
        val b0y = other.axisXY;
        val b0z = other.axisXZ
        val b1x = other.axisYX;
        val b1y = other.axisYY;
        val b1z = other.axisYZ
        val b2x = other.axisZX;
        val b2y = other.axisZY;
        val b2z = other.axisZZ

        // 半长
        val ae0 = this.halfExtentX;
        val ae1 = this.halfExtentY;
        val ae2 = this.halfExtentZ
        val be0 = other.halfExtentX;
        val be1 = other.halfExtentY;
        val be2 = other.halfExtentZ

        // 旋转矩阵 R = Ai · Bj
        val r00 = a0x * b0x + a0y * b0y + a0z * b0z
        val r01 = a0x * b1x + a0y * b1y + a0z * b1z
        val r02 = a0x * b2x + a0y * b2y + a0z * b2z

        val r10 = a1x * b0x + a1y * b0y + a1z * b0z
        val r11 = a1x * b1x + a1y * b1y + a1z * b1z
        val r12 = a1x * b2x + a1y * b2y + a1z * b2z

        val r20 = a2x * b0x + a2y * b0y + a2z * b0z
        val r21 = a2x * b1x + a2y * b1y + a2z * b1z
        val r22 = a2x * b2x + a2y * b2y + a2z * b2z

        // 绝对值矩阵
        val ar00 = abs(r00) + epsilon
        val ar01 = abs(r01) + epsilon
        val ar02 = abs(r02) + epsilon
        val ar10 = abs(r10) + epsilon
        val ar11 = abs(r11) + epsilon
        val ar12 = abs(r12) + epsilon
        val ar20 = abs(r20) + epsilon
        val ar21 = abs(r21) + epsilon
        val ar22 = abs(r22) + epsilon

        // 平移向量 t
        val tx = other.centerX - this.centerX
        val ty = other.centerY - this.centerY
        val tz = other.centerZ - this.centerZ
        val t0 = tx * a0x + ty * a0y + tz * a0z
        val t1 = tx * a1x + ty * a1y + tz * a1z
        val t2 = tx * a2x + ty * a2y + tz * a2z

        // OBB A 的 3 个轴
        var ra: Float = ae0
        var rb: Float = be0 * ar00 + be1 * ar01 + be2 * ar02
        if (abs(t0) > ra + rb) return false
        ra = ae1
        rb = be0 * ar10 + be1 * ar11 + be2 * ar12
        if (abs(t1) > ra + rb) return false
        ra = ae2
        rb = be0 * ar20 + be1 * ar21 + be2 * ar22
        if (abs(t2) > ra + rb) return false

        // OBB B 的 3 个轴
        ra = ae0 * ar00 + ae1 * ar10 + ae2 * ar20
        rb = be0
        if (abs(t0 * r00 + t1 * r10 + t2 * r20) > ra + rb) return false
        ra = ae0 * ar01 + ae1 * ar11 + ae2 * ar21
        rb = be1
        if (abs(t0 * r01 + t1 * r11 + t2 * r21) > ra + rb) return false
        ra = ae0 * ar02 + ae1 * ar12 + ae2 * ar22
        rb = be2
        if (abs(t0 * r02 + t1 * r12 + t2 * r22) > ra + rb) return false

        // 9 个叉乘轴
        ra = ae1 * ar20 + ae2 * ar10
        rb = be1 * ar02 + be2 * ar01
        if (abs(t2 * r10 - t1 * r20) > ra + rb) return false
        ra = ae1 * ar21 + ae2 * ar11
        rb = be0 * ar02 + be2 * ar00
        if (abs(t2 * r11 - t1 * r21) > ra + rb) return false
        ra = ae1 * ar22 + ae2 * ar12
        rb = be0 * ar01 + be1 * ar00
        if (abs(t2 * r12 - t1 * r22) > ra + rb) return false
        ra = ae0 * ar20 + ae2 * ar00
        rb = be1 * ar12 + be2 * ar11
        if (abs(t0 * r20 - t2 * r00) > ra + rb) return false
        ra = ae0 * ar21 + ae2 * ar01
        rb = be0 * ar12 + be2 * ar10
        if (abs(t0 * r21 - t2 * r01) > ra + rb) return false
        ra = ae0 * ar22 + ae2 * ar02
        rb = be0 * ar11 + be1 * ar10
        if (abs(t0 * r22 - t2 * r02) > ra + rb) return false
        ra = ae0 * ar10 + ae1 * ar00
        rb = be1 * ar22 + be2 * ar21
        if (abs(t1 * r00 - t0 * r10) > ra + rb) return false
        ra = ae0 * ar11 + ae1 * ar01
        rb = be0 * ar22 + be2 * ar20
        if (abs(t1 * r01 - t0 * r11) > ra + rb) return false
        ra = ae0 * ar12 + ae1 * ar02
        rb = be0 * ar21 + be1 * ar20
        if (abs(t1 * r02 - t0 * r12) > ra + rb) return false

        return true
    }


    /**
     * 检查该 OBB 是否与 [other] 碰撞.
     * 基于分离轴定理 (Separating Axis Theorem, SAT).
     * 标准高可读性版本.
     */
    fun isCollide(other: OBB, epsilon: Float = 1e-6f): Boolean {
        val axes1 = listOf(this.axisX, this.axisY, this.axisZ)
        val axes2 = listOf(other.axisX, other.axisY, other.axisZ)

        val axes = mutableListOf<Vector3f>().apply {
            // 两个OBB的所有轴
            addAll(axes1)
            addAll(axes2)

            // 所有叉乘轴
            for (axis1 in axes1) {
                for (axis2 in axes2) {
                    val cross = axis1.copy().cross(axis2)
                    // 忽略接近零的叉乘轴
                    if (cross.lengthSquared() < epsilon) continue
                    add(cross.normalize())
                }
            }
        }

        // 由OBB1中心点指向OBB2中心点的向量
        val centerDiffVector = other.center.sub(this.center)

        // 遍历所有分离轴
        for (axis in axes) {
            val projectionLength1 = obbProjectionLength(this, axis)
            val projectionLength2 = obbProjectionLength(other, axis)
            val centerProjectionLength = abs(centerDiffVector.dot(axis))

            // 两中心点间向量投影长度 大于 两OBB投影长度之和
            // 说明存在一条分离轴, 判定为未碰撞
            if (centerProjectionLength > projectionLength1 + projectionLength2 + epsilon) {
                return false
            }
        }

        // 找不到任何分离轴, 判定为碰撞
        return true
    }

    /**
     * 计算 [obb] 在指定轴上的投影长度.
     */
    private fun obbProjectionLength(obb: OBB, axis: Vector3f): Float {
        return obb.halfExtentX * abs(obb.axisX.dot(axis)) +
                obb.halfExtentY * abs(obb.axisY.dot(axis)) +
                obb.halfExtentZ * abs(obb.axisZ.dot(axis))
    }

    /**
     * 检查该 OBB 是否与 [aabb] 碰撞.
     * 纯标量快速计算版本.
     */
    fun isCollideFast(aabb: BoundingBox, epsilon: Float = 1e-6f): Boolean {
        // OBB
        val ocx = this.centerX
        val ocy = this.centerY
        val ocz = this.centerZ
        val a0x = this.axisXX;
        val a0y = this.axisXY;
        val a0z = this.axisXZ
        val a1x = this.axisYX;
        val a1y = this.axisYY;
        val a1z = this.axisYZ
        val a2x = this.axisZX;
        val a2y = this.axisZY;
        val a2z = this.axisZZ
        val oex = this.halfExtentX
        val oey = this.halfExtentY
        val oez = this.halfExtentZ

        // AABB
        val acx = ((aabb.minX + aabb.maxX) * 0.5).toFloat()
        val acy = ((aabb.minY + aabb.maxY) * 0.5).toFloat()
        val acz = ((aabb.minZ + aabb.maxZ) * 0.5).toFloat()
        val aex = ((aabb.maxX - aabb.minX) * 0.5).toFloat()
        val aey = ((aabb.maxY - aabb.minY) * 0.5).toFloat()
        val aez = ((aabb.maxZ - aabb.minZ) * 0.5).toFloat()

        // 平移向量 t
        val tx = acx - ocx
        val ty = acy - ocy
        val tz = acz - ocz
        val t0 = tx * a0x + ty * a0y + tz * a0z
        val t1 = tx * a1x + ty * a1y + tz * a1z
        val t2 = tx * a2x + ty * a2y + tz * a2z

        // 绝对值矩阵
        val ar00 = abs(a0x) + epsilon
        val ar01 = abs(a0y) + epsilon
        val ar02 = abs(a0z) + epsilon
        val ar10 = abs(a1x) + epsilon
        val ar11 = abs(a1y) + epsilon
        val ar12 = abs(a1z) + epsilon
        val ar20 = abs(a2x) + epsilon
        val ar21 = abs(a2y) + epsilon
        val ar22 = abs(a2z) + epsilon

        // OBB 的 3 个轴
        var ra: Float = oex
        var rb: Float = aex * ar00 + aey * ar01 + aez * ar02
        if (abs(t0) > ra + rb) return false
        ra = oey
        rb = aex * ar10 + aey * ar11 + aez * ar12
        if (abs(t1) > ra + rb) return false
        ra = oez
        rb = aex * ar20 + aey * ar21 + aez * ar22
        if (abs(t2) > ra + rb) return false

        // AABB 的 3 个轴
        ra = oex * ar00 + oey * ar10 + oez * ar20
        rb = aex
        if (abs(tx) > ra + rb) return false
        ra = oex * ar01 + oey * ar11 + oez * ar21
        rb = aey
        if (abs(ty) > ra + rb) return false
        ra = oex * ar02 + oey * ar12 + oez * ar22
        rb = aez
        if (abs(tz) > ra + rb) return false

        // 9 个叉乘轴
        ra = oey * ar20 + oez * ar10
        rb = aey * ar02 + aez * ar01
        if (abs(t2 * a1x - t1 * a2x) > ra + rb) return false
        ra = oey * ar21 + oez * ar11
        rb = aex * ar02 + aez * ar00
        if (abs(t2 * a1y - t1 * a2y) > ra + rb) return false
        ra = oey * ar22 + oez * ar12
        rb = aex * ar01 + aey * ar00
        if (abs(t2 * a1z - t1 * a2z) > ra + rb) return false
        ra = oex * ar20 + oez * ar00
        rb = aey * ar12 + aez * ar11
        if (abs(t0 * a2x - t2 * a0x) > ra + rb) return false
        ra = oex * ar21 + oez * ar01
        rb = aex * ar12 + aez * ar10
        if (abs(t0 * a2y - t2 * a0y) > ra + rb) return false
        ra = oex * ar22 + oez * ar02
        rb = aex * ar11 + aey * ar10
        if (abs(t0 * a2z - t2 * a0z) > ra + rb) return false
        ra = oex * ar10 + oey * ar00
        rb = aey * ar22 + aez * ar21
        if (abs(t1 * a0x - t0 * a1x) > ra + rb) return false
        ra = oex * ar11 + oey * ar01
        rb = aex * ar22 + aez * ar20
        if (abs(t1 * a0y - t0 * a1y) > ra + rb) return false
        ra = oex * ar12 + oey * ar02
        rb = aex * ar21 + aey * ar20
        if (abs(t1 * a0z - t0 * a1z) > ra + rb) return false

        return true
    }

    /**
     * 使用末地烛粒子绘制盒体边界线框.
     * 用于调试.
     */
    fun drawWireframe(viewer: Player) {
        val vertices = getVertices().map { it.toLocation(viewer.world) }

        val edges = arrayOf(
            0 to 1, 1 to 2, 2 to 3, 3 to 0,
            4 to 5, 5 to 6, 6 to 7, 7 to 4,
            0 to 4, 1 to 5, 2 to 6, 3 to 7
        )
        for ((i, j) in edges) {
            val start = vertices[i]
            val end = vertices[j]
            ParticleManager.addEffect(
                viewer.world,
                ParticleEffect(
                    viewer.world,
                    ParticleConfiguration(
                        builder = { location ->
                            ParticleBuilder(Particle.END_ROD)
                                .location(location)
                                .receivers(listOf(viewer))
                                .extra(.0)
                        },
                        path = LinePath(start, end),
                        count = (start.distance(end) / 0.25).toInt(),
                        times = 1
                    )
                )
            )
        }
    }

    /**
     * 计算盒体的八个顶点坐标.
     */
    private fun getVertices(): List<Vector3f> {
        val signs = arrayOf(-1f, 1f)
        val vertices = mutableListOf<Vector3f>()
        for (dx in signs) {
            for (dy in signs) {
                for (dz in signs) {
                    val vertex = center
                        .plus(axisX.mul(dx * halfExtentX))
                        .plus(axisY.mul(dy * halfExtentY))
                        .plus(axisZ.mul(dz * halfExtentZ))
                    vertices.add(vertex)
                }
            }
        }
        return vertices
    }
}