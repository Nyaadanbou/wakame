package cc.mewcraft.wakame.item.property.impl

import org.bukkit.entity.Player
import org.spongepowered.configurate.objectmapping.ConfigSerializable

/**
 * 滑翔时的魔法值消耗配置.
 */
@ConfigSerializable
data class GlidingProfile(
    val glideDrainPerSecond: Double = 0.0,
    val enterGlideManaCost: Double = 0.0,
    val rocketBoostManaCost: Double = 0.0,
)

/**
 * 滑翔时的额外配置, 支持按权限组覆盖.
 *
 * @param default 默认配置, 当玩家不匹配任何权限组时使用
 * @param overrides 权限组覆盖, 按配置文件顺序排列, 越靠后优先级越高
 */
@ConfigSerializable
data class GlidingExtras(
    val default: GlidingProfile = GlidingProfile(),
    val overrides: Map<String, GlidingProfile> = emptyMap(),
) {
    /**
     * 根据玩家的权限解析出最终的 [GlidingProfile].
     *
     * 遍历 [overrides] 中的权限组 (按配置顺序), 取最后一个匹配的; 如果没有任何匹配, 则返回 [default].
     */
    fun resolve(player: Player): GlidingProfile {
        return overrides.entries.lastOrNull { player.hasPermission(it.key) }?.value ?: default
    }
}
