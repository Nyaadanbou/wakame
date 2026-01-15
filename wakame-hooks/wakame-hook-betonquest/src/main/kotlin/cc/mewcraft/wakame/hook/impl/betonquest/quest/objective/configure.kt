package cc.mewcraft.wakame.hook.impl.betonquest.quest.objective

import io.papermc.paper.event.connection.configuration.AsyncPlayerConnectionConfigureEvent
import org.betonquest.betonquest.api.DefaultObjective
import org.betonquest.betonquest.api.QuestException
import org.betonquest.betonquest.api.instruction.Instruction
import org.betonquest.betonquest.api.profile.Profile
import org.betonquest.betonquest.api.quest.objective.Objective
import org.betonquest.betonquest.api.quest.objective.ObjectiveFactory
import org.betonquest.betonquest.api.quest.objective.service.ObjectiveService
import org.bukkit.event.EventPriority


/**
 * Requires the player to join the server.
 */
class ConfigureObjective : DefaultObjective {

    /**
     * Constructor for the LoginObjective.
     *
     * @param service the objective service
     * @throws QuestException if there is an error in the instruction
     */
    @Throws(QuestException::class)
    constructor(service: ObjectiveService) : super(service)

    /**
     * Check if the player has joined the server.
     *
     * @param event the event that triggers when the player joins
     * @param profile the profile of the player that joined
     */
    fun onConfigure(event: AsyncPlayerConnectionConfigureEvent, profile: Profile) {
        service.complete(profile)
    }
}


/**
 * Factory for creating [ConfigureObjective] instances from [Instruction]s.
 */
// FIXME 事件触发时, 获取不到玩家的 objectiveData. 也许是 BQ 内部实现问题?
class ConfigureObjectiveFactory : ObjectiveFactory {

    override fun parseInstruction(instruction: Instruction, service: ObjectiveService): Objective {
        val objective = ConfigureObjective(service)
        service.request(AsyncPlayerConnectionConfigureEvent::class.java)
            .priority(EventPriority.NORMAL)
            .handler(objective::onConfigure)
            .uuid { event -> event.connection.profile.id }
            .subscribe(false)
        return objective
    }
}