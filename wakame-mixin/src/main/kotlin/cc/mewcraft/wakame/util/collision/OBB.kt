package cc.mewcraft.wakame.util.collision

import cc.mewcraft.wakame.ecs.Fleks
import cc.mewcraft.wakame.ecs.component.ParticleEffect
import cc.mewcraft.wakame.ecs.data.LinePath
import cc.mewcraft.wakame.ecs.data.ParticleConfiguration
import cc.mewcraft.wakame.util.*
import com.destroystokyo.paper.ParticleBuilder
import org.bukkit.Particle
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.util.BoundingBox
import org.joml.Vector3f
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * 有向包围盒 (Oriented Bounding Box).
 */
data class OBB(
    /**
     * 该 OBB 的中心点.
     */
    val center: Vector3f,

    /**
     * 该 OBB 所在局部坐标系三轴的单位向量. 需保证构成正交基.
     */
    val axes: Array<Vector3f>,

    /**
     * 该 OBB 在局部坐标系三轴上的半长.
     */
    val halfExtents: FloatArray,
) {
    init {
        require(axes.size == 3) { "The amount of OBB axes must be 3." }
        require(halfExtents.size == 3) { "The amount of OBB half extents must be 3." }
        require(halfExtents[0] > 0 && halfExtents[1] > 0 && halfExtents[2] > 0) { "The half extents of obb must more than 0." }
        require(isOrthonormalBasis(axes[0], axes[1], axes[2])) { "The axes must be orthonormal basis." }
    }

    constructor(
        center: Vector3f,
        axis1: Vector3f,
        axis2: Vector3f,
        axis3: Vector3f,
        halfExtent1: Float,
        halfExtent2: Float,
        halfExtent3: Float,
    ) : this(center, arrayOf(axis1, axis2, axis3), floatArrayOf(halfExtent1, halfExtent2, halfExtent3))

    constructor(boundingBox: BoundingBox) : this(
        boundingBox.center.toVector3f(),
        MINECRAFT_ORTHONORMAL_BASIS,
        floatArrayOf(boundingBox.widthX.toFloat(), boundingBox.height.toFloat(), boundingBox.widthZ.toFloat())
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OBB

        if (center != other.center) return false
        if (!axes.contentEquals(other.axes)) return false
        if (!halfExtents.contentEquals(other.halfExtents)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = center.hashCode()
        result = 31 * result + axes.contentHashCode()
        result = 31 * result + halfExtents.contentHashCode()
        return result
    }

    fun isCollide(other: OBB): Boolean {
        return isCollide(this, other)
    }

    fun isCollide(boundingBox: BoundingBox): Boolean {
        return isCollide(OBB(boundingBox))
    }

    fun isCollide(entity: Entity): Boolean {
        return isCollide(entity.boundingBox)
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
            Fleks.INSTANCE.createEntity {
                it += ParticleEffect(
                    world = viewer.world,
                    ParticleConfiguration(
                        builderProvider = { loc ->
                            ParticleBuilder(Particle.END_ROD)
                                .location(loc)
                                .receivers(listOf(viewer))
                                .extra(.0)
                        },
                        particlePath = LinePath(start, end),
                        amount = (start.distance(end) / 0.25).toInt(),
                        times = 1
                    )
                )
            }
        }
    }

    private fun getVertices(): List<Vector3f> {
        val signs = arrayOf(-1f, 1f)
        val vertices = mutableListOf<Vector3f>()
        for (dx in signs) {
            for (dy in signs) {
                for (dz in signs) {
                    val vertex = center +
                            (axes[0] mul (dx * halfExtents[0])) +
                            (axes[1] mul (dy * halfExtents[1])) +
                            (axes[2] mul (dz * halfExtents[2]))
                    vertices.add(vertex)
                }
            }
        }
        return vertices
    }
}

/**
 * 检查两个OBB是否碰撞.
 * 基于分离轴定理 (Separating Axis Theorem, SAT).
 */
fun isCollide(obb1: OBB, obb2: OBB): Boolean {
    val axes = mutableListOf<Vector3f>().apply {
        // 两个OBB的所有轴
        addAll(obb1.axes)
        addAll(obb2.axes)

        // 所有叉乘轴
        for (axis1 in obb1.axes) {
            for (axis2 in obb2.axes) {
                val cross = axis1 cross axis2
                // 忽略接近零的叉乘轴
                if (cross.lengthSquared() < 1e-6f) continue
                add(cross.normalize())
            }
        }
    }

    // 由OBB1中心点指向OBB2中心点的向量
    val centerDiffVector = obb2.center - obb1.center

    // 遍历所有分离轴
    for (axis in axes) {
        val projectionLength1 = obbProjectionLength(obb1, axis)
        val projectionLength2 = obbProjectionLength(obb2, axis)
        val centerProjectionLength = abs(centerDiffVector dot axis)

        // 两中心点间向量投影长度 大于 两OBB投影长度之和
        // 说明存在一条分离轴, 判定为未碰撞
        if (centerProjectionLength > projectionLength1 + projectionLength2 + 1e-6f) {
            return false
        }
    }
    // 找不到任何分离轴, 判定为碰撞
    return true
}

