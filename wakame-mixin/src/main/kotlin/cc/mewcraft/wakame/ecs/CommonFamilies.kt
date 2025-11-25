package cc.mewcraft.wakame.ecs

import cc.mewcraft.wakame.ecs.bridge.EWorld
import cc.mewcraft.wakame.ecs.component.*
import com.github.quillraven.fleks.Family

/**
 * 所有会长期使用到的 [Family] 都应该在此声明.
 */
object CommonFamilies : Families {

    // ------------------------------
    // 这些 family 为 Bukkit API 游戏实例在 ECS 下的唯一映射
    // 也就是说这些 family 里的每一个 entity, 都存在一个唯一的 Bukkit API 游戏实例与之对应
    // Bukkit API 游戏实例与这些 family 中的 ECS entity 是 one-to-one 的关系
    // ------------------------------

    @JvmField
    val BUKKIT_BLOCK: Family = EWorld.family { all(BukkitObject, BukkitBlock) }

    @JvmField
    val BUKKIT_ENTITY: Family = EWorld.family { all(BukkitObject, BukkitEntity) }

    @JvmField
    val BUKKIT_PLAYER: Family = EWorld.family { all(BukkitObject, BukkitEntity, BukkitPlayer) }

}