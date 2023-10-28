package cc.mewcraft.wakame.attribute

import org.bukkit.inventory.EquipmentSlot
import java.util.*
import kotlin.collections.HashSet

/**
 * A map tied to a player, containing current attributes of the player.
 */
class AttributeMap {
    // Skills activated by this player
    val skillAttributes: Set<SkillAttribute> = HashSet()

    // Numerical attributes activated by this player
    val numericalAttributes: Map<EquipmentSlot, List<NumericalAttributeModifier>> = EnumMap(EquipmentSlot::class.java)
}
