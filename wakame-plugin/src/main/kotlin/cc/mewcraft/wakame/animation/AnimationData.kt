package cc.mewcraft.wakame.animation

import cc.mewcraft.lazyconfig.configurate.SimpleSerializer
import cc.mewcraft.lazyconfig.configurate.serializer.DispatchingSerializer
import cc.mewcraft.wakame.entity.display.*
import cc.mewcraft.wakame.util.math.Transformation
import cc.mewcraft.wakame.util.runTaskLater
import org.bukkit.Location
import org.bukkit.entity.Display.Billboard
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay.TextAlignment
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.PostProcess
import org.spongepowered.configurate.serialize.SerializationException
import kotlin.math.max

/**
 * 动画数据.
 * 包含有序播放的**单帧动画列表**和**设置项**.
 * 设置项相当于所有帧的共通属性.
 */
interface AnimationData<T : CommonDisplayData> {
    val frames: Map<Long, AnimationFrame<T>>
    val settings: AnimationSettings<T>

    /**
     * 播放动画.
     */
    fun play(viewer: Player, animLocation: Location, context: AnimationContext)

    companion object {
        fun serializer(): SimpleSerializer<AnimationData<*>> {
            return DispatchingSerializer.createPartial(
                mapOf(
                    "text_display" to TextDisplayAnimationData::class
                )
            )
        }
    }
}

/**
 * 文本展示实体动画数据.
 */
@ConfigSerializable
data class TextDisplayAnimationData(
    override val frames: Map<Long, TextDisplayAnimationFrame>,
    override val settings: TextDisplayAnimationSettings,
) : AnimationData<TextDisplayData> {

    override fun play(viewer: Player, animLocation: Location, context: AnimationContext) {
        val data = settings.applyTo(TextDisplayData(), context)
        val display = TextDisplay(data, animLocation)
        display.create()

        for ((delay, frame) in frames) {
            // 反序列化时保证 delay 必定大于等于 0
            if (delay == 0L) {
                frame.applyTo(data, context)
                display.update()
                display.show(viewer)
            } else {
                runTaskLater(delay) { ->
                    frame.applyTo(data, context)
                    display.update()
                    display.refresh(viewer)
                }
            }
        }

        // 反序列化时保证至少会有 delay = 0 这一帧
        val entry = frames.maxByOrNull { it.key }!!
        val lastFrame = entry.value
        val animDuration = entry.key + lastFrame.startInterpolation + max(lastFrame.interpolationDuration, lastFrame.teleportDuration)
        runTaskLater(animDuration) { ->
            display.hide(viewer)
        }
    }

    @PostProcess
    private fun callback() {
        if (frames.any { it.key < 0 }) throw SerializationException("The delay of a frame must be non-negative")
        if (!frames.keys.contains(0)) throw SerializationException("Missing start frame (delay = 0)")
    }

}


/**
 * 单帧动画数据.
 */
interface AnimationFrame<T : CommonDisplayData> {
    val startInterpolation: Int
    val interpolationDuration: Int
    val teleportDuration: Int
    val transformation: Transformation

    /**
     * 应用该帧.
     * 本质上是设置相关展示实体数据.
     */
    fun applyTo(data: T, context: AnimationContext): T

    /**
     * 方便函数.
     * 设置共通数据.
     */
    fun AnimationFrame<*>.applyToCommon(data: CommonDisplayData) {
        data.startInterpolation = this.startInterpolation
        data.interpolationDuration = this.interpolationDuration
        data.teleportDuration = this.teleportDuration
        data.transformation = this.transformation
    }
}

/**
 * 文本展示实体单帧动画数据.
 * 用于序列化.
 */
@ConfigSerializable
data class TextDisplayAnimationFrame(
    override val startInterpolation: Int = 0,
    override val interpolationDuration: Int = 0,
    override val teleportDuration: Int = 0,
    override val transformation: Transformation = Transformation.identity(),
    /**
     * 可以发现, 此处和 [TextDisplayAnimationSettings] 均有 [text] 属性.
     * 这样设计的目的是:
     * 1.如果动画中的文本每帧不变化, 只需要在 Settings 中全局设置一次即可, 各帧直接 null 节约性能.
     * 2.如果动画中的文本每帧变化, 则各帧各自设置.
     * 3.实现了简单的继承关系, 各帧可以继承 Settings 中的文本, 也可以自行覆写.
     */
    val text: TextBuilder? = null,
) : AnimationFrame<TextDisplayData> {
    override fun applyTo(data: TextDisplayData, context: AnimationContext): TextDisplayData {
        applyToCommon(data)
        if (text != null) {
            data.text = text.build(context)
        }
        return data
    }
}


/**
 * 动画设置项.
 */
interface AnimationSettings<T : CommonDisplayData> {
    val billboard: Billboard
    val brightness: Brightness
    val glowColorOverride: Int
    val height: Float
    val width: Float
    val shadowRadius: Float
    val shadowStrength: Float
    val viewRange: Float

    /**
     * 应用设置项.
     * 本质上是设置相关展示实体数据.
     */
    fun applyTo(data: T, context: AnimationContext): T

    /**
     * 方便函数.
     * 设置共通数据.
     */
    fun AnimationSettings<*>.applyToCommon(data: CommonDisplayData) {
        data.billboard = this.billboard
        data.brightness = this.brightness
        data.glowColorOverride = this.glowColorOverride
        data.height = this.height
        data.width = this.width
        data.shadowRadius = this.shadowRadius
        data.shadowStrength = this.shadowStrength
        data.viewRange = this.viewRange
    }
}

/**
 * 文本展示实体动画设置项.
 * 用于序列化.
 */
@ConfigSerializable
data class TextDisplayAnimationSettings(
    override val billboard: Billboard = Billboard.FIXED,
    override val brightness: Brightness = DefaultBrightness,
    override val glowColorOverride: Int = -1,
    override val height: Float = 0.0f,
    override val width: Float = 0.0f,
    override val shadowRadius: Float = 0.0f,
    override val shadowStrength: Float = 1.0f,
    override val viewRange: Float = 1.0f,
    val textAlignment: TextAlignment = TextAlignment.CENTER,
    val background: Int = 0x40000000,
    val defaultBackground: Boolean = false,
    val lineWidth: Int = 200,
    val isSeeThrough: Boolean = false,
    val hasTextShadow: Boolean = false,
    /**
     * 参见 [TextDisplayAnimationFrame.text].
     */
    val text: TextBuilder = FixedTextBuilder(),
    val textOpacity: Byte = -1,
) : AnimationSettings<TextDisplayData> {
    override fun applyTo(data: TextDisplayData, context: AnimationContext): TextDisplayData {
        this.applyToCommon(data)
        data.textAlignment = this.textAlignment
        data.background = this.background
        data.defaultBackground = this.defaultBackground
        data.lineWidth = this.lineWidth
        data.isSeeThrough = this.isSeeThrough
        data.hasTextShadow = this.hasTextShadow
        data.text = this.text.build(context)
        data.textOpacity = this.textOpacity
        return data
    }
}

/**
 * 动画上下文.
 */
interface AnimationContext {
    object Empty : AnimationContext
}
