package cc.mewcraft.wakame.hook.impl.auraskills

import dev.aurelium.auraskills.api.registry.NamespacedId
import dev.aurelium.auraskills.api.trait.CustomTrait

object KoishTraits {

    @JvmField
    val ATTACK_KNOCKBACK: CustomTrait = CustomTrait
        .builder(NamespacedId.of("koish", "attribute/attack_knockback"))
        .displayName("Attack Knockback")
        .build()

    @JvmField
    val BLOCK_INTERACTION_RANGE: CustomTrait = CustomTrait
        .builder(NamespacedId.of("koish", "attribute/block_interaction_range"))
        .displayName("Block Interaction Range")
        .build()

    @JvmField
    val ENTITY_INTERACTION_RANGE: CustomTrait = CustomTrait
        .builder(NamespacedId.of("koish", "attribute/entity_interaction_range"))
        .displayName("Entity Interaction Range")
        .build()

    @JvmField
    val KNOCKBACK_RESISTANCE: CustomTrait = CustomTrait
        .builder(NamespacedId.of("koish", "attribute/knockback_resistance"))
        .displayName("Knockback Resistance")
        .build()

    @JvmField
    val MAX_ABSORPTION: CustomTrait = CustomTrait
        .builder(NamespacedId.of("koish", "attribute/max_absorption"))
        .displayName("Max Absorption")
        .build()

    @JvmField
    val MAX_HEALTH: CustomTrait = CustomTrait
        .builder(NamespacedId.of("koish", "attribute/max_health"))
        .displayName("Max Health")
        .build()

    @JvmField
    val MINING_EFFICIENCY: CustomTrait = CustomTrait
        .builder(NamespacedId.of("koish", "attribute/mining_efficiency"))
        .displayName("Mining Efficiency")
        .build()

    @JvmField
    val MOVEMENT_SPEED: CustomTrait = CustomTrait
        .builder(NamespacedId.of("koish", "attribute/movement_speed"))
        .displayName("Movement Speed")
        .build()

    @JvmField
    val SAFE_FALL_DISTANCE: CustomTrait = CustomTrait
        .builder(NamespacedId.of("koish", "attribute/safe_fall_distance"))
        .displayName("Safe Fall Distance")
        .build()

    @JvmField
    val SCALE: CustomTrait = CustomTrait
        .builder(NamespacedId.of("koish", "attribute/scale"))
        .displayName("Scale")
        .build()

    @JvmField
    val STEP_HEIGHT: CustomTrait = CustomTrait
        .builder(NamespacedId.of("koish", "attribute/step_height"))
        .displayName("Step Height")
        .build()

    @JvmField
    val SWEEPING_DAMAGE_RATIO: CustomTrait = CustomTrait
        .builder(NamespacedId.of("koish", "attribute/sweeping_damage_ratio"))
        .displayName("Sweeping Damage Ratio")
        .build()

    @JvmField
    val WATER_MOVEMENT_EFFICIENCY: CustomTrait = CustomTrait
        .builder(NamespacedId.of("koish", "attribute/water_movement_efficiency"))
        .displayName("Water Movement Efficiency")
        .build()

    @JvmField
    val ATTACK_EFFECT_CHANCE: CustomTrait = CustomTrait
        .builder(NamespacedId.of("koish", "attribute/attack_effect_chance"))
        .displayName("Attack Effect Chance")
        .build()

    @JvmField
    val CRITICAL_STRIKE_CHANCE: CustomTrait = CustomTrait
        .builder(NamespacedId.of("koish", "attribute/critical_strike_chance"))
        .displayName("Critical Strike Chance")
        .build()

    @JvmField
    val CRITICAL_STRIKE_POWER: CustomTrait = CustomTrait
        .builder(NamespacedId.of("koish", "attribute/critical_strike_power"))
        .displayName("Critical Strike Power")
        .build()

    @JvmField
    val DAMAGE_RATE_BY_UNTARGETED: CustomTrait = CustomTrait
        .builder(NamespacedId.of("koish", "attribute/damage_rate_by_untargeted"))
        .displayName("Damage Rate By Untargeted")
        .build()

    @JvmField
    val HAMMER_DAMAGE_RANGE: CustomTrait = CustomTrait
        .builder(NamespacedId.of("koish", "attribute/hammer_damage_range"))
        .displayName("Hammer Damage Range")
        .build()

    @JvmField
    val HAMMER_DAMAGE_RATIO: CustomTrait = CustomTrait
        .builder(NamespacedId.of("koish", "attribute/hammer_damage_ratio"))
        .displayName("Hammer Damage Ratio")
        .build()

    @JvmField
    val HEALTH_REGENERATION: CustomTrait = CustomTrait
        .builder(NamespacedId.of("koish", "attribute/health_regeneration"))
        .displayName("Health Regeneration")
        .build()

    @JvmField
    val LIFESTEAL: CustomTrait = CustomTrait
        .builder(NamespacedId.of("koish", "attribute/lifesteal"))
        .displayName("Lifesteal")
        .build()

    @JvmField
    val NEGATIVE_CRITICAL_STRIKE_POWER: CustomTrait = CustomTrait
        .builder(NamespacedId.of("koish", "attribute/negative_critical_strike_power"))
        .displayName("Negative Critical Strike Power")
        .build()

    @JvmField
    val NONE_CRITICAL_STRIKE_POWER: CustomTrait = CustomTrait
        .builder(NamespacedId.of("koish", "attribute/none_critical_strike_power"))
        .displayName("None Critical Strike Power")
        .build()

    @JvmField
    val UNIVERSAL_DEFENSE: CustomTrait = CustomTrait
        .builder(NamespacedId.of("koish", "attribute/universal_defense"))
        .displayName("Universal Defense")
        .build()

    @JvmField
    val UNIVERSAL_DEFENSE_PENETRATION: CustomTrait = CustomTrait
        .builder(NamespacedId.of("koish", "attribute/universal_defense_penetration"))
        .displayName("Universal Defense Penetration")
        .build()

    @JvmField
    val UNIVERSAL_DEFENSE_PENETRATION_RATE: CustomTrait = CustomTrait
        .builder(NamespacedId.of("koish", "attribute/universal_defense_penetration_rate"))
        .displayName("Universal Defense Penetration Rate")
        .build()

    @JvmField
    val UNIVERSAL_MAX_ATTACK_DAMAGE: CustomTrait = CustomTrait
        .builder(NamespacedId.of("koish", "attribute/universal_max_attack_damage"))
        .displayName("Universal Max Attack Damage")
        .build()

    @JvmField
    val UNIVERSAL_MIN_ATTACK_DAMAGE: CustomTrait = CustomTrait
        .builder(NamespacedId.of("koish", "attribute/universal_min_attack_damage"))
        .displayName("Universal Min Attack Damage")
        .build()
}