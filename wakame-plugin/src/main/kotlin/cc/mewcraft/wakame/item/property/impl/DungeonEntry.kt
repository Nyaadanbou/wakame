package cc.mewcraft.wakame.item.property.impl

import net.kyori.adventure.key.Key
import org.bukkit.potion.PotionEffect
import org.spongepowered.configurate.objectmapping.ConfigSerializable

/**
 * @param dungeon 地牢的唯一标识符
 * @param structures 入口结构的唯一标识符
 * @param dimensions 入口所在维度的唯一标识符
 * @param partyRadius 组队半径 (单位: 方块)
 * @param requireSneaking 是否需要潜行才能组队
 * @param useEffects 进入时给予玩家的效果列表
 */
@ConfigSerializable
data class DungeonEntry(
    val dungeon: String,
    val structures: Set<Key>,
    val dimensions: Set<Key>,
    val partyRadius: Double = 16.0,
    val requireSneaking: Boolean = false,
    val useEffects: List<PotionEffect> = emptyList(),
)