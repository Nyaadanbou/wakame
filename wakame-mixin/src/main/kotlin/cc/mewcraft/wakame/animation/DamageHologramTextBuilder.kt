package cc.mewcraft.wakame.animation

import cc.mewcraft.lazyconfig.configurate.SimpleSerializer
import cc.mewcraft.wakame.damage.CriticalStrikeMetadata
import cc.mewcraft.wakame.damage.CriticalStrikeState
import cc.mewcraft.wakame.event.bukkit.PostprocessDamageEvent
import cc.mewcraft.wakame.registry.BuiltInRegistries
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.format.StyleBuilderApplicable
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get
import java.lang.reflect.Type

data class DamageHologramContext(
    val event: PostprocessDamageEvent
) : AnimationContext

/**
 * 伤害显示文本构建器.
 * 用于伤害显示.
 */
abstract class DamageHologramTextBuilder : TextBuilder {
    // 需要通过反序列化得到的通用配置项
    abstract val finalText: String
    abstract val criticalStrikeStylePositive: Array<StyleBuilderApplicable>
    abstract val criticalStrikeStyleNegative: Array<StyleBuilderApplicable>
    abstract val criticalStrikeStyleNone: Array<StyleBuilderApplicable>
    abstract val criticalStrikeTextPositive: Component
    abstract val criticalStrikeTextNegative: Component
    abstract val criticalStrikeTextNone: Component

    override fun build(context: AnimationContext): Component {
        if (context !is DamageHologramContext) return Component.text("Unexpected context type")
        return finalText(context.event)
    }

    fun finalText(event: PostprocessDamageEvent): Component {
        val criticalStrikeMetadata = event.damageMetadata.criticalStrikeMetadata
        val criticalStrikeStyle = criticalStrikeStyle(criticalStrikeMetadata)
        val criticalStrikeText = criticalStrikeText(criticalStrikeMetadata)

        val finalText = MiniMessage.miniMessage().deserialize(
            finalText,
            Placeholder.styling("critical_strike_style", *criticalStrikeStyle),
            Placeholder.component("critical_strike_text", criticalStrikeText),
            Placeholder.component("damage_value_text", damageValueText(event)),
        )

        return finalText
    }

