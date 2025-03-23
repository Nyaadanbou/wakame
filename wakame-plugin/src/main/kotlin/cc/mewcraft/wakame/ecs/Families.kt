package cc.mewcraft.wakame.ecs

import cc.mewcraft.wakame.ability.component.*
import cc.mewcraft.wakame.ecs.component.*
import cc.mewcraft.wakame.element.component.ElementComponent
import cc.mewcraft.wakame.element.component.ElementStackComponent
import com.github.quillraven.fleks.Family
import com.github.quillraven.fleks.World.Companion.family

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
    val BUKKIT_BLOCK: Family = family { all(BukkitObject, BukkitBlockComponent) }

    @JvmField
    val BUKKIT_ENTITY: Family = family { all(BukkitObject, BukkitEntityComponent) }

    @JvmField
    val BUKKIT_PLAYER: Family = family { all(BukkitObject, BukkitPlayerComponent) }

    // ------------------------------
    // 其余 family 为任意系统的具体实现, 其具体定义应该参考具体的文档
    // 在这里声明 family 仅仅是 Fleks 最佳实践 (https://github.com/Quillraven/Fleks/wiki/Family)
    // 创建 family 有消耗, 并且 family 中的 entity 始终会在每 tick 更新
    // ------------------------------

    @JvmField
    val ABILITY: Family = family { all(AbilityComponent, CastBy, TargetTo, TickCountComponent, AbilityArchetypeComponent) }

    @JvmField
    val MANA_COSTING_ABILITY: Family = family { all(AbilityComponent, CastBy, TargetTo, TickCountComponent, AbilityArchetypeComponent, ManaCost) }

    @JvmField
    val ELEMENT_STACK: Family = family { all(ElementComponent, ElementStackComponent, TargetTo) }

    // 用于初始化本 object 里的 val
    internal fun bootstrap() = Unit

}