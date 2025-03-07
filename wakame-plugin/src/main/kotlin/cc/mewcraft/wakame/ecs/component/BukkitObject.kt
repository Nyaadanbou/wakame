package cc.mewcraft.wakame.ecs.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.EntityTag

/**
 * 如无特殊说明下面所说的 [Component] 和 [Entity] 都是 ECS 的概念.
 *
 * 拥有该 [Component] 的 [Entity] 代表一个 Bukkit API 的游戏实例在 ECS 系统里对应的 [Entity].
 * 每一个 Bukkit API 的游戏实例 (例如: 方块, 生物, 玩家, ...) 在 ECS 系统下有且仅有一个 [Entity].
 * 反过来也是, 也就是说对于任意拥有 [BukkitObject] 的 [Entity] 都对应一个唯一的 Bukkit API 的游戏实例.
 */
object BukkitObject : EntityTag()
