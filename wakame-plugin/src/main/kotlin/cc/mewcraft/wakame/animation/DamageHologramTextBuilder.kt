package cc.mewcraft.wakame.animation

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
import org.spongepowered.configurate.objectmapping.ConfigSerializable

data class DamageHologramContext(
    val event: PostprocessDamageEvent,
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
@ConfigSerializable
data class MergedDamageHologramTextBuilder(
    override val finalText: String = "未配置: [final_text]",
    override val criticalStrikeStylePositive: Array<StyleBuilderApplicable> = emptyArray(),
    override val criticalStrikeStyleNegative: Array<StyleBuilderApplicable> = emptyArray(),
    override val criticalStrikeStyleNone: Array<StyleBuilderApplicable> = emptyArray(),
    override val criticalStrikeTextPositive: Component = Component.text("未配置: [critical_strike_text.positive]"),
    override val criticalStrikeTextNegative: Component = Component.text("未配置: [critical_strike_text.negative]"),
    override val criticalStrikeTextNone: Component = Component.text("未配置: [critical_strike_text.none]"),
    val damageValueText: String = "未配置: [damage_value_text]",
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MergedDamageHologramTextBuilder

        if (finalText != other.finalText) return false
        if (!criticalStrikeStylePositive.contentEquals(other.criticalStrikeStylePositive)) return false
        if (!criticalStrikeStyleNegative.contentEquals(other.criticalStrikeStyleNegative)) return false
        if (!criticalStrikeStyleNone.contentEquals(other.criticalStrikeStyleNone)) return false
        if (criticalStrikeTextPositive != other.criticalStrikeTextPositive) return false
        if (criticalStrikeTextNegative != other.criticalStrikeTextNegative) return false
        if (criticalStrikeTextNone != other.criticalStrikeTextNone) return false
        if (damageValueText != other.damageValueText) return false

        return true
    }

    override fun hashCode(): Int {
        var result = finalText.hashCode()
        result = 31 * result + criticalStrikeStylePositive.contentHashCode()
        result = 31 * result + criticalStrikeStyleNegative.contentHashCode()
        result = 31 * result + criticalStrikeStyleNone.contentHashCode()
        result = 31 * result + criticalStrikeTextPositive.hashCode()
        result = 31 * result + criticalStrikeTextNegative.hashCode()
        result = 31 * result + criticalStrikeTextNone.hashCode()
        result = 31 * result + damageValueText.hashCode()
        return result
    }

}

/**
 * 一种伤害显示文本构建器的实现.
 * 该实现下各元素伤害分别显示.
 */
@ConfigSerializable
data class SeparatedDamageHologramTextBuilder(
    override val finalText: String = "未配置: [final_text]",
    override val criticalStrikeStylePositive: Array<StyleBuilderApplicable> = emptyArray(),
    override val criticalStrikeStyleNegative: Array<StyleBuilderApplicable> = emptyArray(),
    override val criticalStrikeStyleNone: Array<StyleBuilderApplicable> = emptyArray(),
    override val criticalStrikeTextPositive: Component = Component.text("未配置: [critical_strike_text.positive]"),
    override val criticalStrikeTextNegative: Component = Component.text("未配置: [critical_strike_text.negative]"),
    override val criticalStrikeTextNone: Component = Component.text("未配置: [critical_strike_text.none]"),
    val damageValueText: String = "未配置: [damage_value_text]",
    val separator: Component = Component.text("未配置: [separator]"),
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SeparatedDamageHologramTextBuilder

        if (finalText != other.finalText) return false
        if (!criticalStrikeStylePositive.contentEquals(other.criticalStrikeStylePositive)) return false
        if (!criticalStrikeStyleNegative.contentEquals(other.criticalStrikeStyleNegative)) return false
        if (!criticalStrikeStyleNone.contentEquals(other.criticalStrikeStyleNone)) return false
        if (criticalStrikeTextPositive != other.criticalStrikeTextPositive) return false
        if (criticalStrikeTextNegative != other.criticalStrikeTextNegative) return false
        if (criticalStrikeTextNone != other.criticalStrikeTextNone) return false
        if (damageValueText != other.damageValueText) return false
        if (separator != other.separator) return false

        return true
    }

    override fun hashCode(): Int {
        var result = finalText.hashCode()
        result = 31 * result + criticalStrikeStylePositive.contentHashCode()
        result = 31 * result + criticalStrikeStyleNegative.contentHashCode()
        result = 31 * result + criticalStrikeStyleNone.contentHashCode()
        result = 31 * result + criticalStrikeTextPositive.hashCode()
        result = 31 * result + criticalStrikeTextNegative.hashCode()
        result = 31 * result + criticalStrikeTextNone.hashCode()
        result = 31 * result + damageValueText.hashCode()
        result = 31 * result + separator.hashCode()
        return result
    }

}