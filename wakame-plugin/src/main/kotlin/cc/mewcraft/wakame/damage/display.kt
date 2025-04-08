package cc.mewcraft.wakame.damage

import cc.mewcraft.wakame.MM
import cc.mewcraft.wakame.config.ConfigAccess
import cc.mewcraft.wakame.config.entry
import cc.mewcraft.wakame.config.node
import cc.mewcraft.wakame.config.registerSerializer
import cc.mewcraft.wakame.entity.hologram.AnimationData
import cc.mewcraft.wakame.entity.hologram.Hologram
import cc.mewcraft.wakame.entity.hologram.TextHologramData
import cc.mewcraft.wakame.event.bukkit.NekoPostprocessDamageEvent
import cc.mewcraft.wakame.extensions.*
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.registry2.BuiltInRegistries
import cc.mewcraft.wakame.serialization.configurate.TypeSerializer2
import cc.mewcraft.wakame.util.KOISH_NAMESPACE
import cc.mewcraft.wakame.util.registerEvents
import cc.mewcraft.wakame.util.require
import cc.mewcraft.wakame.util.runTaskLater
import net.kyori.adventure.extra.kotlin.join
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.format.StyleBuilderApplicable
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.joml.Vector3f
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get
import xyz.xenondevs.commons.provider.Provider
import java.lang.reflect.Type
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.random.Random

private val DAMAGE_CONFIG = ConfigAccess.INSTANCE["damage/config"]
private val DISPLAY_CONFIG = DAMAGE_CONFIG.node("display")
private val MERGED_DISPLAY_CONFIG = DISPLAY_CONFIG.node("merged")
private val SEPARATED_DISPLAY_CONFIG = DISPLAY_CONFIG.node("separated")

/**
 * 该 object 实现了元素伤害显示的功能.
 */
@Init(stage = InitStage.POST_WORLD)
internal object DamageDisplay : Listener {

    // 这些 Vector3f 实例都是可变的, 请注意副作用 !!!
    private val ZERO = Vector3f(0f, 0f, 0f)
    private val UNIT_X = Vector3f(1f, 0f, 0f)
    private val UNIT_Y = Vector3f(0f, 1f, 0f)
    private val UNIT_Z = Vector3f(0f, 0f, 1f)
    private val ONE = Vector3f(1f, 1f, 1f)

    // 用于辅助生成*伪随机*的伤害悬浮文字的坐标位置
    private val RADIAL_POINT_CYCLE = RadialPointCycle(8, 1f)

    // 用于构建 Display 的常量
    private val BACKGROUND = Color.fromARGB(0)
    private val BRIGHTNESS = Display.Brightness(15, 0)

    // 当前使用的伤害显示配置
    private val settings by DISPLAY_CONFIG.entry<DamageDisplaySettings>("mode")

    @InitFun
    fun init() {
        registerEvents()
    }

    @EventHandler
    fun on(event: NekoPostprocessDamageEvent) {
        val damagee = event.damagee as? LivingEntity ?: return
        val damageeLoc = damagee.location
        val css = event.getCriticalState()
        val hologramLoc = calculateHologramLocation1(damagee = damagee)
        val hologramText = settings.finalText(event)

        for (viewer in damagee.trackedBy) {
            showHologram(viewer, css, damageeLoc, hologramLoc, hologramText)
        }
    }

