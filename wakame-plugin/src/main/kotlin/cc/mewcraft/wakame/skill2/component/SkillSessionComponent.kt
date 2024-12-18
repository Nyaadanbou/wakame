package cc.mewcraft.wakame.skill2.component

import cc.mewcraft.wakame.skill2.condition.SkillConditionSession
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class SkillSessionComponent(
    var session: SkillConditionSession
) : Component<SkillSessionComponent> {
    override fun type(): ComponentType<SkillSessionComponent> = SkillSessionComponent

    companion object : ComponentType<SkillSessionComponent>()
}