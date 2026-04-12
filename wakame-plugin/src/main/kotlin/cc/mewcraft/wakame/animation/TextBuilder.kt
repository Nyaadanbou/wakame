package cc.mewcraft.wakame.animation

import cc.mewcraft.lazyconfig.configurate.SimpleSerializer
import cc.mewcraft.lazyconfig.configurate.serializer.DispatchingSerializer
import net.kyori.adventure.text.Component
import org.spongepowered.configurate.objectmapping.ConfigSerializable

/**
 * 文本展示实体单帧动画中的文本构建器.
 */
interface TextBuilder {
    fun build(context: AnimationContext): Component

    companion object {
        internal fun serializer(): SimpleSerializer<TextBuilder> {
            return DispatchingSerializer.createPartial(
                mapOf(
                    "fixed" to FixedTextBuilder::class,
                    "merged_damage_display" to MergedDamageHologramTextBuilder::class,
                    "separated_damage_display" to SeparatedDamageHologramTextBuilder::class,
                )
            )
        }
    }
}

/**
 * 固定文本构建器.
 */
@ConfigSerializable
data class FixedTextBuilder(
    val text: Component = Component.text("未配置"),
) : TextBuilder {

    override fun build(context: AnimationContext): Component {
        return text
    }
}