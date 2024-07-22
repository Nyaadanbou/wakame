package cc.mewcraft.wakame.reforge.modding.match

import cc.mewcraft.wakame.item.components.cells.Core
import cc.mewcraft.wakame.item.components.cells.cores.skill.CoreSkill
import cc.mewcraft.wakame.skill.trigger.Trigger
import cc.mewcraft.wakame.skill.trigger.TriggerVariant
import java.util.regex.Pattern

/**
 * 用于测试技能核心.
 */
class CoreMatchRuleSkill(
    override val path: Pattern,
    val trigger: Trigger,
    val variant: TriggerVariant,
) : CoreMatchRule {
    override val priority: Int = 2

    override fun test(core: Core): Boolean {
        if (core !is CoreSkill) {
            return false
        }

        val matcher = path.matcher(core.key.value())
        if (!matcher.matches()) {
            return false
        }

        return trigger == core.trigger && variant == core.variant
    }
}