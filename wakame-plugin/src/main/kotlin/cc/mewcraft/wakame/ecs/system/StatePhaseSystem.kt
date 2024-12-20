package cc.mewcraft.wakame.ecs.system

import cc.mewcraft.wakame.ecs.component.CastBy
import cc.mewcraft.wakame.ecs.component.IdentifierComponent
import cc.mewcraft.wakame.ecs.component.StatePhaseComponent
import cc.mewcraft.wakame.ecs.component.Tags
import cc.mewcraft.wakame.ecs.data.StatePhase
import cc.mewcraft.wakame.event.PlayerSkillStateChangeEvent
import cc.mewcraft.wakame.registry.SkillRegistry
import cc.mewcraft.wakame.skill2.Skill
import cc.mewcraft.wakame.util.Key
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import me.lucko.helper.text3.mini
import net.kyori.adventure.text.event.HoverEvent
import org.bukkit.entity.Player

class StatePhaseSystem : IteratingSystem(
    family = family { all(IdentifierComponent, CastBy, StatePhaseComponent, Tags.CAN_NEXT_STATE) }
) {
    override fun onTickEntity(entity: Entity) {
        val oldPhase = entity[StatePhaseComponent].phase
        val newPhase = oldPhase.next()
        val player = entity[CastBy].entity as? Player ?: return
        val id = entity[IdentifierComponent].id

        entity[StatePhaseComponent].phase = newPhase

        onStateChange(id, player, SkillRegistry.INSTANCES[Key(id)], oldPhase, newPhase)
        entity.configure { it -= Tags.CAN_NEXT_STATE }
    }

    private fun onStateChange(identifier: String, player: Player, skill: Skill, old: StatePhase, new: StatePhase) {
        PlayerSkillStateChangeEvent(player, skill, old, new).callEvent()
        player.sendMessage("技能 $identifier 状态已切换为 $new".mini.hoverEvent(HoverEvent.showText("技能状态变更: $old -> $new".mini)))
    }
}