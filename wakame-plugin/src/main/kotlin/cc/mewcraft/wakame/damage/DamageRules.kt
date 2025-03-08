package cc.mewcraft.wakame.damage

import cc.mewcraft.wakame.config.Configs
import cc.mewcraft.wakame.config.entry
import cc.mewcraft.wakame.config.node
import cc.mewcraft.wakame.config.optionalEntry
import cc.mewcraft.wakame.util.bindInstance
import team.unnamed.mocha.MochaEngine
import team.unnamed.mocha.runtime.binding.Binding
import xyz.xenondevs.commons.provider.orElse


/**
 * 伤害系统中可通过配置修改的规则.
 */
object DamageRules {
    private val DAMAGE_CONFIG = Configs["damage/config"]

    val ATTACK_DAMAGE_RATE_MULTIPLY_BEFORE_DEFENSE: Boolean by DAMAGE_CONFIG.entry<Boolean>("attack_damage_rate_multiply_before_defense")
    val CRITICAL_STRIKE_POWER_MULTIPLY_BEFORE_DEFENSE: Boolean by DAMAGE_CONFIG.entry<Boolean>("critical_strike_power_multiply_before_defense")
    val ROUNDING_DAMAGE: Boolean by DAMAGE_CONFIG.entry<Boolean>("rounding_damage")

    val LEAST_DAMAGE: Double by DAMAGE_CONFIG.entry<Double>("least_damage")

    private val VALID_DEFENSE_FORMULA: String by DAMAGE_CONFIG.entry<String>("valid_defense_formula")
    private val DAMAGE_AFTER_DEFENSE_FORMULA: String by DAMAGE_CONFIG.entry<String>("damage_after_defense_formula")
    private val BOW_FORCE_FORMULA: String by DAMAGE_CONFIG.entry<String>("bow_force_formula")

    @Binding("query")
    internal class BindingValidDefense(
        @JvmField @Binding("defense")
        val defense: Double,
        @JvmField @Binding("defense_penetration")
        val defensePenetration: Double,
        @JvmField @Binding("defense_penetration_rate")
        val defensePenetrationRate: Double
    )

    @Binding("query")
    internal class BindingDamageAfterDefense(
        @JvmField @Binding("original_damage")
        val originalDamage: Double,
        @JvmField @Binding("valid_defense")
        val validDefense: Double,
    )

    @Binding("query")
    internal class BindingBowForce(
        @JvmField @Binding("use_ticks")
        val useTicks: Int,
    )

    /**
     * 计算*单种元素*的有效防御.
     *
     * 影响因素:
     * - 受伤者防御属性值
     * - 伤害元数据防御穿透值
     * - 伤害元数据防御穿透率值
     */
    fun calculateValidDefense(
        defense: Double, defensePenetration: Double, defensePenetrationRate: Double,
    ): Double {
        val mocha = MochaEngine.createStandard()
        mocha.bindInstance(BindingValidDefense(defense, defensePenetration, defensePenetrationRate), "query")
        return mocha.eval(VALID_DEFENSE_FORMULA)
    }

    /**
     * 计算*单种元素*被防御后的伤害.
     *
     * 影响因素:
     * - 原始伤害值
     * - 有效防御值(本元素+通用元素)
     */
    fun calculateDamageAfterDefense(
        originalDamage: Double, validDefense: Double
    ): Double {
        val mocha = MochaEngine.createStandard()
        mocha.bindInstance(BindingDamageAfterDefense(originalDamage, validDefense), "query")
        return mocha.eval(DAMAGE_AFTER_DEFENSE_FORMULA)
    }

    /**
     * 计算弓的力度.
     *
     * 影响因素:
     * - 拉弓时间, 单位 tick
     */
    fun calculateBowForce(useTicks: Int): Double {
        val mocha = MochaEngine.createStandard()
        mocha.bindInstance(BindingBowForce(useTicks), "query")
        return mocha.eval(BOW_FORCE_FORMULA)
    }
}

object DamageDisplayConfig {
    private val DAMAGE_DISPLAY_NODE = Configs["damage/config"].node("damage_display")

    val TEXT: String by DAMAGE_DISPLAY_NODE.entry<String>("text")
    val FORMAT: String by DAMAGE_DISPLAY_NODE.entry<String>("format")
    val CRITICAL_STRIKE_STYLE_POSITIVE: String by DAMAGE_DISPLAY_NODE.entry<String>("critical_strike_style", "positive")
    val CRITICAL_STRIKE_STYLE_NEGATIVE: String by DAMAGE_DISPLAY_NODE.entry<String>("critical_strike_style", "negative")
    val CRITICAL_STRIKE_STYLE_NONE: String by DAMAGE_DISPLAY_NODE.entry<String>("critical_strike_style", "none")
    val CRITICAL_STRIKE_TEXT_POSITIVE: String by DAMAGE_DISPLAY_NODE.entry<String>("critical_strike_text", "positive")
    val CRITICAL_STRIKE_TEXT_NEGATIVE: String by DAMAGE_DISPLAY_NODE.entry<String>("critical_strike_text", "negative")
    val CRITICAL_STRIKE_TEXT_NONE: String by DAMAGE_DISPLAY_NODE.entry<String>("critical_strike_text", "none")
    val DETAIL: Boolean by DAMAGE_DISPLAY_NODE.entry<Boolean>("detail")
    val ELEMENT_TEXT: String by DAMAGE_DISPLAY_NODE.entry<String>("element_text")
    val SEPARATOR: String by DAMAGE_DISPLAY_NODE.optionalEntry<String>("separator").orElse(" ")
}