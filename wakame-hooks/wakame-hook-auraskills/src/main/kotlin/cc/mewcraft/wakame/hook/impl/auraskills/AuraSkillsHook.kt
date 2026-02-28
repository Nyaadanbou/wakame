package cc.mewcraft.wakame.hook.impl.auraskills

import cc.mewcraft.lazyconfig.MAIN_CONFIG
import cc.mewcraft.lazyconfig.access.entry
import cc.mewcraft.wakame.KoishDataPaths
import cc.mewcraft.wakame.adventure.translator.TranslatableMessages
import cc.mewcraft.wakame.entity.player.PlayerDataLoadingCoordinator
import cc.mewcraft.wakame.entity.player.ResourceLoadingFixHandler
import cc.mewcraft.wakame.integration.Hook
import cc.mewcraft.wakame.integration.playerlevel.PlayerLevelIntegration
import cc.mewcraft.wakame.integration.playerlevel.PlayerLevelType
import cc.mewcraft.wakame.integration.playermana.PlayerManaIntegration
import cc.mewcraft.wakame.integration.playermana.PlayerManaType
import cc.mewcraft.wakame.util.event
import cc.mewcraft.wakame.util.registerEvents
import dev.aurelium.auraskills.api.AuraSkillsApi
import dev.aurelium.auraskills.api.event.user.UserLoadEvent
import dev.aurelium.auraskills.api.user.SkillsUser
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*

@Hook(plugins = ["AuraSkills"])
object AuraSkillsHook : PlayerManaIntegration by AuraPlayerManaIntegration {

    private val PLAYER_LEVEL_PROVIDER by MAIN_CONFIG.entry<PlayerLevelType>("player_level_provider")
    private val PLAYER_MANA_PROVIDER by MAIN_CONFIG.entry<PlayerManaType>("player_mana_provider")

    init {
        if (PLAYER_LEVEL_PROVIDER == PlayerLevelType.AURA_SKILLS) {
            PlayerLevelIntegration.setImplementation(AuraPlayerLevelIntegration)
            PlayerDataLoadingCoordinator.registerExternalStage2Handler("AuraSkills")
            ResourceLoadingFixHandler.setImplementation(AuraResourceLoadingFixHandler)
        }

        if (PLAYER_MANA_PROVIDER == PlayerManaType.AURA_SKILLS) {
            PlayerManaIntegration.setImplementation(AuraPlayerManaIntegration)
        }

        registerTraits()
        registerTraitHandlers()

        AuraSkillsListener().registerEvents()
    }

