package cc.mewcraft.wakame.molang

import cc.mewcraft.wakame.skill.Caster
import cc.mewcraft.wakame.skill.condition.SkillCastContext
import team.unnamed.mocha.runtime.binding.Binding

class SkillCastContextQuery(
    private val context: SkillCastContext
) {
    @Binding("is_player")
    fun isPlayer(): Boolean {
        return context.caster is Caster.Player
    }
}