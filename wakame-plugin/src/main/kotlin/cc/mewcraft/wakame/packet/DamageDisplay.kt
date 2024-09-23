package cc.mewcraft.wakame.packet

import cc.mewcraft.wakame.event.NekoEntityDamageEvent
import cc.mewcraft.wakame.extensions.*
import cc.mewcraft.wakame.hologram.Hologram
import cc.mewcraft.wakame.hologram.TextHologramData
import cc.mewcraft.wakame.util.runTaskLater
import net.kyori.adventure.extra.kotlin.text
import net.kyori.adventure.sound.Sound.*
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.entity.Display
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.joml.Vector3f
import java.util.WeakHashMap
import kotlin.math.cos
import kotlin.math.sin

/**
 * 以悬浮文字显示玩家造成的伤害.
 */
internal class DamageDisplay : Listener {

    companion object {
        // 这些 Vector3f 实例都是可变的, 请注意副作用 !!!
        private val ZERO = Vector3f(0f, 0f, 0f)
        private val ONE = Vector3f(1f, 1f, 1f)
        private val BASE_I = Vector3f(1f, 0f, 0f)
        private val BASE_J = Vector3f(0f, 1f, 0f)
        private val BASE_K = Vector3f(0f, 0f, 1f)

        // 用于辅助生成*伪随机*的伤害悬浮文字的坐标位置
        private val RADIAL_POINT_GENERATOR = RadialPointGenerator(8, 1f)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private fun onWakameEntityDamage(event: NekoEntityDamageEvent) {
        val damageSource = event.damageSource
        val damager = damageSource.causingEntity as? Player ?: return
        val damageValueMap = event.getFinalDamageMap(excludeZeroDamage = true)
        val isCritical = event.damageMetadata.isCritical

        val hologramLoc = calculateHologramLocation(damager = damager, d = 3f)
        val hologramText = damageValueMap
            .map { (element, value) ->
                val damageValue = "%.1f".format(value)
                val damageText = text {
                    content(damageValue)
                    element.styles.forEach { applicableApply(it) }
                }
                if (isCritical) {
                    damager.playSound(sound(Sound.ENTITY_PLAYER_ATTACK_CRIT, Source.PLAYER, 1f, 1f), Emitter.self())
                    text {
                        content("\ud83d\udca5 ")
                        color(TextColor.color(0xff9900))
                        style { decorate(TextDecoration.BOLD) }
                        append(damageText)
                    }
                } else {
                    damageText
                }
            }.let { components ->
                Component.join(JoinConfiguration.spaces(), components)
            }

        sendDamageHologram(damager, hologramLoc, isCritical, hologramText)
    }

    /**
     * 计算一个坐标 `C`, 使其落在 [玩家][damager] 的视线向量 `AB` 上,
     * 并且保证与玩家的 [眼睛][Player.getEyeLocation] 的距离为 [d].
     *
     * 具体来说, 我们先找到一个平面 `P`, 使得平面垂直于 `AB` 向量.
     * 平面 `P` 与点 `A` (眼睛) 之间的距离由给定的参数 [d] 决定.
     *
     * 本算法的优点: 即使玩家使用远程武器, 也能够清晰的看到伤害值.
     */
    private fun calculateHologramLocation(
        damager: Player,
        d: Float,
    ): Location {
        val a = damager.eyeLocation.toVector3f()
        val ab = damager.eyeLocation.direction.toVector3f()
        val c0 = a + (ab mul d)

        // 生成不平行于 AB 的任意向量
        val vx = if (ab.x != 0f || ab.z != 0f) BASE_J else BASE_I

        // 生成平面 P 的基向量
        val v1 = (ab cross vx).normalize()
        val v2 = (ab cross v1).normalize()

        // 生成垂直平面的随机因子
        val (r1, r2) = RADIAL_POINT_GENERATOR.next(damager)

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
        isCritical: Boolean,
        damageText: Component,
    ) {
        val hologramData = TextHologramData(
            hologramLocation,
            damageText,
            Color.fromARGB(0),
            false,
            TextDisplay.TextAlignment.CENTER,
            true
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
                if (isCritical) {
                    this.scale.add(3f, 3f, 3f)
                } else {
                    this.scale.add(1f, 1f, 1f)
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
                this.translation.add(0f, 2f, 0f)
            }
            hologram.setEntityData(hologramData)
            hologram.refresh(hologramViewer)
        }

        runTaskLater(32) {
            hologram.hide(hologramViewer)
        }
    }
}

/**
 * @param divisions 分割数
 * @param radius 半径
 */
private class RadialPointGenerator(
    divisions: Int,
    radius: Float
) {
    private val points: List<Pair<Float, Float>> = createCirclePoints(divisions, radius)
    private val currentIndexMap = WeakHashMap<Entity, Int>() // race-condition 就算发生也没什么大问题

    // 获取当前索引的点对
    fun next(viewer: Entity): Pair<Float, Float> {
        val currentIndex = currentIndexMap.getOrPut(viewer) { 0 }
        val pair = points[currentIndex]

        // 让索引每次步进到对立位置，确保下一次获取时是“对立点”
        currentIndexMap[viewer] = (currentIndex + points.size / 2) % points.size

        return pair
    }

    // 初始化生成一组在圆上均匀分布的点对
    private fun createCirclePoints(divisions: Int, radius: Float): List<Pair<Float, Float>> {
        val points = mutableListOf<Pair<Float, Float>>()
        val step = (2 * Math.PI / divisions).toFloat()

        for (i in 0 until divisions) {
            val angle = step * i
            val r1 = radius * cos(angle)
            val r2 = radius * sin(angle)
            points.add(Pair(r1, r2))
        }

        return points
    }
}
