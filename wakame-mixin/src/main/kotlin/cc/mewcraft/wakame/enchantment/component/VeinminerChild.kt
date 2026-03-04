package cc.mewcraft.wakame.enchantment.component

import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Player

/**
 * 用于记录一个连锁挖矿的遍历状态.
 *
 * 当开始遍历一处矿脉时, 会创建一个 ecs entity ([VeinminerChild]) 用于记录遍历的状态.
 * 每 tick 会根据这个 ecs entity 的信息, 破坏相邻方块, 直到整个矿脉遍历完成或达到最大遍历数量.
 * 遍历完成后, 该 ecs entity 会被移除, 表示一个连锁挖矿的效果执行完毕.
 */
class VeinminerChild(
    val player: Player,
    val parent: Veinminer,
    var currentCount: Short,
    val maximumCount: Short,
    val startBlockType: Material,
    val queue: ArrayDeque<Block>,
    val visited: HashSet<Block>,
) {

    constructor(
        player: Player,
        veinminer: Veinminer,
        startBlock: Block,
    ) : this(
        player = player,
        parent = veinminer,
        currentCount = 0,
        maximumCount = veinminer.longestMiningChain,
        startBlockType = startBlock.type,
        queue = ArrayDeque<Block>(12).apply { this.addFirst(startBlock) },
        visited = HashSet<Block>(12).apply { this.add(startBlock) },
    )

    fun sameType(block: Block): Boolean {
        return block.type == startBlockType
    }
}