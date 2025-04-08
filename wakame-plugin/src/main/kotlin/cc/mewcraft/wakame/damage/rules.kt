package cc.mewcraft.wakame.damage

import cc.mewcraft.wakame.config.ConfigAccess
import cc.mewcraft.wakame.config.entry
import cc.mewcraft.wakame.config.node
import cc.mewcraft.wakame.util.bindInstance
import team.unnamed.mocha.MochaEngine
import team.unnamed.mocha.runtime.binding.Binding

private val DAMAGE_CONFIG = ConfigAccess.INSTANCE["damage/config"]
private val RULES_CONFIG = DAMAGE_CONFIG.node("rules")

/**
 * 伤害系统中可通过配置修改的规则.
 */
internal object DamageRules {

    val ATTACK_DAMAGE_RATE_MULTIPLY_BEFORE_DEFENSE: Boolean by RULES_CONFIG.entry("attack_damage_rate_multiply_before_defense")
    val CRITICAL_STRIKE_POWER_MULTIPLY_BEFORE_DEFENSE: Boolean by RULES_CONFIG.entry("critical_strike_power_multiply_before_defense")
    val ROUNDING_DAMAGE: Boolean by RULES_CONFIG.entry("rounding_damage")

    val LEAST_DAMAGE: Double by RULES_CONFIG.entry("least_damage")

    private val VALID_DEFENSE_FORMULA: String by RULES_CONFIG.entry("valid_defense_formula")
    private val DAMAGE_AFTER_DEFENSE_FORMULA: String by RULES_CONFIG.entry("damage_after_defense_formula")
    private val BOW_FORCE_FORMULA: String by RULES_CONFIG.entry("bow_force_formula")

    @Binding("q")
    internal class BindingValidDefense(
        @JvmField @Binding("defense")
        val defense: Double,
        @JvmField @Binding("defense_penetration")
        val defensePenetration: Double,
        @JvmField @Binding("defense_penetration_rate")
        val defensePenetrationRate: Double
    )

    @Binding("q")
    internal class BindingDamageAfterDefense(
        @JvmField @Binding("original_damage")
        val originalDamage: Double,
        @JvmField @Binding("valid_defense")
        val validDefense: Double,
    )

    @Binding("q")
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
        mocha.bindInstance(BindingValidDefense(defense, defensePenetration, defensePenetrationRate), "q")
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
        mocha.bindInstance(BindingDamageAfterDefense(originalDamage, validDefense), "q")
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
        mocha.bindInstance(BindingBowForce(useTicks), "q")
        return mocha.eval(BOW_FORCE_FORMULA)
    }

}
