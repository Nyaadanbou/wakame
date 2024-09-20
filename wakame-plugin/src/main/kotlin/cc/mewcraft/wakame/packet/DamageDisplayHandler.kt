package cc.mewcraft.wakame.packet

import cc.mewcraft.wakame.event.NekoEntityDamageEvent
import cc.mewcraft.wakame.extensions.*
import cc.mewcraft.wakame.hologram.Hologram
import cc.mewcraft.wakame.hologram.TextHologramData
import cc.mewcraft.wakame.util.runTaskLater
import net.kyori.adventure.extra.kotlin.text
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.Display
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.joml.Vector3f
import kotlin.random.Random

class DamageDisplayHandler : Listener {

    companion object {
        // 这些 Vector3f 实例都是可变的, 请注意副作用 !!!

        private val CRITICAL_SCALE = Vector3f(4f, 4f, 4f)
        private val NON_CRITICAL_SCALE = Vector3f(2f, 2f, 2f)

        private val BASE_I = Vector3f(1f, 0f, 0f)
        private val BASE_J = Vector3f(0f, 1f, 0f)
        private val BASE_K = Vector3f(0f, 0f, 1f)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private fun onWakameEntityDamage(event: NekoEntityDamageEvent) {
        val damageSource = event.damageSource
        val damager = damageSource.causingEntity as? Player ?: return
        val damagee = event.damagee
        val damageeLocation = damagee.location.add(0.0, damagee.height / 4, 0.0) // 获取受伤实体的中心位置
        val damageValueMap = event.getFinalDamageMap(excludeZeroDamage = true)
        val isCritical = event.damageMetadata.isCritical

        val hologramLoc = findCBetweenAB(a0 = damager.location, b0 = damageeLocation, t0 = 1.25, r0 = 0.5)
        val hologramScale = if (isCritical) CRITICAL_SCALE else NON_CRITICAL_SCALE
        val hologramText = damageValueMap
            .map { (element, value) ->
                val damageValue = "%.1f".format(value)
                val damageText = text {
                    content(damageValue)
                    element.styles.forEach { applicableApply(it) }
                }
                if (isCritical) {
                    text {
                        content("\ud83d\udca5 ")
                        color(TextColor.color(0xff9900))
                        append(damageText)
                    }
                } else {
                    damageText
                }
            }.let { components ->
                Component.join(JoinConfiguration.spaces(), components)
            }

        sendDamageHologram(damager, hologramLoc, hologramScale, hologramText)
    }

    /**
     * 计算坐标 `C`, 使得 `C` 落在点 `A` 和点 `B` 之间.
     *
     * 具体来说, 我们先找到一个平面 `P`, 使得平面垂直于 `AB` 向量.
     * 平面 `P` 更靠近点 `A` 还是更靠近点 `B` 取决于 [t0] 的取值.
     * 然后我们在平面 `P` 上找到点 `C`, 其随机程度由 [r0] 决定.
     *
     * [t0] 的取值应该在 `[0.0, 1.0]` 之间.
     * 如果 [t0] 接近于 `0.0`, 则 `C` 更靠近 `A`.
     * 如果 [t0] 接近于 `1.0`, 则 `C` 更靠近 `B`.
     *
     * @param a0 坐标
     * @param b0 坐标
     * @param t0 取值范围 `[0.0, 1.0]`
     * @param r0 取值范围不限
     */
    private fun findCBetweenAB(
        a0: Location,
        b0: Location,
        t0: Double,
        r0: Double,
    ): Location {
        val t = t0.toFloat()

        val a = a0.toVector3f()
        val b = b0.toVector3f()
        val ab = b.copy() - a

        // 生成不平行于 AB 的任意向量
        val vx = if (ab.x != 0f || ab.z != 0f) BASE_J.copy() else BASE_I.copy()

        // 生成平面 P 的基向量
        // v1=(1,0,0)×AB=(0,dz,−dy)
        // v2=AB×v1
        val v1 = (vx cross ab).normalize()
        val v2 = (ab cross v1).normalize()

        // 生成垂直平面的随机因子
        val r1 = Random.nextDouble(-r0, r0).toFloat()
        val r2 = Random.nextDouble(-r0, r0).toFloat()

        // 计算 C: C=A+t⋅(B−A)+r1⋅v1+r2⋅v2
        val c = a + ((b - a) mul t) + (v1 mul r1) + (v2 mul r2)

        return c.toLocation(a0.world)
    }

    /**
     * 发送伤害数值的悬浮文字.
     */
    private fun sendDamageHologram(
        hologramViewer: Player,
        hologramLocation: Location,
        initialScale: Vector3f,
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
            this.scale = initialScale
            this.brightness = Display.Brightness(15, 0)
        }
        val hologram = Hologram(hologramData)

        hologram.show(hologramViewer)

        runTaskLater(2) {
            hologramData.apply {
                this.startInterpolation = 0
                this.interpolationDuration = 5
                this.translation.add(0.0f, 0.5f, 0.0f)
                this.scale.set(6.0f, 6.0f, 6.0f)
            }
            hologram.setEntityData(hologramData)
            hologram.refresh(hologramViewer)
        }

        runTaskLater(8) {
            hologramData.apply {
                this.startInterpolation = 0
                this.interpolationDuration = 15
                this.translation.add(0.0f, 1.0f, 0.0f)
                this.scale.set(0.0f, 0.0f, 0.0f)
            }
            hologram.setEntityData(hologramData)
            hologram.refresh(hologramViewer)
        }

        runTaskLater(24) {
            hologram.hide(hologramViewer)
        }
    }
}
