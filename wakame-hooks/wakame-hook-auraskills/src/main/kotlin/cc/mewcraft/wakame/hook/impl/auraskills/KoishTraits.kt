package cc.mewcraft.wakame.hook.impl.auraskills

import dev.aurelium.auraskills.api.registry.NamespacedId
import dev.aurelium.auraskills.api.trait.CustomTrait

object KoishTraits {


    private val KOISH_ATTRIBUTE_TRAITS = ArrayList<CustomTrait>()

    // 使用懒加载. 按照目前的实现, 读取这个 property 时, KOISH_ATTRIBUTE_TRAITS 应该已经初始化完了.
    val KOISH_ATTRIBUTE_TRAITS_ARRAY: Array<CustomTrait> by lazy { KOISH_ATTRIBUTE_TRAITS.toTypedArray() }

    @JvmField
    val ATTACK_KNOCKBACK: CustomTrait = register(
        TraitType.ATTRIBUTE,
        CustomTrait
            .builder(NamespacedId.of("koish", "attribute_attack_knockback"))
            //.displayName("Attack Knockback")
            .build()
    )

    @JvmField
    val BLOCK_INTERACTION_RANGE: CustomTrait = register(
        TraitType.ATTRIBUTE,
        CustomTrait
            .builder(NamespacedId.of("koish", "attribute_block_interaction_range"))
            //.displayName("Block Interaction Range")
            .build()
    )

    @JvmField
    val ENTITY_INTERACTION_RANGE: CustomTrait = register(
        TraitType.ATTRIBUTE,
        CustomTrait
            .builder(NamespacedId.of("koish", "attribute_entity_interaction_range"))
            //.displayName("Entity Interaction Range")
            .build()
    )

    @JvmField
    val KNOCKBACK_RESISTANCE: CustomTrait = register(
        TraitType.ATTRIBUTE,
        CustomTrait
            .builder(NamespacedId.of("koish", "attribute_knockback_resistance"))
            //.displayName("Knockback Resistance")
            .build()
    )

    @JvmField
    val MAX_ABSORPTION: CustomTrait = register(
        TraitType.ATTRIBUTE,
        CustomTrait
            .builder(NamespacedId.of("koish", "attribute_max_absorption"))
            //.displayName("Max Absorption")
            .build()
    )

    @JvmField
    val MAX_HEALTH: CustomTrait = register(
        TraitType.ATTRIBUTE,
        CustomTrait
            .builder(NamespacedId.of("koish", "attribute_max_health"))
            //.displayName("Max Health")
            .build()
    )

    @JvmField
    val MINING_EFFICIENCY: CustomTrait = register(
        TraitType.ATTRIBUTE,
        CustomTrait
            .builder(NamespacedId.of("koish", "attribute_mining_efficiency"))
            //.displayName("Mining Efficiency")
            .build()
    )

    @JvmField
    val MOVEMENT_SPEED: CustomTrait = register(
        TraitType.ATTRIBUTE,
        CustomTrait
            .builder(NamespacedId.of("koish", "attribute_movement_speed"))
            //.displayName("Movement Speed")
            .build()
    )

    @JvmField
    val SAFE_FALL_DISTANCE: CustomTrait = register(
        TraitType.ATTRIBUTE,
        CustomTrait
            .builder(NamespacedId.of("koish", "attribute_safe_fall_distance"))
            //.displayName("Safe Fall Distance")
            .build()
    )

    @JvmField
    val SCALE: CustomTrait = register(
        TraitType.ATTRIBUTE,
        CustomTrait
            .builder(NamespacedId.of("koish", "attribute_scale"))
            //.displayName("Scale")
            .build()
    )

    @JvmField
    val STEP_HEIGHT: CustomTrait = register(
        TraitType.ATTRIBUTE,
        CustomTrait
            .builder(NamespacedId.of("koish", "attribute_step_height"))
            //.displayName("Step Height")
            .build()
    )

    @JvmField
    val SWEEPING_DAMAGE_RATIO: CustomTrait = register(
        TraitType.ATTRIBUTE,
        CustomTrait
            .builder(NamespacedId.of("koish", "attribute_sweeping_damage_ratio"))
            //.displayName("Sweeping Damage Ratio")
            .build()
    )

    @JvmField
    val WATER_MOVEMENT_EFFICIENCY: CustomTrait = register(
        TraitType.ATTRIBUTE,
        CustomTrait
            .builder(NamespacedId.of("koish", "attribute_water_movement_efficiency"))
            //.displayName("Water Movement Efficiency")
            .build()
    )

    @JvmField
    val ATTACK_EFFECT_CHANCE: CustomTrait = register(
        TraitType.ATTRIBUTE,
        CustomTrait
            .builder(NamespacedId.of("koish", "attribute_attack_effect_chance"))
            //.displayName("Attack Effect Chance")
            .build()
    )

