package cc.mewcraft.wakame.animation

import cc.mewcraft.lazyconfig.configurate.SimpleSerializer
import cc.mewcraft.lazyconfig.configurate.require
import cc.mewcraft.wakame.util.typeTokenOf
import net.kyori.adventure.text.Component
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get
import java.lang.reflect.Type

/**
 * 文本展示实体单帧动画中的文本构建器.
 */
interface TextBuilder {
    fun build(context: AnimationContext): Component

    companion object Serializer : SimpleSerializer<TextBuilder> {
        override fun deserialize(type: Type, node: ConfigurationNode): TextBuilder {
            val type = node.node("type").require<String>()

            return when (type) {
                EmptyTextBuilder.TYPE -> EmptyTextBuilder
                FixedTextBuilder.TYPE -> FixedTextBuilder.deserialize(typeTokenOf<FixedTextBuilder>().type, node)
                MergedDamageHologramTextBuilder.TYPE -> MergedDamageHologramTextBuilder.deserialize(typeTokenOf<MergedDamageHologramTextBuilder>().type, node)
                SeparatedDamageHologramTextBuilder.TYPE -> SeparatedDamageHologramTextBuilder.deserialize(typeTokenOf<SeparatedDamageHologramTextBuilder>().type, node)

                else -> throw IllegalArgumentException("Unknown text builder type: '$type'")
            }
        }
    }
}

/**
 * 空文本构建器.
 */
object EmptyTextBuilder : TextBuilder {
    override fun build(context: AnimationContext): Component {
        return Component.empty()
    }

    const val TYPE = "empty"
}

/**
 * 固定文本构建器.
 */
class FixedTextBuilder(
    val text: Component,
) : TextBuilder {

    override fun build(context: AnimationContext): Component {
        return text
    }

    companion object Serializer : SimpleSerializer<FixedTextBuilder> {
        const val TYPE = "fixed"

        override fun deserialize(type: Type, node: ConfigurationNode): FixedTextBuilder {
            val component = node.node("text").get<Component>(Component.text("未配置文本"))

            return FixedTextBuilder(component)
        }
    }
}