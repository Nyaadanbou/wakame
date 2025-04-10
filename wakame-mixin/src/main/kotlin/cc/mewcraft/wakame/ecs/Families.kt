package cc.mewcraft.wakame.ecs

import cc.mewcraft.wakame.ability2.component.Ability
import cc.mewcraft.wakame.ability2.component.CastBy
import cc.mewcraft.wakame.ability2.component.ManaCost
import cc.mewcraft.wakame.ability2.component.TargetTo
import cc.mewcraft.wakame.ecs.bridge.EWorld
import cc.mewcraft.wakame.ecs.component.*
import cc.mewcraft.wakame.element.component.ElementStack
import cc.mewcraft.wakame.element.component.Elemental
import com.github.quillraven.fleks.Family

/**
 * 所有会长期使用到的 [Family] 都应该在此声明.
 */
object Families {

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

    // ------------------------------
    // 其余 family 为任意系统的具体实现, 其具体定义应该参考具体的文档
    // 在这里声明 family 仅仅是 Fleks 最佳实践 (https://github.com/Quillraven/Fleks/wiki/Family)
    // 创建 family 有消耗, 并且 family 中的 entity 始终会在每 tick 更新
    // ------------------------------

    @JvmField
    val ABILITY: Family = EWorld.family { all(Ability, CastBy, TargetTo, TickCount) }

    @JvmField
    val MANA_COSTING_ABILITY: Family = EWorld.family { all(Ability, CastBy, TargetTo, TickCount, ManaCost) }

    @JvmField
    val ELEMENT_STACK: Family = EWorld.family { all(Elemental, ElementStack, TargetTo) }

    // 用于初始化本 object 里的 val
    fun bootstrap() = Unit

}