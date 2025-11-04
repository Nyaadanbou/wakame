package cc.mewcraft.wakame.hook.impl.auraskills

import cc.mewcraft.wakame.KoishDataPaths
import cc.mewcraft.wakame.entity.attribute.Attributes
import cc.mewcraft.wakame.entity.player.PlayerDataLoadingCoordinator
import cc.mewcraft.wakame.entity.player.ResourceLoadingFixHandler
import cc.mewcraft.wakame.integration.Hook
import cc.mewcraft.wakame.integration.playerlevel.PlayerLevelIntegration
import cc.mewcraft.wakame.integration.playerlevel.PlayerLevelType
import cc.mewcraft.wakame.integration.playermana.PlayerManaIntegration
import cc.mewcraft.wakame.integration.playermana.PlayerManaType
import cc.mewcraft.wakame.lifecycle.initializer.DisableFun
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.util.event
import dev.aurelium.auraskills.api.AuraSkillsApi
import dev.aurelium.auraskills.api.event.user.UserLoadEvent
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

    @InitFun
    fun init() {
        registerTraits()
        registerTraitHandlers()
    }

    @DisableFun
    fun disable() {

    }

    private fun registerTraits() {
        val contentDirectory = KoishDataPaths.CONFIGS.resolve("auraskills").toFile()
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

        auraApi.handlers.registerTraitHandler(KoishAttributeTrait(Attributes.ATTACK_KNOCKBACK, KoishTraits.ATTACK_KNOCKBACK))
        auraApi.handlers.registerTraitHandler(KoishAttributeTrait(Attributes.BLOCK_INTERACTION_RANGE, KoishTraits.BLOCK_INTERACTION_RANGE))
        auraApi.handlers.registerTraitHandler(KoishAttributeTrait(Attributes.ENTITY_INTERACTION_RANGE, KoishTraits.ENTITY_INTERACTION_RANGE))
        auraApi.handlers.registerTraitHandler(KoishAttributeTrait(Attributes.KNOCKBACK_RESISTANCE, KoishTraits.KNOCKBACK_RESISTANCE))
        auraApi.handlers.registerTraitHandler(KoishAttributeTrait(Attributes.MAX_ABSORPTION, KoishTraits.MAX_ABSORPTION))
        auraApi.handlers.registerTraitHandler(KoishAttributeTrait(Attributes.MAX_HEALTH, KoishTraits.MAX_HEALTH))
        auraApi.handlers.registerTraitHandler(KoishAttributeTrait(Attributes.MINING_EFFICIENCY, KoishTraits.MINING_EFFICIENCY))
        auraApi.handlers.registerTraitHandler(KoishAttributeTrait(Attributes.MOVEMENT_SPEED, KoishTraits.MOVEMENT_SPEED))
        auraApi.handlers.registerTraitHandler(KoishAttributeTrait(Attributes.SAFE_FALL_DISTANCE, KoishTraits.SAFE_FALL_DISTANCE))
        auraApi.handlers.registerTraitHandler(KoishAttributeTrait(Attributes.SCALE, KoishTraits.SCALE))
        auraApi.handlers.registerTraitHandler(KoishAttributeTrait(Attributes.STEP_HEIGHT, KoishTraits.STEP_HEIGHT))
        auraApi.handlers.registerTraitHandler(KoishAttributeTrait(Attributes.SWEEPING_DAMAGE_RATIO, KoishTraits.SWEEPING_DAMAGE_RATIO))
        auraApi.handlers.registerTraitHandler(KoishAttributeTrait(Attributes.WATER_MOVEMENT_EFFICIENCY, KoishTraits.WATER_MOVEMENT_EFFICIENCY))
        auraApi.handlers.registerTraitHandler(KoishAttributeTrait(Attributes.ATTACK_EFFECT_CHANCE, KoishTraits.ATTACK_EFFECT_CHANCE))
        auraApi.handlers.registerTraitHandler(KoishAttributeTrait(Attributes.CRITICAL_STRIKE_CHANCE, KoishTraits.CRITICAL_STRIKE_CHANCE))
        auraApi.handlers.registerTraitHandler(KoishAttributeTrait(Attributes.CRITICAL_STRIKE_POWER, KoishTraits.CRITICAL_STRIKE_POWER))
        auraApi.handlers.registerTraitHandler(KoishAttributeTrait(Attributes.DAMAGE_RATE_BY_UNTARGETED, KoishTraits.DAMAGE_RATE_BY_UNTARGETED))
        auraApi.handlers.registerTraitHandler(KoishAttributeTrait(Attributes.HAMMER_DAMAGE_RANGE, KoishTraits.HAMMER_DAMAGE_RANGE))
        auraApi.handlers.registerTraitHandler(KoishAttributeTrait(Attributes.HAMMER_DAMAGE_RATIO, KoishTraits.HAMMER_DAMAGE_RATIO))
        auraApi.handlers.registerTraitHandler(KoishAttributeTrait(Attributes.HEALTH_REGENERATION, KoishTraits.HEALTH_REGENERATION))
        auraApi.handlers.registerTraitHandler(KoishAttributeTrait(Attributes.LIFESTEAL, KoishTraits.LIFESTEAL))
        auraApi.handlers.registerTraitHandler(KoishAttributeTrait(Attributes.NEGATIVE_CRITICAL_STRIKE_POWER, KoishTraits.NEGATIVE_CRITICAL_STRIKE_POWER))
        auraApi.handlers.registerTraitHandler(KoishAttributeTrait(Attributes.NONE_CRITICAL_STRIKE_POWER, KoishTraits.NONE_CRITICAL_STRIKE_POWER))
        auraApi.handlers.registerTraitHandler(KoishAttributeTrait(Attributes.UNIVERSAL_DEFENSE, KoishTraits.UNIVERSAL_DEFENSE))
        auraApi.handlers.registerTraitHandler(KoishAttributeTrait(Attributes.UNIVERSAL_DEFENSE_PENETRATION, KoishTraits.UNIVERSAL_DEFENSE_PENETRATION))
        auraApi.handlers.registerTraitHandler(KoishAttributeTrait(Attributes.UNIVERSAL_DEFENSE_PENETRATION_RATE, KoishTraits.UNIVERSAL_DEFENSE_PENETRATION_RATE))
        auraApi.handlers.registerTraitHandler(KoishAttributeTrait(Attributes.UNIVERSAL_MAX_ATTACK_DAMAGE, KoishTraits.UNIVERSAL_MAX_ATTACK_DAMAGE))
        auraApi.handlers.registerTraitHandler(KoishAttributeTrait(Attributes.UNIVERSAL_MIN_ATTACK_DAMAGE, KoishTraits.UNIVERSAL_MIN_ATTACK_DAMAGE))

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

        // 将所有 Skill 等级之和作为玩家等级返回
        val result = user.powerLevel

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