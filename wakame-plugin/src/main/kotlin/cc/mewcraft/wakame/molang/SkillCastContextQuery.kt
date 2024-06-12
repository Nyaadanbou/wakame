package cc.mewcraft.wakame.molang

import cc.mewcraft.wakame.skill.context.SkillCastContext
import cc.mewcraft.wakame.skill.context.SkillCastContextKeys
import team.unnamed.mocha.runtime.binding.Binding

class SkillCastContextQuery(
    private val context: SkillCastContext
) {
    @Binding("is_player")
    fun isPlayer(): Boolean {
        return context.has(SkillCastContextKeys.CASTER_PLAYER)
    }
}