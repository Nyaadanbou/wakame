package cc.mewcraft.wakame.hook.impl.betonquest.quest.event.party

import cc.mewcraft.wakame.integration.party.PartyIntegration
import cc.mewcraft.wakame.util.adventure.plain
import org.betonquest.betonquest.api.logger.BetonQuestLogger
import org.betonquest.betonquest.api.profile.OnlineProfile
import org.betonquest.betonquest.api.quest.event.online.OnlineEvent

class LeavePartyEvent(
    private val logger: BetonQuestLogger,
) : OnlineEvent {

    override fun execute(profile: OnlineProfile) {
        val party = PartyIntegration.lookupPartyByPlayer(profile.playerUUID).join()
        if (party != null) {
            party.removeMember(profile.playerUUID)
            logger.info("Removed player ${profile.player.name} from party \"${party.name.plain}\"(${party.id}).")
        }
    }
}