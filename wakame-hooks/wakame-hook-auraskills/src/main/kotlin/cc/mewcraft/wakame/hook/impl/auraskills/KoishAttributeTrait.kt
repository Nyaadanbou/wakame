package cc.mewcraft.wakame.hook.impl.auraskills

import cc.mewcraft.wakame.entity.attribute.Attribute
import cc.mewcraft.wakame.entity.attribute.AttributeModifier
import cc.mewcraft.wakame.entity.attribute.Attributes
import cc.mewcraft.wakame.entity.player.attributeContainer
import dev.aurelium.auraskills.api.AuraSkillsApi
import dev.aurelium.auraskills.api.AuraSkillsBukkit
import dev.aurelium.auraskills.api.bukkit.BukkitTraitHandler
import dev.aurelium.auraskills.api.trait.Trait
import dev.aurelium.auraskills.api.user.SkillsUser
import net.kyori.adventure.key.Key
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerChangedWorldEvent

/**
 * 用于处理 Koish 的所有 Attribute Trait.
 */
class KoishAttributeTrait : Listener, BukkitTraitHandler {

    companion object {

        private val TRAIT_TO_ATTRIBUTE: Map<Trait, Attribute> = hashMapOf(
            KoishTraits.ATTACK_KNOCKBACK to Attributes.ATTACK_KNOCKBACK,
            KoishTraits.BLOCK_INTERACTION_RANGE to Attributes.BLOCK_INTERACTION_RANGE,
            KoishTraits.ENTITY_INTERACTION_RANGE to Attributes.ENTITY_INTERACTION_RANGE,
            KoishTraits.KNOCKBACK_RESISTANCE to Attributes.KNOCKBACK_RESISTANCE,
            KoishTraits.MAX_ABSORPTION to Attributes.MAX_ABSORPTION,
            KoishTraits.MAX_HEALTH to Attributes.MAX_HEALTH,
            KoishTraits.MINING_EFFICIENCY to Attributes.MINING_EFFICIENCY,
            KoishTraits.MOVEMENT_SPEED to Attributes.MOVEMENT_SPEED,
            KoishTraits.SAFE_FALL_DISTANCE to Attributes.SAFE_FALL_DISTANCE,
            KoishTraits.SCALE to Attributes.SCALE,
            KoishTraits.STEP_HEIGHT to Attributes.STEP_HEIGHT,
            KoishTraits.SWEEPING_DAMAGE_RATIO to Attributes.SWEEPING_DAMAGE_RATIO,
            KoishTraits.WATER_MOVEMENT_EFFICIENCY to Attributes.WATER_MOVEMENT_EFFICIENCY,
            KoishTraits.ATTACK_EFFECT_CHANCE to Attributes.ATTACK_EFFECT_CHANCE,
            KoishTraits.CRITICAL_STRIKE_CHANCE to Attributes.CRITICAL_STRIKE_CHANCE,
            KoishTraits.CRITICAL_STRIKE_POWER to Attributes.CRITICAL_STRIKE_POWER,
            KoishTraits.DAMAGE_RATE_BY_UNTARGETED to Attributes.DAMAGE_RATE_BY_UNTARGETED,
            KoishTraits.HAMMER_DAMAGE_RANGE to Attributes.HAMMER_DAMAGE_RANGE,
            KoishTraits.HAMMER_DAMAGE_RATIO to Attributes.HAMMER_DAMAGE_RATIO,
            KoishTraits.HEALTH_REGENERATION to Attributes.HEALTH_REGENERATION,
            KoishTraits.LIFESTEAL to Attributes.LIFESTEAL,
            KoishTraits.NEGATIVE_CRITICAL_STRIKE_POWER to Attributes.NEGATIVE_CRITICAL_STRIKE_POWER,
            KoishTraits.NONE_CRITICAL_STRIKE_POWER to Attributes.NONE_CRITICAL_STRIKE_POWER,
            KoishTraits.UNIVERSAL_DEFENSE to Attributes.UNIVERSAL_DEFENSE,
            KoishTraits.UNIVERSAL_DEFENSE_PENETRATION to Attributes.UNIVERSAL_DEFENSE_PENETRATION,
            KoishTraits.UNIVERSAL_DEFENSE_PENETRATION_RATE to Attributes.UNIVERSAL_DEFENSE_PENETRATION_RATE,
            KoishTraits.UNIVERSAL_MAX_ATTACK_DAMAGE to Attributes.UNIVERSAL_MAX_ATTACK_DAMAGE,
            KoishTraits.UNIVERSAL_MIN_ATTACK_DAMAGE to Attributes.UNIVERSAL_MIN_ATTACK_DAMAGE,
        )

        private val ATTRIBUTE_TO_MODIFIER_KEY: HashMap<Attribute, Key> = HashMap<Attribute, Key>()
    }

    override fun getBaseLevel(player: Player, trait: Trait): Double {
        val attribute = TRAIT_TO_ATTRIBUTE[trait] ?: return .0
        val attributeContainer = player.attributeContainer
        val attributeInstance = attributeContainer.getInstance(attribute) ?: return .0
        var current = attributeContainer.getValue(attribute)

        // 减去由 Trait 添加的部分
        val attributeModifier = attributeInstance.getModifier(getModifierKeyBy(attribute))
        if (attributeModifier != null) {
            current -= attributeModifier.amount
        }

        return current
    }

    override fun onReload(player: Player, user: SkillsUser, trait: Trait) {
        if (trait !in TRAIT_TO_ATTRIBUTE.keys) return
        set(player, user, trait)
    }

    override fun getTraits(): Array<out Trait> {
        return KoishTraits.KOISH_ATTRIBUTE_TRAITS_ARRAY
    }

    @EventHandler
    private fun on(event: PlayerChangedWorldEvent) {
        for (trait in traits) {
            val player = event.player
            val user = AuraSkillsApi.get().getUser(player.uniqueId)
            set(player, user, trait)
        }
    }

    private fun getModifierKeyBy(attribute: Attribute): Key {
        return ATTRIBUTE_TO_MODIFIER_KEY.computeIfAbsent(attribute) { attribute ->
            Key.key("koish", "auraskills/trait/${attribute.id}")
        }
    }

    private fun set(player: Player, user: SkillsUser, trait: Trait) {
        val attribute = TRAIT_TO_ATTRIBUTE[trait] ?: return
        val attributeContainer = player.attributeContainer
        val attributeInstance = attributeContainer.getInstance(attribute) ?: return

        // 先移除原本的由本 Trait 添加的 AttributeModifier
        val modifierKey = getModifierKeyBy(attribute)
        attributeInstance.removeModifier(modifierKey)

        // 遵循 AuraSkills 的一些全局限制
        if (!trait.isEnabled)
            return
        if (AuraSkillsBukkit.get().locationManager.isPluginDisabled(player.location, player))
            return

        val amount = user.getBonusTraitLevel(trait)
        if (amount < 0.01)
            return
        attributeInstance.addTransientModifier(AttributeModifier(modifierKey, amount, AttributeModifier.Operation.ADD))
    }
}