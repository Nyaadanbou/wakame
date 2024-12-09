package cc.mewcraft.wakame.skill2.component

import cc.mewcraft.wakame.skill2.condition.SkillConditionSession
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class MechanicSessionComponent(
    var session: SkillConditionSession
) : Component<MechanicSessionComponent> {
    override fun type(): ComponentType<MechanicSessionComponent> = MechanicSessionComponent

    companion object : ComponentType<MechanicSessionComponent>()
}