package cc.mewcraft.wakame.util

import cc.mewcraft.wakame.attribute.base.Attributes
import cc.mewcraft.wakame.attribute.base.Attribute as WakaAttribute
import cc.mewcraft.wakame.attribute.base.AttributeModifier as WakaAttributeModifier
import org.bukkit.attribute.Attribute as BukkitAttribute
import org.bukkit.attribute.AttributeModifier as BukkitAttributeModifier

fun WakaAttributeModifier.Operation.toBukkit() = when (this) {
    WakaAttributeModifier.Operation.ADD -> BukkitAttributeModifier.Operation.ADD_NUMBER
    WakaAttributeModifier.Operation.MULTIPLY_BASE -> BukkitAttributeModifier.Operation.ADD_SCALAR
    WakaAttributeModifier.Operation.MULTIPLY_TOTAL -> BukkitAttributeModifier.Operation.MULTIPLY_SCALAR_1
}

fun BukkitAttributeModifier.Operation.toWaka() = when (this) {
    BukkitAttributeModifier.Operation.ADD_NUMBER -> WakaAttributeModifier.Operation.ADD
    BukkitAttributeModifier.Operation.ADD_SCALAR -> WakaAttributeModifier.Operation.MULTIPLY_BASE
    BukkitAttributeModifier.Operation.MULTIPLY_SCALAR_1 -> WakaAttributeModifier.Operation.MULTIPLY_TOTAL
}

fun WakaAttributeModifier.toBukkit() = BukkitAttributeModifier(
    id,
    name.orEmpty(),
    amount,
    operation.toBukkit(),
)

fun BukkitAttributeModifier.toWaka() = WakaAttributeModifier(
    uniqueId,
    name.takeIf { it.isNotEmpty() },
    amount,
    operation.toWaka(),
)

fun WakaAttribute.toBukkit(): BukkitAttribute {
    require(Attributes.isVanilla(this)) { "Can't convert non-vanilla attribute to Bukkit" }

    return when (this) {
        Attributes.MAX_HEALTH -> BukkitAttribute.GENERIC_MAX_HEALTH
        Attributes.MAX_ABSORPTION -> BukkitAttribute.GENERIC_MAX_ABSORPTION
        Attributes.MOVEMENT_SPEED_RATE -> BukkitAttribute.GENERIC_MOVEMENT_SPEED
        Attributes.BLOCK_INTERACTION_RANGE -> return BukkitAttribute.GENERIC_ARMOR // TODO: 等新版本支持后修改
        Attributes.ENTITY_INTERACTION_RANGE -> return BukkitAttribute.GENERIC_ARMOR_TOUGHNESS // TODO: 等新版本支持后修改
        else -> throw IllegalArgumentException("Can't find Bukkit attribute for $this")
    }
}

fun BukkitAttribute.toWaka(): WakaAttribute {
    return when (this) {
        BukkitAttribute.GENERIC_MAX_HEALTH -> Attributes.MAX_HEALTH
        BukkitAttribute.GENERIC_MAX_ABSORPTION -> Attributes.MAX_ABSORPTION
        BukkitAttribute.GENERIC_MOVEMENT_SPEED -> Attributes.MOVEMENT_SPEED_RATE
        BukkitAttribute.GENERIC_ARMOR -> return Attributes.BLOCK_INTERACTION_RANGE // TODO: 等新版本支持后修改
        BukkitAttribute.GENERIC_ARMOR_TOUGHNESS -> return Attributes.ENTITY_INTERACTION_RANGE // TODO: 等新版本支持后修改
        else -> throw IllegalArgumentException("Can't find Waka attribute for $this")
    }
}