    abstract fun damageValueText(event: PostprocessDamageEvent): Component

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

/**
 * 一种伤害显示文本构建器的实现.
 * 该实现下各元素伤害合并显示.
 */
class MergedDamageHologramTextBuilder(
    override val finalText: String,
    override val criticalStrikeStylePositive: Array<StyleBuilderApplicable>,
    override val criticalStrikeStyleNegative: Array<StyleBuilderApplicable>,
    override val criticalStrikeStyleNone: Array<StyleBuilderApplicable>,
    override val criticalStrikeTextPositive: Component,
    override val criticalStrikeTextNegative: Component,
    override val criticalStrikeTextNone: Component,
    val damageValueText: String
) : DamageHologramTextBuilder() {

    override fun damageValueText(event: PostprocessDamageEvent): Component {
        val damageMap = event.finalDamageMap
        val elementType = damageMap.maxWithOrNull(
            compareBy { it.value }
        )?.key ?: BuiltInRegistries.ELEMENT.getDefaultEntry()
        val damageValueText = MiniMessage.miniMessage().deserialize(
            damageValueText,
            Placeholder.component("element_name", elementType.unwrap().displayName),
            Placeholder.styling("element_style", *elementType.unwrap().displayStyles),
            Formatter.number("damage_value", event.finalDamage)
        )
        return damageValueText
    }

    companion object Serializer : SimpleSerializer<MergedDamageHologramTextBuilder> {
        const val TYPE = "merged_damage_display"

        override fun deserialize(type: Type, node: ConfigurationNode): MergedDamageHologramTextBuilder {
            val finalText = node.node("final_text").get<String>("请输入文本: [final_text]")
            val criticalStrikeStylePositive = node.node("critical_strike_style", "positive").get<Array<StyleBuilderApplicable>>(emptyArray())
            val criticalStrikeStyleNegative = node.node("critical_strike_style", "negative").get<Array<StyleBuilderApplicable>>(emptyArray())
            val criticalStrikeStyleNone = node.node("critical_strike_style", "none").get<Array<StyleBuilderApplicable>>(emptyArray())
            val criticalStrikeTextPositive = node.node("critical_strike_text", "positive").get<Component>(Component.text("请输入文本: [critical_strike_text.positive]"))
            val criticalStrikeTextNegative = node.node("critical_strike_text", "negative").get<Component>(Component.text("请输入文本: [critical_strike_text.negative]"))
            val criticalStrikeTextNone = node.node("critical_strike_text", "none").get<Component>(Component.text("请输入文本: [critical_strike_text.none]"))
            val damageValueText = node.node("damage_value_text").get<String>("请输入文本: [damage_value_text]")

            return MergedDamageHologramTextBuilder(
                finalText,
                criticalStrikeStylePositive,
                criticalStrikeStyleNegative,
                criticalStrikeStyleNone,
                criticalStrikeTextPositive,
                criticalStrikeTextNegative,
                criticalStrikeTextNone,
                damageValueText
            )
        }
    }

}

/**
 * 一种伤害显示文本构建器的实现.
 * 该实现下各元素伤害分别显示.
 */
class SeparatedDamageHologramTextBuilder(
    override val finalText: String,
    override val criticalStrikeStylePositive: Array<StyleBuilderApplicable>,
    override val criticalStrikeStyleNegative: Array<StyleBuilderApplicable>,
    override val criticalStrikeStyleNone: Array<StyleBuilderApplicable>,
    override val criticalStrikeTextPositive: Component,
    override val criticalStrikeTextNegative: Component,
    override val criticalStrikeTextNone: Component,
    val damageValueText: String,
    val separator: Component
) : DamageHologramTextBuilder() {

    override fun damageValueText(event: PostprocessDamageEvent): Component {
        val damageMap = event.finalDamageMap
        val damageValueTexts = damageMap.map { (elementType, damageValue) ->
            MiniMessage.miniMessage().deserialize(
                damageValueText,
                Placeholder.component("element_name", elementType.unwrap().displayName),
                Placeholder.styling("element_style", *elementType.unwrap().displayStyles),
                Formatter.number("damage_value", damageValue)
            )
        }
        return Component.join(JoinConfiguration.separator(separator), damageValueTexts)
    }

    companion object Serializer : SimpleSerializer<SeparatedDamageHologramTextBuilder> {
        const val TYPE = "separated_damage_display"

        override fun deserialize(type: Type, node: ConfigurationNode): SeparatedDamageHologramTextBuilder {
            val finalText = node.node("final_text").get<String>("请输入文本: [final_text]")
            val criticalStrikeStylePositive = node.node("critical_strike_style", "positive").get<Array<StyleBuilderApplicable>>(emptyArray())
            val criticalStrikeStyleNegative = node.node("critical_strike_style", "negative").get<Array<StyleBuilderApplicable>>(emptyArray())
            val criticalStrikeStyleNone = node.node("critical_strike_style", "none").get<Array<StyleBuilderApplicable>>(emptyArray())
            val criticalStrikeTextPositive = node.node("critical_strike_text", "positive").get<Component>(Component.text("请输入文本: [critical_strike_text.positive]"))
            val criticalStrikeTextNegative = node.node("critical_strike_text", "negative").get<Component>(Component.text("请输入文本: [critical_strike_text.negative]"))
            val criticalStrikeTextNone = node.node("critical_strike_text", "none").get<Component>(Component.text("请输入文本: [critical_strike_text.none]"))
            val damageValueText = node.node("damage_value_text").get<String>("请输入文本: [damage_value_text]")
            val separator = node.node("separator").get<Component>(Component.text("请输入文本: [separator]"))

            return SeparatedDamageHologramTextBuilder(
                finalText,
                criticalStrikeStylePositive,
                criticalStrikeStyleNegative,
                criticalStrikeStyleNone,
                criticalStrikeTextPositive,
                criticalStrikeTextNegative,
                criticalStrikeTextNone,
                damageValueText,
                separator
            )
        }
    }

}