    private fun registerTraits() {
        val contentDirectory = KoishDataPaths.CONFIGS.resolve("hook/auraskills").toFile()
        val koishRegistry = AuraSkillsApi.get().useRegistry("koish", contentDirectory)

        koishRegistry.registerTrait(KoishTraits.ATTACK_KNOCKBACK)
        koishRegistry.registerTrait(KoishTraits.BLOCK_INTERACTION_RANGE)
        koishRegistry.registerTrait(KoishTraits.ENTITY_INTERACTION_RANGE)
        koishRegistry.registerTrait(KoishTraits.KNOCKBACK_RESISTANCE)
        koishRegistry.registerTrait(KoishTraits.MAX_ABSORPTION)
        koishRegistry.registerTrait(KoishTraits.MAX_HEALTH)
        koishRegistry.registerTrait(KoishTraits.MINING_EFFICIENCY)
        koishRegistry.registerTrait(KoishTraits.MOVEMENT_SPEED)
        koishRegistry.registerTrait(KoishTraits.SAFE_FALL_DISTANCE)
        koishRegistry.registerTrait(KoishTraits.SCALE)
        koishRegistry.registerTrait(KoishTraits.STEP_HEIGHT)
        koishRegistry.registerTrait(KoishTraits.SWEEPING_DAMAGE_RATIO)
        koishRegistry.registerTrait(KoishTraits.WATER_MOVEMENT_EFFICIENCY)
        koishRegistry.registerTrait(KoishTraits.ATTACK_EFFECT_CHANCE)
        koishRegistry.registerTrait(KoishTraits.CRITICAL_STRIKE_CHANCE)
        koishRegistry.registerTrait(KoishTraits.CRITICAL_STRIKE_POWER)
        koishRegistry.registerTrait(KoishTraits.DAMAGE_RATE_BY_UNTARGETED)
        koishRegistry.registerTrait(KoishTraits.HAMMER_DAMAGE_RANGE)
        koishRegistry.registerTrait(KoishTraits.HAMMER_DAMAGE_RATIO)
        koishRegistry.registerTrait(KoishTraits.HEALTH_REGENERATION)
        koishRegistry.registerTrait(KoishTraits.LIFESTEAL)
        koishRegistry.registerTrait(KoishTraits.NEGATIVE_CRITICAL_STRIKE_POWER)
        koishRegistry.registerTrait(KoishTraits.NONE_CRITICAL_STRIKE_POWER)
        koishRegistry.registerTrait(KoishTraits.UNIVERSAL_DEFENSE)
        koishRegistry.registerTrait(KoishTraits.UNIVERSAL_DEFENSE_PENETRATION)
        koishRegistry.registerTrait(KoishTraits.UNIVERSAL_DEFENSE_PENETRATION_RATE)
        koishRegistry.registerTrait(KoishTraits.UNIVERSAL_MAX_ATTACK_DAMAGE)
        koishRegistry.registerTrait(KoishTraits.UNIVERSAL_MIN_ATTACK_DAMAGE)
    }

    private fun registerTraitHandlers() {
        val auraApi = AuraSkillsApi.get()

        auraApi.handlers.registerTraitHandler(KoishAttributeTrait())
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

    override val type: PlayerLevelType = PlayerLevelType.AURA_SKILLS

    override fun get(uuid: UUID): Int? {
        val user = auraApi.getUser(uuid)
        if (!user.isLoaded) return null

        // 将所有 Skill 等级之和作为玩家等级返回
        val result = user.powerLevel

        return result
    }
}

private object AuraPlayerManaIntegration : PlayerManaIntegration {

    private val auraApi: AuraSkillsApi by lazy { AuraSkillsApi.get() }

    override val type: PlayerManaType = PlayerManaType.AURA_SKILLS

    override fun getMana(player: Player): Double {
        return getAuraUser(player).mana
    }

    override fun getMana(playerId: UUID): Double {
        return getAuraUser(playerId).mana
    }

    override fun setMana(player: Player, amount: Double) {
        getAuraUser(player).mana = amount
    }

    override fun setMana(playerId: UUID, amount: Double) {
        getAuraUser(playerId).mana = amount
    }

    override fun getMaxMana(player: Player): Double {
        return getAuraUser(player).maxMana
    }

    override fun getMaxMana(playerId: UUID): Double {
        return getAuraUser(playerId).maxMana
    }

    override fun consumeMana(player: Player, amount: Double): Boolean {
        val consumeMana = getAuraUser(player).consumeMana(amount)
        if (consumeMana && amount > 0) {
            player.sendActionBar(TranslatableMessages.MSG_MANA_CONSUMED.arguments(Component.text(amount)))
        }
        return consumeMana
    }

    override fun consumeMana(playerId: UUID, amount: Double): Boolean {
        val auraUser = getAuraUser(playerId)
        val consumeMana = auraUser.consumeMana(amount)
        val player = Bukkit.getPlayer(playerId)
        if (player != null && amount > 0) {
            player.sendActionBar(TranslatableMessages.MSG_MANA_CONSUMED.arguments(Component.text(amount)))
        }
        return consumeMana
    }

    private fun getAuraUser(player: Player): SkillsUser {
        return getAuraUser(player.uniqueId)
    }

    private fun getAuraUser(playerId: UUID): SkillsUser {
        return auraApi.getUser(playerId)
    }
}