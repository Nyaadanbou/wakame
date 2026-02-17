package cc.mewcraft.wakame.hook.impl.betonquest.quest.objective

import cc.mewcraft.wakame.hook.impl.betonquest.util.request
import net.kyori.adventure.key.Key
import org.betonquest.betonquest.api.DefaultObjective
import org.betonquest.betonquest.api.instruction.Argument
import org.betonquest.betonquest.api.instruction.Instruction
import org.betonquest.betonquest.api.quest.objective.Objective
import org.betonquest.betonquest.api.quest.objective.ObjectiveFactory
import org.betonquest.betonquest.api.quest.objective.service.ObjectiveService
import org.bukkit.event.player.PlayerChangedWorldEvent
import kotlin.jvm.optionals.getOrNull

/**
 * @property entry 进入目标世界的标识符
 * @property exit 离开目标世界的标识符
 */
class ChangeWorldObjective(
    service: ObjectiveService,
    private val entry: Argument<Key>?,
    private val exit: Argument<Key>?,
) : DefaultObjective(service) {

    fun registerChangeWorldEvents(service: ObjectiveService) {
        service
            .request<PlayerChangedWorldEvent>()
            .onlineHandler { event, profile ->
                val to = event.player.world.key
                val exit2 = entry?.getValue(profile)
                if (exit2 == to) {
                    service.complete(profile)
                }
                val from = event.from.key
                val entry2 = exit?.getValue(profile)
                if (entry2 == from) {
                    service.complete(profile)
                }
            }
            .player { event -> event.player }
            .subscribe(true)
    }
}

class ChangeWorldObjectiveFactory : ObjectiveFactory {

    override fun parseInstruction(instruction: Instruction, service: ObjectiveService): Objective {
        val entry = instruction.parse { Key.key(it) }.get("entry").getOrNull()
        val exit = instruction.parse { Key.key(it) }.get("exit").getOrNull()
        val objective = ChangeWorldObjective(service, entry, exit)
        objective.registerChangeWorldEvents(service)
        return objective
    }
}