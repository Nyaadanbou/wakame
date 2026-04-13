package cc.mewcraft.wakame.damage

import cc.mewcraft.lazyconfig.access.ConfigAccess
import cc.mewcraft.lazyconfig.access.entry
import cc.mewcraft.lazyconfig.access.node
import cc.mewcraft.wakame.animation.AnimationData
import cc.mewcraft.wakame.animation.DamageHologramContext
import cc.mewcraft.wakame.event.bukkit.PostprocessDamageEvent
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.util.math.Vec3f
import cc.mewcraft.wakame.util.math.Vec3f.toLocation
import cc.mewcraft.wakame.util.math.Vec3f.toVector3f
import cc.mewcraft.wakame.util.math.copy
import cc.mewcraft.wakame.util.registerEvents
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.random.Random

private val DAMAGE_CONFIG = ConfigAccess["damage/config"]
private val DISPLAY_CONFIG = DAMAGE_CONFIG.node("display2")

/**
 * 该 object 实现了元素伤害显示的功能.
 */
@Init(InitStage.POST_WORLD)
internal object DamageDisplay : Listener {

    // 当前使用的伤害显示动画
    private val normalAnimation by DISPLAY_CONFIG.entry<AnimationData<*>>("normal")
    private val positiveAnimation by DISPLAY_CONFIG.entry<AnimationData<*>>("positive_critical_strike")
    private val negativeAnimation by DISPLAY_CONFIG.entry<AnimationData<*>>("negative_critical_strike")

    @InitFun
    fun init() {
        registerEvents()
    }

    @EventHandler(ignoreCancelled = true)
    fun on(event: PostprocessDamageEvent) {
        val damagee = event.damagee as? LivingEntity ?: return
        val animLocation = calculateHologramLocation1(damagee)

        val context = DamageHologramContext(event)
        when (event.damageMetadata.criticalStrikeMetadata.state) {
            CriticalStrikeState.NONE -> {
                damagee.trackedBy.forEach { player ->
                    normalAnimation.play(player, animLocation, context)
                }
            }

            CriticalStrikeState.POSITIVE -> {
                damagee.trackedBy.forEach { player ->
                    positiveAnimation.play(player, animLocation, context)
                }
            }

            CriticalStrikeState.NEGATIVE -> {
                damagee.trackedBy.forEach { player ->
                    negativeAnimation.play(player, animLocation, context)
                }
            }
        }
    }

    /**
     * 计算一个坐标 `C`, 使其落在 [受伤实体][damagee] 的周围.
     */
    private fun calculateHologramLocation1(
        damagee: LivingEntity,
        radius: Double = 1.0,
    ): Location {
        // 生成随机变量
        val random = ThreadLocalRandom.current()
        val theta = random.nextDouble(0.0, 2 * Math.PI)
        val r = random.nextDouble(0.2, radius)

        // 计算 offset
        val x = r * cos(theta)
        val y = damagee.height
        val z = r * sin(theta)

        val result = damagee.location.apply { add(x, y, z) }

        return result
    }

    /**
     * 计算一个坐标 `C`, 使其落在 [start] 与 [end] 的连线 `AB` 上,
     * 并且坐标 `C` 与 [start] 的距离为 [distance].
     *
     * 具体来说, 我们先找到一个平面 `P`, 使得平面垂直于 `AB` 向量.
     * 平面 `P` 与点 `A` (眼睛) 之间的距离由参数 [distance] 决定.
     *
     * 本算法的优点:
     * - 即使玩家使用远程武器, 也能够清晰的看到伤害信息
     * - 即使玩家零距离攻击怪物, 也能够清晰的看到伤害信息
     * - 无论玩家以什么角度发起攻击, 伤害信息始终都在正前方
     */
    private fun calculateHologramLocation2(
        start: Location,
        end: Location,
        distance: Float,
    ): Location {
        val a = start.toVector3f()
        val b = end.toVector3f()
        val ab = b.sub(a).normalize()
        val c0 = ab.copy().mul(distance).add(a)

        // 生成不平行于 AB 的任意向量
        val vx = if (ab.x != 0f || ab.z != 0f) Vec3f.unitY() else Vec3f.unitX()

        // 生成平面 P 的基向量
        val v1 = ab.copy().cross(vx).normalize()
        val v2 = ab.cross(v1).normalize()

        // 生成垂直平面的随机因子
        val r1 = (Random.nextFloat() - .5f) * 1f // *1 就是 -0.5 ~ +0.5, *2 就是 -1.0 ~ +1.0
        val r2 = (Random.nextFloat() - .5f) * 1f

        // 计算 C
        val c = c0.add(v1.mul(r1)).add(v2.mul(r2))

        return c.toLocation(end.world)
    }
}

// 经过讨论, 不使用这个算法生成坐标.
// 保留这个类, 以便以后某天可能会用到.
internal class RadialPointCycle {
    private val slices: Int
    private val radius: Float
    private val points: List<Pair<Float, Float>>

    /**
     * @param slices 分割数
     * @param radius 半径
     */
    constructor(slices: Int, radius: Float) {
        // 分割数量, 必须为偶数
        this.slices = max(2, if (slices % 2 == 0) slices else slices + 1)
        // 最小半径, 必须大于等于 0
        this.radius = max(0f, radius)
        // 根据给定的参数生成一组均匀分布的点对
        this.points = createPoints(this.slices, this.radius)
    }

    // 每个实体对应的 "trace"
    private val traceMap = WeakHashMap<Entity, Trace>() // race-condition 就算发生也没什么大问题

    // 获取下一个点对
    fun next(viewer: Entity): Pair<Float, Float> {
        val trace = traceMap.getOrPut(viewer) { Trace(index1 = 0, index2 = slices / 2, invert = false) }
        val pair = points[trace.next()]
        return pair
    }

    // 初始化生成一组在圆上均匀分布的点对
    private fun createPoints(slices: Int, radius: Float): List<Pair<Float, Float>> {
        val points = mutableListOf<Pair<Float, Float>>()
        val step = (2 * Math.PI / slices).toFloat()

        for (i in 0 until slices) {
            val angle = step * i
            val r1 = radius * cos(angle)
            val r2 = radius * sin(angle)
            points.add(Pair(r1, r2))
        }

        return points
    }

    /**
     * 封装一些状态, 用于计算下个点对的位置.
     */
    private inner class Trace(
        private var index1: Int,
        private var index2: Int,
        private var invert: Boolean,
    ) {
        fun next(): Int {
            val i1 = index1
            val i2 = index2
            val inv = invert
            val ret = if (inv) i1 else i2

            // 更新状态, 步进到下一个位置
            if (inv) index1 = (i1 + 1) % slices
            else index2 = (i2 + 1) % slices
            invert = !inv

            return ret
        }
    }
}
