package cc.mewcraft.wakame.animation

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
    abstract val damageValueText: String
    abstract val isDamageValueMerged: Boolean
    abstract val separator: Component

    override fun build(context: AnimationContext): Component {
        if (context !is DamageHologramContext) return Component.text("Unexpected context type")
        return finalText(context.event)
    }

    abstract fun finalText(event: PostprocessDamageEvent): Component

    fun damageValueText(event: PostprocessDamageEvent): Component {
        val damageMap = event.finalDamageMap
        val damageValueText = if (isDamageValueMerged) {
            val elementType = damageMap.maxWithOrNull(
                compareBy { it.value }
            )?.key ?: BuiltInRegistries.ELEMENT.getDefaultEntry()
            MiniMessage.miniMessage().deserialize(
                damageValueText,
                Placeholder.component("element_name", elementType.unwrap().displayName),
                Placeholder.styling("element_style", *elementType.unwrap().displayStyles),
                Formatter.number("damage_value", event.finalDamage)
            )
        } else {
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
        return damageValueText
    }

}

/**
 * 普通攻击伤害显示文本构建器.
 */
@ConfigSerializable
data class NormalAttackDamageHologramTextBuilder(
    override val finalText: String = "未配置: [final_text]",
    override val damageValueText: String = "未配置: [damage_value_text]",
    override val isDamageValueMerged: Boolean = false,
    override val separator: Component = Component.text("未配置: [separator]"),
) : DamageHologramTextBuilder() {

    override fun finalText(event: PostprocessDamageEvent): Component {
        return MiniMessage.miniMessage().deserialize(
            finalText,
            Placeholder.component("damage_value_text", damageValueText(event)),
        )
    }

}

/**
 * 暴击伤害显示文本构建器.
 */
@ConfigSerializable
data class CriticalStrikeDamageHologramTextBuilder(
    override val finalText: String = "未配置: [final_text]",
    override val damageValueText: String = "未配置: [damage_value_text]",
    override val isDamageValueMerged: Boolean = false,
    override val separator: Component = Component.text("未配置: [separator]"),
    val criticalStrikeStyle: Array<StyleBuilderApplicable> = emptyArray(),
    val criticalStrikeText: Component = Component.text("未配置: [critical_strike_text.positive]"),
) : DamageHologramTextBuilder() {
    override fun finalText(event: PostprocessDamageEvent): Component {
        return MiniMessage.miniMessage().deserialize(
            finalText,
            Placeholder.styling("critical_strike_style", *criticalStrikeStyle),
            Placeholder.component("critical_strike_text", criticalStrikeText),
            Placeholder.component("damage_value_text", damageValueText(event)),
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CriticalStrikeDamageHologramTextBuilder

        if (isDamageValueMerged != other.isDamageValueMerged) return false
        if (finalText != other.finalText) return false
        if (damageValueText != other.damageValueText) return false
        if (separator != other.separator) return false
        if (!criticalStrikeStyle.contentEquals(other.criticalStrikeStyle)) return false
        if (criticalStrikeText != other.criticalStrikeText) return false

        return true
    }

    override fun hashCode(): Int {
        var result = isDamageValueMerged.hashCode()
        result = 31 * result + finalText.hashCode()
        result = 31 * result + damageValueText.hashCode()
        result = 31 * result + separator.hashCode()
        result = 31 * result + criticalStrikeStyle.contentHashCode()
        result = 31 * result + criticalStrikeText.hashCode()
        return result
    }

}