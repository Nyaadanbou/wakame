package cc.mewcraft.wakame.hook.impl.auraskills

import cc.mewcraft.wakame.entity.player.PlayerDataLoadingCoordinator
import cc.mewcraft.wakame.entity.player.ResourceLoadingFixHandler
import cc.mewcraft.wakame.integration.Hook
import cc.mewcraft.wakame.integration.playerlevel.PlayerLevelIntegration
import cc.mewcraft.wakame.integration.playerlevel.PlayerLevelType
import cc.mewcraft.wakame.integration.playermana.PlayerManaIntegration
import cc.mewcraft.wakame.integration.playermana.PlayerManaType
import cc.mewcraft.wakame.util.event
import dev.aurelium.auraskills.api.AuraSkillsApi
import dev.aurelium.auraskills.api.event.user.UserLoadEvent
import dev.aurelium.auraskills.api.skill.Skills
import dev.aurelium.auraskills.api.user.SkillsUser
import org.bukkit.entity.Player
import java.util.*

@Hook(plugins = ["AuraSkills"])
object AuraSkillsHook :
    ResourceLoadingFixHandler by AuraResourceLoadingFixHandler,
    PlayerLevelIntegration by AuraPlayerLevelIntegration,
    PlayerManaIntegration by AuraPlayerManaIntegration {

    init {
        PlayerDataLoadingCoordinator.registerExternalStage2Handler("AuraSkills")
    }
}

private object AuraResourceLoadingFixHandler : ResourceLoadingFixHandler {

    override fun fix() {

        event<UserLoadEvent> { event ->
            val player = event.player
            PlayerDataLoadingCoordinator.getOrCreateSession(player).completeStage2()
        }
    }
}

private object AuraPlayerLevelIntegration : PlayerLevelIntegration {

    private val auraApi: AuraSkillsApi by lazy { AuraSkillsApi.get() }

    override val levelType: PlayerLevelType = PlayerLevelType.AURA_SKILLS

    override fun get(uuid: UUID): Int? {
        val user = auraApi.getUser(uuid)
        if (!user.isLoaded) return null

        // 将所有 Skill 等级取平均值作为玩家等级返回
        val skills = Skills.entries
        val result = skills.sumOf { user.getSkillLevel(it) }

        return result
    }
}

private object AuraPlayerManaIntegration : PlayerManaIntegration {

    private val auraApi: AuraSkillsApi by lazy { AuraSkillsApi.get() }

    override val manaType: PlayerManaType = PlayerManaType.AURA_SKILLS

    override fun getMana(player: Player): Double {
        return getAuraUser(player).mana
    }

    override fun setMana(player: Player, amount: Double) {
        getAuraUser(player).mana = amount
    }

    override fun getMaxMana(player: Player): Double {
        return getAuraUser(player).maxMana
    }

    override fun consumeMana(player: Player, amount: Double): Boolean {
        return getAuraUser(player).consumeMana(amount)
    }

    private fun getAuraUser(player: Player): SkillsUser {
        return auraApi.getUser(player.uniqueId)
    }
}