    private fun showHologram(
        viewer: Player,
        css: CriticalStrikeState,
        damageeLoc: Location,
        hologramLoc: Location,
        hologramText: Component,
    ) {
        val hologram = Hologram(
            data = TextHologramData(
                location = hologramLoc,
                text = hologramText,
                background = BACKGROUND,
                hasTextShadow = true,
                textAlignment = TextDisplay.TextAlignment.CENTER,
                isSeeThrough = viewer.location.distanceSquared(damageeLoc) < 512.0
            ).apply {
                this.brightness = BRIGHTNESS
            }
        )

        for (animation in settings.animations) {
            runTaskLater(animation.delay) {
                val hologramData = hologram.data<TextHologramData>()

                // 根据 animation 更新 hologram
                when (css) {
                    CriticalStrikeState.NONE -> animation.normalData
                    CriticalStrikeState.POSITIVE -> animation.positiveData
                    CriticalStrikeState.NEGATIVE -> animation.negativeData
                }.applyTo(hologramData)

                if (animation.delay == 0L) {
                    // delay = 0 表示需要发送 [创建] hologram 的封包
                    hologram.show(viewer)
                } else {
                    // 否则只需要更新
                    hologram.update()
                    hologram.refresh(viewer)
                }
            }
        }

        // 发送 [隐藏] hologram 的封包
        // 配置文件需要手动确保在“动画”播放完毕后再隐藏
        runTaskLater(settings.animationDuration) {
            hologram.hide(viewer)
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


// ------------
// 配置文件的实现
// ------------


internal interface DamageDisplaySettings : DamageDisplaySettingsFields {

    fun finalText(context: NekoPostprocessDamageEvent): Component {
        val criticalStrikeMetadata = context.damageMetadata.criticalStrikeMetadata
        val criticalStrikeStyle = criticalStrikeStyle(criticalStrikeMetadata)
        val criticalStrikeText = criticalStrikeText(criticalStrikeMetadata)

        val finalText = MM.deserialize(
            MergedDamageDisplaySettings.finalText,
            Placeholder.styling("critical_strike_style", *criticalStrikeStyle),
            Placeholder.component("critical_strike_text", criticalStrikeText),
            Placeholder.component("damage_value_text", damageValueText(context)),
        )

        return finalText
    }

    fun damageValueText(context: NekoPostprocessDamageEvent): Component

    fun criticalStrikeStyle(context: CriticalStrikeMetadata): Array<StyleBuilderApplicable> = when (context.state) {
        CriticalStrikeState.NONE -> criticalStrikeStyleNone
        CriticalStrikeState.POSITIVE -> criticalStrikeStylePositive
        CriticalStrikeState.NEGATIVE -> criticalStrikeStyleNegative
    }

    fun criticalStrikeText(context: CriticalStrikeMetadata): Component = when (context.state) {
        CriticalStrikeState.NONE -> criticalStrikeTextNone
        CriticalStrikeState.POSITIVE -> criticalStrikeTextPositive
        CriticalStrikeState.NEGATIVE -> criticalStrikeTextNegative
    }

}

internal interface DamageDisplaySettingsFields {

    val animations: List<DamageDisplayAnimation>
    val animationDuration: Long
    val finalText: String
    val criticalStrikeStylePositive: Array<StyleBuilderApplicable>
    val criticalStrikeStyleNegative: Array<StyleBuilderApplicable>
    val criticalStrikeStyleNone: Array<StyleBuilderApplicable>
    val criticalStrikeTextPositive: Component
    val criticalStrikeTextNegative: Component
    val criticalStrikeTextNone: Component

}

internal class DamageDisplaySettingsCommonFields(
    config: Provider<ConfigurationNode>,
) : DamageDisplaySettingsFields {
    override val animations: List<DamageDisplayAnimation> by config.entry("animations")
    override val animationDuration: Long by config.entry("animation_duration")
    override val finalText: String by config.entry("final_text")
    override val criticalStrikeStylePositive: Array<StyleBuilderApplicable> by config.entry("critical_strike_style", "positive")
    override val criticalStrikeStyleNegative: Array<StyleBuilderApplicable> by config.entry("critical_strike_style", "negative")
    override val criticalStrikeStyleNone: Array<StyleBuilderApplicable> by config.entry("critical_strike_style", "none")
    override val criticalStrikeTextPositive: Component by config.entry("critical_strike_text", "positive")
    override val criticalStrikeTextNegative: Component by config.entry("critical_strike_text", "negative")
    override val criticalStrikeTextNone: Component by config.entry("critical_strike_text", "none")
}

internal object MergedDamageDisplaySettings : DamageDisplaySettings, DamageDisplaySettingsFields by DamageDisplaySettingsCommonFields(MERGED_DISPLAY_CONFIG) {

    val damageValueText: String by MERGED_DISPLAY_CONFIG.entry("damage_value_text")

    override fun damageValueText(context: NekoPostprocessDamageEvent): Component {
        val damageMap = context.getFinalDamageMap()
        val elementType = damageMap.maxWithOrNull(
            compareBy { it.value }
        )?.key ?: BuiltInRegistries.ELEMENT.getDefaultEntry()
        val damageValueText = MM.deserialize(
            damageValueText,
            Placeholder.component("element_name", elementType.unwrap().displayName),
            Placeholder.styling("element_style", *elementType.unwrap().displayStyles),
            Formatter.number("damage_value", context.getFinalDamage())
        )
        return damageValueText
    }

}

internal object SeparatedDamageDisplaySettings : DamageDisplaySettings, DamageDisplaySettingsFields by DamageDisplaySettingsCommonFields(SEPARATED_DISPLAY_CONFIG) {

    val damageValueText: String by SEPARATED_DISPLAY_CONFIG.entry("damage_value_text")
    val separator: Component by SEPARATED_DISPLAY_CONFIG.entry("separator")

    override fun damageValueText(context: NekoPostprocessDamageEvent): Component {
        val damageMap = context.getFinalDamageMap()
        val damageValueText = damageMap.map { (elementType, damageValue) ->
            MM.deserialize(
                damageValueText,
                Placeholder.component("element_name", elementType.unwrap().displayName),
                Placeholder.styling("element_style", *elementType.unwrap().displayStyles),
                Formatter.number("damage_value", damageValue)
            )
        }.join(JoinConfiguration.separator(separator))
        return damageValueText
    }

}

/**
 * 伤害显示中文本展示实体的单次动画.
 */
internal data class DamageDisplayAnimation(
    val delay: Long,
    val normalData: AnimationData,
    val positiveData: AnimationData,
    val negativeData: AnimationData,
)

@Init(stage = InitStage.PRE_CONFIG)
internal object DamageDisplayAnimationSerializer : TypeSerializer2<DamageDisplayAnimation> {
    override fun deserialize(type: Type, node: ConfigurationNode): DamageDisplayAnimation {
        val delay = node.node("delay").require<Long>()

        val normalData = buildData(AnimationData.DEFAULT, node.node("normal"))
        val positiveData = buildData(normalData, node.node("positive_critical_strike"))
        val negativeData = buildData(normalData, node.node("negative_critical_strike"))

        return DamageDisplayAnimation(delay, normalData, positiveData, negativeData)
    }

    /**
     * 方便函数.
     */
    private fun buildData(parentData: AnimationData, node: ConfigurationNode): AnimationData {
        val startInterpolation = node.node("start_interpolation").get<Int>()
        val interpolationDuration = node.node("interpolation_duration").get<Int>()
        val translation = node.node("translation").get<Vector3f>()
        val scale = node.node("scale").get<Vector3f>()
        return AnimationData(parentData, startInterpolation, interpolationDuration, translation, scale)
    }

    @InitFun
    fun init() {
        ConfigAccess.INSTANCE.registerSerializer(KOISH_NAMESPACE, this)
    }
}

internal enum class DamageDisplayMode {
    MERGED, SEPARATED
}

@Init(stage = InitStage.PRE_CONFIG)
internal object DamageDisplaySettingsSerializer : TypeSerializer2<DamageDisplaySettings> {

    override fun deserialize(type: Type, node: ConfigurationNode): DamageDisplaySettings = when (node.require<DamageDisplayMode>()) {
        DamageDisplayMode.MERGED -> MergedDamageDisplaySettings
        DamageDisplayMode.SEPARATED -> SeparatedDamageDisplaySettings
    }

    @InitFun
    fun init() {
        ConfigAccess.INSTANCE.registerSerializer(KOISH_NAMESPACE, this)
    }

}
