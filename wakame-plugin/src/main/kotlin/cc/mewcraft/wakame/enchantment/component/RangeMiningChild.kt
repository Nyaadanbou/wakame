package cc.mewcraft.wakame.enchantment.component

import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Player
import java.util.*

/**
 * 用于记录一次范围挖掘的执行状态.
 *
 * 当范围挖掘触发时, 会创建一个 [RangeMiningChild] 用于记录需要破坏的方块列表.
 * 每若干 tick 会根据这个状态破坏一个方块, 直到所有方块都被破坏或玩家离线/失去魔咒.
 *
 * @param player 执行范围挖掘的玩家
 * @param parent 范围挖掘的配置数据
 * @param centerType 中心方块的类型, 用于 requireSameType 判断
 * @param queue 待破坏的方块队列
 */
class RangeMiningChild(
    val player: Player,
    val parent: RangeMining,
    val centerType: Material,
    val queue: ArrayDeque<Block>,
)
