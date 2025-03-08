package cc.mewcraft.wakame.ecs.component

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.EntityTag

/**
 * 如无特殊说明下面所说的 [EntityTag] 和 [Entity] 都是 ECS 的概念.
 *
 * 拥有该 [EntityTag] 的 [Entity] 代表一个 Bukkit API 的游戏实例在 ECS 系统里对应的 [Entity].
 * 每一个 Bukkit API 的游戏实例 (例如: 方块, 生物, 玩家, ...) 在 ECS 系统下有且仅有一个 [Entity].
 * 反过来也是, 即任意拥有 [BukkitObject] 的 [Entity] 与 Bukkit API 游戏实例是 one-to-one 的关系.
 */
object BukkitObject : EntityTag()