// 这些 Vector3f 实例都是可变的, 请注意副作用 !!!
private val ZERO = Vector3f(0f, 0f, 0f)
private val UNIT_X = Vector3f(1f, 0f, 0f)
private val UNIT_Y = Vector3f(0f, 1f, 0f)
private val UNIT_Z = Vector3f(0f, 0f, 1f)
private val ONE = Vector3f(1f, 1f, 1f)

fun calculateOrthonormalBasis(entity: Entity, angle: Float = 0f): Array<Vector3f> {
    // 小于浮点误差认为为0, 不额外计算旋转.
    return if (abs(angle) < 1e-6f) {
        calculateOrthonormalBasis(entity.yaw, entity.pitch)
    } else {
        calculateOrthonormalBasis(entity.yaw, entity.pitch, angle)
    }
}

/**
 * 基于实体视角计算标准正交基.
 * 单位使用角度制.
 * 必须用 [yaw] 和 [pitch] 计算, 不能直接通过视线向量与世界上向量叉乘得右向量.
 * 否则会导致叉乘结果为0向量, 无法处理实体看向正上方和正下方的情况.
 */
fun calculateOrthonormalBasis(yaw: Float, pitch: Float): Array<Vector3f> {
    val yawRad = yaw.toRadians()
    val pitchRad = pitch.toRadians()

    val sinYaw = sin(yawRad)
    val cosYaw = cos(yawRad)
    val sinPitch = sin(pitchRad)
    val cosPitch = cos(pitchRad)

    val forward = Vector3f(-cosPitch * sinYaw, -sinPitch, cosPitch * cosYaw).normalize()
    val right = Vector3f(-cosYaw, 0f, -sinYaw).normalize()
    val up = (right cross forward).normalize()
    return arrayOf(right, up, forward)
}

/**
 * 基于实体视角计算标准正交基.
 * 并在视角平面 (与视线向量垂直的平面) 内旋转 [angle] 角度.
 * 单位使用角度制.
 * 正值为顺时针旋转.
 */
fun calculateOrthonormalBasis(yaw: Float, pitch: Float, angle: Float): Array<Vector3f> {
    val yawRad = yaw.toRadians()
    val pitchRad = pitch.toRadians()

    val sinYaw = sin(yawRad)
    val cosYaw = cos(yawRad)
    val sinPitch = sin(pitchRad)
    val cosPitch = cos(pitchRad)

    val forward = Vector3f(-cosPitch * sinYaw, -sinPitch, cosPitch * cosYaw).normalize()
    val right = Vector3f(-cosYaw, 0f, -sinYaw)
    val rotatedRight = rotateVectorAroundAxis(right, forward, angle).normalize()
    val up = (rotatedRight cross forward).normalize()
    return arrayOf(rotatedRight, up, forward)
}

/**
 * Minecraft 世界坐标系下的标准正交基.
 */
val MINECRAFT_ORTHONORMAL_BASIS: Array<Vector3f> = arrayOf(UNIT_X, UNIT_Y, UNIT_Z)

/**
 * 计算 [obb] 在指定轴上的投影长度.
 */
private fun obbProjectionLength(obb: OBB, axis: Vector3f): Float {
    return obb.halfExtents[0] * abs(obb.axes[0] dot axis) +
            obb.halfExtents[1] * abs(obb.axes[1] dot axis) +
            obb.halfExtents[2] * abs(obb.axes[2] dot axis)
}

// TODO 我觉得应该搞个 MathUtils
/**
 * 检查三个向量是否构成标准正交基.
 * 即三个向量两两垂直且均为单位向量.
 */
private fun isOrthonormalBasis(a: Vector3f, b: Vector3f, c: Vector3f, epsilon: Float = 1e-6f): Boolean {
    return a.isUnit(epsilon) && b.isUnit(epsilon) && c.isUnit(epsilon) &&
            a.isOrthogonalTo(b, epsilon) && a.isOrthogonalTo(c, epsilon) && b.isOrthogonalTo(c, epsilon)
}

/**
 * 将向量 [v] 绕轴 [axis] 旋转 [angle] 角度.
 * [angle] 单位使用角度制.
 * 基于罗德里格斯旋转公式 (Rodrigues' rotation formula).
 */
private fun rotateVectorAroundAxis(v: Vector3f, axis: Vector3f, angle: Float): Vector3f {
    val angleRad = angle.toRadians()
    val cos = cos(angleRad)
    val sin = sin(angleRad)
    val axisNorm = axis.copy().normalize()

    return (v mul cos) + ((axisNorm cross v) mul sin) + (axisNorm mul ((axisNorm dot v) * (1 - cos)))
}

/**
 * 单精度浮点型的圆周率.
 */
private const val PI_FLOAT: Float = PI.toFloat()

/**
 * 把角度制转为弧度制.
 */
private fun Float.toRadians(): Float = this * (PI_FLOAT / 180f)

/**
 * 把弧度制转为角度制.
 */
private fun Float.toDegrees(): Float = this * (180f / PI_FLOAT)