    @JvmField
    val CRITICAL_STRIKE_CHANCE: CustomTrait = register(
        TraitType.ATTRIBUTE,
        CustomTrait
            .builder(NamespacedId.of("koish", "attribute_critical_strike_chance"))
            //.displayName("Critical Strike Chance")
            .build()
    )

    @JvmField
    val CRITICAL_STRIKE_POWER: CustomTrait = register(
        TraitType.ATTRIBUTE,
        CustomTrait
            .builder(NamespacedId.of("koish", "attribute_critical_strike_power"))
            //.displayName("Critical Strike Power")
            .build()
    )

    @JvmField
    val DAMAGE_RATE_BY_UNTARGETED: CustomTrait = register(
        TraitType.ATTRIBUTE,
        CustomTrait
            .builder(NamespacedId.of("koish", "attribute_damage_rate_by_untargeted"))
            //.displayName("Damage Rate By Untargeted")
            .build()
    )

    @JvmField
    val HAMMER_DAMAGE_RANGE: CustomTrait = register(
        TraitType.ATTRIBUTE,
        CustomTrait
            .builder(NamespacedId.of("koish", "attribute_hammer_damage_range"))
            //.displayName("Hammer Damage Range")
            .build()
    )

    @JvmField
    val HAMMER_DAMAGE_RATIO: CustomTrait = register(
        TraitType.ATTRIBUTE,
        CustomTrait
            .builder(NamespacedId.of("koish", "attribute_hammer_damage_ratio"))
            //.displayName("Hammer Damage Ratio")
            .build()
    )

    @JvmField
    val HEALTH_REGENERATION: CustomTrait = register(
        TraitType.ATTRIBUTE,
        CustomTrait
            .builder(NamespacedId.of("koish", "attribute_health_regeneration"))
            //.displayName("Health Regeneration")
            .build()
    )

    @JvmField
    val LIFESTEAL: CustomTrait = register(
        TraitType.ATTRIBUTE,
        CustomTrait
            .builder(NamespacedId.of("koish", "attribute_lifesteal"))
            //.displayName("Lifesteal")
            .build()
    )

    @JvmField
    val NEGATIVE_CRITICAL_STRIKE_POWER: CustomTrait = register(
        TraitType.ATTRIBUTE,
        CustomTrait
            .builder(NamespacedId.of("koish", "attribute_negative_critical_strike_power"))
            //.displayName("Negative Critical Strike Power")
            .build()
    )

    @JvmField
    val NONE_CRITICAL_STRIKE_POWER: CustomTrait = register(
        TraitType.ATTRIBUTE,
        CustomTrait
            .builder(NamespacedId.of("koish", "attribute_none_critical_strike_power"))
            //.displayName("None Critical Strike Power")
            .build()
    )

    @JvmField
    val UNIVERSAL_DEFENSE: CustomTrait = register(
        TraitType.ATTRIBUTE,
        CustomTrait
            .builder(NamespacedId.of("koish", "attribute_universal_defense"))
            //.displayName("Universal Defense")
            .build()
    )

    @JvmField
    val UNIVERSAL_DEFENSE_PENETRATION: CustomTrait = register(
        TraitType.ATTRIBUTE,
        CustomTrait
            .builder(NamespacedId.of("koish", "attribute_universal_defense_penetration"))
            //.displayName("Universal Defense Penetration")
            .build()
    )

    @JvmField
    val UNIVERSAL_DEFENSE_PENETRATION_RATE: CustomTrait = register(
        TraitType.ATTRIBUTE,
        CustomTrait
            .builder(NamespacedId.of("koish", "attribute_universal_defense_penetration_rate"))
            //.displayName("Universal Defense Penetration Rate")
            .build()
    )

    @JvmField
    val UNIVERSAL_MAX_ATTACK_DAMAGE: CustomTrait = register(
        TraitType.ATTRIBUTE,
        CustomTrait
            .builder(NamespacedId.of("koish", "attribute_universal_max_attack_damage"))
            //.displayName("Universal Max Attack Damage")
            .build()
    )

    @JvmField
    val UNIVERSAL_MIN_ATTACK_DAMAGE: CustomTrait = register(
        TraitType.ATTRIBUTE,
        CustomTrait
            .builder(NamespacedId.of("koish", "attribute_universal_min_attack_damage"))
            //.displayName("Universal Min Attack Damage")
            .build()
    )

    enum class TraitType {
        ATTRIBUTE,
    }

    // Intentionally keep the type parameter for future TraitType variants.
    private fun register(type: TraitType, trait: CustomTrait): CustomTrait {
        when (type) {
            TraitType.ATTRIBUTE -> KOISH_ATTRIBUTE_TRAITS.add(trait)
        }
        return trait
    }
}