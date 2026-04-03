package cc.mewcraft.wakame.animation

import cc.mewcraft.lazyconfig.configurate.SimpleSerializer
import cc.mewcraft.lazyconfig.configurate.require
import cc.mewcraft.wakame.entity.display.Brightness
import cc.mewcraft.wakame.entity.display.CommonDisplayData
import cc.mewcraft.wakame.entity.display.DefaultBrightness
import cc.mewcraft.wakame.entity.display.TextDisplay
import cc.mewcraft.wakame.entity.display.TextDisplayData
import cc.mewcraft.wakame.util.math.Transformation
import cc.mewcraft.wakame.util.runTaskLater
import org.bukkit.Location
import org.bukkit.entity.Display.Billboard
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay.TextAlignment
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import java.lang.reflect.Type
import kotlin.math.max

/**
 * 动画数据.
 * 包含有序播放的**单帧动画列表**和**设置项**.
 * 设置项相当于所有帧的共通属性.
 */
interface AnimationData<T : CommonDisplayData> {
    val frames: Map<Long, AnimationFrame<T>>
    val settings: AnimationSettings<T>

    fun play(viewer: Player, animLocation: Location, context: AnimationContext)

    companion object Serializer : SimpleSerializer<AnimationData<*>> {
        override fun deserialize(type: Type, node: ConfigurationNode): AnimationData<*> {
            val type = node.node("type").require<String>()

            val animationData = when (type) {
                TextDisplayAnimationData.TYPE -> node.require<TextDisplayAnimationData>()

                else -> throw IllegalArgumentException("Unknown animation type: '$type'")
            }

            require(animationData.frames.all { it.key >= 0 }) { "The delay of a frame must be non-negative" }
            require(animationData.frames.keys.contains(0)) { "Missing start frame (delay = 0)" }

            return animationData
        }
    }
}

/**
 * 文本展示实体动画数据.
 */
@ConfigSerializable
data class TextDisplayAnimationData(
    override val frames: Map<Long, TextDisplayAnimationFrame>,
    override val settings: TextDisplayAnimationSettings
) : AnimationData<TextDisplayData> {
    companion object {
        const val TYPE = "text_display"
    }

    override fun play(viewer: Player, animLocation: Location, context: AnimationContext) {
        val data = settings.applyTo(TextDisplayData(), context)
        val display = TextDisplay(data, animLocation)
        display.create()

        for ((delay, frame) in frames) {
            // 保险起见还是判断一下非负, 实际上反序列化时已保证
            if (delay < 0) continue

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

        val maxEntry = frames.maxByOrNull { it.key }
        // 实际上不可能为空
        // 反序列化时已保证至少会有 delay = 0 这一帧
        if (maxEntry != null) {
            val animDuration = maxEntry.key + max(maxEntry.value.interpolationDuration, maxEntry.value.teleportDuration)
            runTaskLater(animDuration) { ->
                display.hide(viewer)
            }
        }
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
     * 1.如果动画中的文本每帧不变化, 只需要在 Settings 中全局设置一次即可, 各帧直接 Empty 节约性能.
     * 2.如果动画中的文本每帧变化, 则各帧各自设置.
     * 3.实现了简单的继承关系, 各帧可以继承 Settings 中的文本, 也可以自行覆写.
     */
    val text: TextBuilder = EmptyTextBuilder
) : AnimationFrame<TextDisplayData> {
    override fun applyTo(data: TextDisplayData, context: AnimationContext): TextDisplayData {
        applyToCommon(data)
        if (text == EmptyTextBuilder) {
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
    val text: TextBuilder = EmptyTextBuilder,
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
