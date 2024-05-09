package cc.mewcraft.wakame.util

import cc.mewcraft.wakame.attribute.Attributes
import cc.mewcraft.wakame.attribute.Attribute as WakaAttribute
import cc.mewcraft.wakame.attribute.AttributeModifier as WakameAttributeModifier
import org.bukkit.attribute.Attribute as BukkitAttribute
import org.bukkit.attribute.AttributeModifier as BukkitAttributeModifier

fun WakameAttributeModifier.Operation.toBukkit() = when (this) {
    WakameAttributeModifier.Operation.ADD -> BukkitAttributeModifier.Operation.ADD_NUMBER
    WakameAttributeModifier.Operation.MULTIPLY_BASE -> BukkitAttributeModifier.Operation.ADD_SCALAR
    WakameAttributeModifier.Operation.MULTIPLY_TOTAL -> BukkitAttributeModifier.Operation.MULTIPLY_SCALAR_1
}

fun BukkitAttributeModifier.Operation.toNeko() = when (this) {
    BukkitAttributeModifier.Operation.ADD_NUMBER -> WakameAttributeModifier.Operation.ADD
    BukkitAttributeModifier.Operation.ADD_SCALAR -> WakameAttributeModifier.Operation.MULTIPLY_BASE
    BukkitAttributeModifier.Operation.MULTIPLY_SCALAR_1 -> WakameAttributeModifier.Operation.MULTIPLY_TOTAL
}

fun WakameAttributeModifier.toBukkit() = BukkitAttributeModifier(
    id,
    name.orEmpty(),
    amount,
    operation.toBukkit(),
)

fun BukkitAttributeModifier.toNeko() = WakameAttributeModifier(
    uniqueId,
    name.takeIf { it.isNotEmpty() },
    amount,
    operation.toNeko(),
)

fun WakaAttribute.toBukkit(): BukkitAttribute {
    require(this.vanilla) { "Can't convert non-vanilla attribute to Bukkit" }
    return when (this) {
        Attributes.MAX_HEALTH -> BukkitAttribute.GENERIC_MAX_HEALTH
        Attributes.MAX_ABSORPTION -> BukkitAttribute.GENERIC_MAX_ABSORPTION
        Attributes.MOVEMENT_SPEED -> BukkitAttribute.GENERIC_MOVEMENT_SPEED
        Attributes.BLOCK_INTERACTION_RANGE -> return BukkitAttribute.PLAYER_BLOCK_INTERACTION_RANGE
        Attributes.ENTITY_INTERACTION_RANGE -> return BukkitAttribute.PLAYER_ENTITY_INTERACTION_RANGE
        else -> throw IllegalArgumentException("Can't find Bukkit attribute for $this")
    }
}

fun BukkitAttribute.toNeko(): WakaAttribute {
    return when (this) {
        BukkitAttribute.GENERIC_MAX_HEALTH -> Attributes.MAX_HEALTH
        BukkitAttribute.GENERIC_MAX_ABSORPTION -> Attributes.MAX_ABSORPTION
        BukkitAttribute.GENERIC_MOVEMENT_SPEED -> Attributes.MOVEMENT_SPEED
        BukkitAttribute.PLAYER_BLOCK_INTERACTION_RANGE -> return Attributes.BLOCK_INTERACTION_RANGE
        BukkitAttribute.PLAYER_ENTITY_INTERACTION_RANGE -> return Attributes.ENTITY_INTERACTION_RANGE
        else -> throw IllegalArgumentException("Can't find wakame attribute for $this")
    }
}