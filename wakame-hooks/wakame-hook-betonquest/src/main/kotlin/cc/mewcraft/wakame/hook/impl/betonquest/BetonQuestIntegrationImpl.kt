package cc.mewcraft.wakame.hook.impl.betonquest

import cc.mewcraft.wakame.integration.betonquest.BetonQuestIntegration
import org.betonquest.betonquest.BetonQuest
import org.betonquest.betonquest.api.BetonQuestApi
import org.betonquest.betonquest.api.BetonQuestApiService
import org.bukkit.entity.Player

class BetonQuestIntegrationImpl : BetonQuestIntegration {

    private val api: BetonQuestApi = BetonQuestApiService.get().orElseThrow().api(BetonQuest.getInstance())

    override fun inConversation(player: Player): Boolean {
        val profile = api.profiles().getProfile(player)
        val conversations = api.conversations()
        return conversations.hasActive(profile)
    }
}