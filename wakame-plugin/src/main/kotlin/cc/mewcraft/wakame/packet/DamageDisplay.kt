package cc.mewcraft.wakame.packet

import cc.mewcraft.wakame.damage.CriticalStrikeState
import cc.mewcraft.wakame.event.NekoEntityDamageEvent
import cc.mewcraft.wakame.extensions.cross
import cc.mewcraft.wakame.extensions.minus
import cc.mewcraft.wakame.extensions.mul
import cc.mewcraft.wakame.extensions.plus
import cc.mewcraft.wakame.extensions.toLocation
import cc.mewcraft.wakame.extensions.toVector3f
import cc.mewcraft.wakame.hologram.Hologram
import cc.mewcraft.wakame.hologram.TextHologramData
import cc.mewcraft.wakame.util.runTaskLater
import net.kyori.adventure.extra.kotlin.text
import net.kyori.adventure.sound.Sound.Emitter
import net.kyori.adventure.sound.Sound.Source
import net.kyori.adventure.sound.Sound.sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.entity.Display
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.joml.Vector3f
import java.util.WeakHashMap
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.random.Random

/**
 * 以悬浮文字显示玩家造成的伤害.
 */
internal class DamageDisplay : Listener {

    companion object {
        // 这些 Vector3f 实例都是可变的, 请注意副作用 !!!
        private val ZERO = Vector3f(0f, 0f, 0f)
        private val UNIT_X = Vector3f(1f, 0f, 0f)
        private val UNIT_Y = Vector3f(0f, 1f, 0f)
        private val UNIT_Z = Vector3f(0f, 0f, 1f)
        private val ONE = Vector3f(1f, 1f, 1f)

        // 用于辅助生成*伪随机*的伤害悬浮文字的坐标位置
        private val RADIAL_POINT_CYCLE = RadialPointCycle(8, 1f)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private fun onWakameEntityDamage(event: NekoEntityDamageEvent) {
        val damager = event.damageSource.causingEntity as? Player ?: return
        val damagee = event.damagee as? LivingEntity ?: return
        val damageValueMap = event.getFinalDamageMap()
        val criticalState = event.getCriticalState()

        val hologramLoc = calculateHologramLocation(damager = damager, damagee = damagee, distance = 3f)
        val hologramText = damageValueMap
            .map { (element, value) ->
                val damageValue = "%.1f".format(value)
                val damageText = text {
                    content(damageValue)
                    element.displayStyles.forEach { applicableApply(it) }
                }
                when (criticalState) {
                    CriticalStrikeState.POSITIVE -> {
                        damager.playSound(sound(Sound.ENTITY_PLAYER_ATTACK_CRIT, Source.PLAYER, 1f, 1f), Emitter.self())
                        text {
                            content("\ud83d\udca5 ")
                            color(TextColor.color(0xff9900))
                            style { decorate(TextDecoration.BOLD) }
                            append(damageText)
                        }
                    }

                    CriticalStrikeState.NEGATIVE -> {
                        text {
                            content("\ud83d\udca5 ")
                            color(TextColor.color(0x02afff))
                            style { decorate(TextDecoration.BOLD) }
                            append(damageText)
                        }
                    }

                    CriticalStrikeState.NONE -> damageText
                }
            }.let { components ->
                Component.join(JoinConfiguration.spaces(), components)
            }

        sendDamageHologram(damager, hologramLoc, hologramText, criticalState)
    }

    /**
     * 计算一个坐标 `C`, 使其落在 [玩家][damager] 的眼睛与 [受伤实体][damagee]
     * 的眼睛的连线 `AB` 上, 并且与玩家的 [眼睛][Player.getEyeLocation]
     * 的距离为 [distance].
     *
     * 具体来说, 我们先找到一个平面 `P`, 使得平面垂直于 `AB` 向量.
     * 平面 `P` 与点 `A` (眼睛) 之间的距离由参数 [distance] 决定.
     *
     * 本算法的优点:
     * - 即使玩家使用远程武器, 也能够清晰的看到伤害信息
     * - 即使玩家零距离攻击怪物, 也能够清晰的看到伤害信息
     * - 无论玩家以什么角度发起攻击, 伤害信息始终都在正前方
     */
    private fun calculateHologramLocation(
        damager: Player,
        damagee: LivingEntity,
        distance: Float,
    ): Location {
        val a = damager.eyeLocation.toVector3f()
        val b = damagee.eyeLocation.toVector3f().apply { y -= damagee.height.toFloat() / 3f } // 降低一点高度, 让数字落在屏幕正中间
        val ab = (b - a).normalize()
        val c0 = a + (ab mul distance)

        // 生成不平行于 AB 的任意向量
        val vx = if (ab.x != 0f || ab.z != 0f) UNIT_Y else UNIT_X

        // 生成平面 P 的基向量
        val v1 = (ab cross vx).normalize()
        val v2 = (ab cross v1).normalize()

        // 生成垂直平面的随机因子
        val r1 = (Random.nextFloat() - .5f) * 1f // *1 就是 -0.5 ~ +0.5, *2 就是 -1.0 ~ +1.0
        val r2 = (Random.nextFloat() - .5f) * 1f

        // 计算 C
        val c = c0 + (v1 mul r1) + (v2 mul r2)

        return c.toLocation(damager.world)
    }

    /**
     * 发送伤害数值的悬浮文字.
     */
    private fun sendDamageHologram(
        hologramViewer: Player,
        hologramLocation: Location,
        damageText: Component,
        criticalStrikeState: CriticalStrikeState,
    ) {
        val hologramData = TextHologramData(
            location = hologramLocation,
            text = damageText,
            background = Color.fromARGB(0),
            hasTextShadow = false,
            textAlignment = TextDisplay.TextAlignment.CENTER,
            isSeeThrough = true
        ).apply {
            this.scale.mul(1.5f) // 初始大小
            this.translation.add(0f, -1f, 0f) // 初始位置偏下
            this.brightness = Display.Brightness(15, 0)
        }
        val hologram = Hologram(hologramData)

        hologram.show(hologramViewer)

        runTaskLater(2) {
            hologramData.apply {
                this.startInterpolation = 0
                this.interpolationDuration = 5
                this.translation.add(0f, .5f, 0f)
                if (criticalStrikeState == CriticalStrikeState.NONE) {
                    this.scale.add(1f, 1f, 1f)
                } else {
                    this.scale.add(3f, 3f, 3f)
                }
            }
            hologram.setEntityData(hologramData)
            hologram.refresh(hologramViewer)
        }

        runTaskLater(8) {
            hologramData.apply {
                this.startInterpolation = 0
                this.interpolationDuration = 20
                this.scale.set(1f, 1f, 1f)
                this.translation.add(0f, 1f, 0f)
            }
            hologram.setEntityData(hologramData)
            hologram.refresh(hologramViewer)
        }

        runTaskLater(32) {
            hologram.hide(hologramViewer)
        }
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
