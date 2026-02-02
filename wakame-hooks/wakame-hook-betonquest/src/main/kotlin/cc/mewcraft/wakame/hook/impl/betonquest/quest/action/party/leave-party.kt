package cc.mewcraft.wakame.hook.impl.betonquest.quest.action.party

import cc.mewcraft.wakame.integration.party.PartyIntegration
import cc.mewcraft.wakame.util.adventure.plain
import org.betonquest.betonquest.api.instruction.Instruction
import org.betonquest.betonquest.api.logger.BetonQuestLogger
import org.betonquest.betonquest.api.logger.BetonQuestLoggerFactory
import org.betonquest.betonquest.api.profile.OnlineProfile
import org.betonquest.betonquest.api.quest.action.OnlineAction
import org.betonquest.betonquest.api.quest.action.PlayerAction
import org.betonquest.betonquest.api.quest.action.PlayerActionFactory
import org.betonquest.betonquest.api.quest.action.online.OnlineActionAdapter


class LeavePartyAction(
    private val logger: BetonQuestLogger,
) : OnlineAction {

    override fun execute(profile: OnlineProfile) {
        val party = PartyIntegration.lookupPartyByPlayer(profile.playerUUID).join()
        if (party != null) {
            party.removeMember(profile.playerUUID)
            logger.info("Removed player ${profile.player.name} from party \"${party.name.plain}\"(${party.id}).")
        }
    }
}


/**
 * @param loggerFactory the logger factory to create a logger for the actions
 */
class LeavePartyActionFactory(
    private val loggerFactory: BetonQuestLoggerFactory,
) : PlayerActionFactory {

    override fun parsePlayer(instruction: Instruction): PlayerAction {
        val logger = loggerFactory.create(LeavePartyAction::class.java)
        val onlineAction = LeavePartyAction(logger)
        val questPackage = instruction.getPackage()
        val adapter = OnlineActionAdapter(onlineAction, logger, questPackage)
        return adapter
    }
}