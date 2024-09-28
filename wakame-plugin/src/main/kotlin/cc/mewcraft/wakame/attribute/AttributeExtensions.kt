package cc.mewcraft.wakame.attribute

import cc.mewcraft.wakame.util.toNamespacedKey
import com.google.common.collect.ImmutableBiMap
import cc.mewcraft.wakame.attribute.Attribute as NekoAttribute
import cc.mewcraft.wakame.attribute.AttributeModifier as NekoAttributeModifier
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
    .put(Attributes.BLOCK_INTERACTION_RANGE, BukkitAttribute.PLAYER_BLOCK_INTERACTION_RANGE)
    .put(Attributes.ENTITY_INTERACTION_RANGE, BukkitAttribute.PLAYER_ENTITY_INTERACTION_RANGE)
    .put(Attributes.MAX_HEALTH, BukkitAttribute.GENERIC_MAX_HEALTH)
    .put(Attributes.MAX_ABSORPTION, BukkitAttribute.GENERIC_MAX_ABSORPTION)
    .put(Attributes.MINING_EFFICIENCY, BukkitAttribute.PLAYER_MINING_EFFICIENCY)
    .put(Attributes.MOVEMENT_SPEED, BukkitAttribute.GENERIC_MOVEMENT_SPEED)
    .put(Attributes.SAFE_FALL_DISTANCE, BukkitAttribute.GENERIC_SAFE_FALL_DISTANCE)
    .put(Attributes.SCALE, BukkitAttribute.GENERIC_SCALE)
    .put(Attributes.STEP_HEIGHT, BukkitAttribute.GENERIC_STEP_HEIGHT)
    .put(Attributes.SWEEPING_DAMAGE_RATIO, BukkitAttribute.PLAYER_SWEEPING_DAMAGE_RATIO)
    .build()

fun NekoAttribute.toBukkit(): BukkitAttribute {
    require(this.vanilla) { "Can't convert non-vanilla attribute to Bukkit" }
    return MAPPINGS[this] ?: throw IllegalArgumentException("Can't convert ${this.descriptionId} to Bukkit attribute")
}

fun BukkitAttribute.toNeko(): NekoAttribute {
    return MAPPINGS.inverse()[this] ?: throw IllegalArgumentException("Can't convert ${this.name} to Neko attribute")
}