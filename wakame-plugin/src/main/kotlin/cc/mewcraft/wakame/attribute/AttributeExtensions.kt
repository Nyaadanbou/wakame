package cc.mewcraft.wakame.attribute

import cc.mewcraft.wakame.attribute.Attribute as WakameAttribute
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
    /* uuid = */ id,
    /* name = */ "REMOVE in 1.21",
    /* amount = */ amount,
    /* operation = */ operation.toBukkit(),
)

fun BukkitAttributeModifier.toNeko() = WakameAttributeModifier(
    id = uniqueId,
    amount = amount,
    operation = operation.toNeko(),
)

fun WakameAttribute.toBukkit(): BukkitAttribute {
    require(this.vanilla) { "Can't convert non-vanilla attribute to Bukkit" }
    return when (this) {
        Attributes.MAX_HEALTH -> BukkitAttribute.GENERIC_MAX_HEALTH
        Attributes.MAX_ABSORPTION -> BukkitAttribute.GENERIC_MAX_ABSORPTION
        Attributes.MOVEMENT_SPEED -> BukkitAttribute.GENERIC_MOVEMENT_SPEED
        Attributes.BLOCK_INTERACTION_RANGE -> BukkitAttribute.PLAYER_BLOCK_INTERACTION_RANGE
        Attributes.ENTITY_INTERACTION_RANGE -> BukkitAttribute.PLAYER_ENTITY_INTERACTION_RANGE
        else -> throw IllegalArgumentException("Can't find bukkit attribute for $this")
    }
}

fun BukkitAttribute.toNeko(): WakameAttribute {
    return when (this) {
        BukkitAttribute.GENERIC_MAX_HEALTH -> Attributes.MAX_HEALTH
        BukkitAttribute.GENERIC_MAX_ABSORPTION -> Attributes.MAX_ABSORPTION
        BukkitAttribute.GENERIC_MOVEMENT_SPEED -> Attributes.MOVEMENT_SPEED
        BukkitAttribute.PLAYER_BLOCK_INTERACTION_RANGE -> Attributes.BLOCK_INTERACTION_RANGE
        BukkitAttribute.PLAYER_ENTITY_INTERACTION_RANGE -> Attributes.ENTITY_INTERACTION_RANGE
        else -> throw IllegalArgumentException("Can't find wakame attribute for $this")
    }
}