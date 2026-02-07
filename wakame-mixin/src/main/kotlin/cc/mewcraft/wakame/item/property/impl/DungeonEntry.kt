package cc.mewcraft.wakame.item.property.impl

import net.kyori.adventure.key.Key
import org.bukkit.potion.PotionEffect
import org.spongepowered.configurate.objectmapping.ConfigSerializable

/**
 * @param dungeon 地牢的唯一标识符
 * @param structure 入口结构的唯一标识符
 * @param partyRadius 组队半径 (单位: 方块)
 */
@ConfigSerializable
data class DungeonEntry(
    val dungeon: String,
    val structure: Key,
    val partyRadius: Double,
    val requireSneaking: Boolean = false,
    val useEffects: List<PotionEffect> = emptyList(),
)