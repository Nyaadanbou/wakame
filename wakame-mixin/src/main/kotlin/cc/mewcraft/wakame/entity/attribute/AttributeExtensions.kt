package cc.mewcraft.wakame.entity.attribute

import cc.mewcraft.wakame.util.adventure.toNamespacedKey
import com.google.common.collect.ImmutableBiMap
import cc.mewcraft.wakame.entity.attribute.Attribute as NekoAttribute
import cc.mewcraft.wakame.entity.attribute.AttributeModifier as NekoAttributeModifier
import org.bukkit.attribute.Attribute as BukkitAttribute
import org.bukkit.attribute.AttributeModifier as BukkitAttributeModifier

fun NekoAttributeModifier.Operation.toBukkit() = when (this) {
    NekoAttributeModifier.Operation.ADD -> BukkitAttributeModifier.Operation.ADD_NUMBER
    NekoAttributeModifier.Operation.MULTIPLY_BASE -> BukkitAttributeModifier.Operation.ADD_SCALAR
    NekoAttributeModifier.Operation.MULTIPLY_TOTAL -> BukkitAttributeModifier.Operation.MULTIPLY_SCALAR_1
}

fun BukkitAttributeModifier.Operation.toNeko() = when (this) {
    BukkitAttributeModifier.Operation.ADD_NUMBER -> NekoAttributeModifier.Operation.ADD
    BukkitAttributeModifier.Operation.ADD_SCALAR -> NekoAttributeModifier.Operation.MULTIPLY_BASE
    BukkitAttributeModifier.Operation.MULTIPLY_SCALAR_1 -> NekoAttributeModifier.Operation.MULTIPLY_TOTAL
}

fun NekoAttributeModifier.toBukkit() = BukkitAttributeModifier(id.toNamespacedKey, amount, operation.toBukkit())
fun BukkitAttributeModifier.toNeko() = NekoAttributeModifier(key, amount, operation.toNeko())

private val MAPPINGS: ImmutableBiMap<NekoAttribute, BukkitAttribute> = ImmutableBiMap.builder<NekoAttribute, BukkitAttribute>()
    .put(Attributes.ATTACK_KNOCKBACK, BukkitAttribute.ATTACK_KNOCKBACK)
    .put(Attributes.BLOCK_INTERACTION_RANGE, BukkitAttribute.BLOCK_INTERACTION_RANGE)
    .put(Attributes.ENTITY_INTERACTION_RANGE, BukkitAttribute.ENTITY_INTERACTION_RANGE)
    .put(Attributes.KNOCKBACK_RESISTANCE, BukkitAttribute.KNOCKBACK_RESISTANCE)
    .put(Attributes.MAX_HEALTH, BukkitAttribute.MAX_HEALTH)
    .put(Attributes.MAX_ABSORPTION, BukkitAttribute.MAX_ABSORPTION)
    .put(Attributes.MINING_EFFICIENCY, BukkitAttribute.MINING_EFFICIENCY)
    .put(Attributes.MOVEMENT_SPEED, BukkitAttribute.MOVEMENT_SPEED)
    .put(Attributes.SAFE_FALL_DISTANCE, BukkitAttribute.SAFE_FALL_DISTANCE)
    .put(Attributes.SCALE, BukkitAttribute.SCALE)
    .put(Attributes.STEP_HEIGHT, BukkitAttribute.STEP_HEIGHT)
    .put(Attributes.SWEEPING_DAMAGE_RATIO, BukkitAttribute.SWEEPING_DAMAGE_RATIO)
    .put(Attributes.WATER_MOVEMENT_EFFICIENCY, BukkitAttribute.WATER_MOVEMENT_EFFICIENCY)
    .build()

fun NekoAttribute.toBukkit(): BukkitAttribute {
    require(this.vanilla) { "Can't convert non-vanilla attribute to Bukkit" }
    return MAPPINGS[this] ?: throw IllegalArgumentException("Can't convert ${this.id} to Bukkit attribute")
}

fun BukkitAttribute.toNeko(): NekoAttribute {
    return MAPPINGS.inverse()[this] ?: throw IllegalArgumentException("Can't convert ${this.key} to Neko attribute")
}