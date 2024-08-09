package cc.mewcraft.wakame.reforge.merge

import cc.mewcraft.wakame.item.NekoStack
import net.kyori.examination.Examinable
import org.bukkit.entity.Player

/**
 * 封装了一次合并物品的操作, 包含了合并中产生的所有状态.
 *
 * ## Gui 大致实现
 *
 */
interface MergingSession : Examinable {
    val viewer: Player
    val table: MergingTable

    /**
     * 输入的第一个物品.
     */
    var inputItem1: NekoStack?

    /**
     * 输入的第二个物品.
     */
    var inputItem2: NekoStack?

    /**
     * 将 [inputItem1] (的克隆) 归还给玩家.
     */
    fun returnInputItem1(viewer: Player)

    /**
     * 将 [inputItem2] (的克隆) 归还给玩家.
     */
    fun returnInputItem2(viewer: Player)

    /**
     * 合并 [inputItem1] 和 [inputItem2] 的结果.
     *
     * 每次调用函数 [merge] 之后, 该属性会被重新赋值.
     */
    val result: Result

    /**
     * 尝试合并 [inputItem1] 和 [inputItem2].
     *
     * 该函数会检查 [inputItem1] 和 [inputItem2] 是否可以合并, 如果可以, 则合并它们.
     *
     * 如果合并成功, [inputItem1] 和 [inputItem2] 将会被设置为 `null` (表示已消耗).
     * 如果合并失败, [inputItem1] 和 [inputItem2] 将会保持不变.
     *
     * 函数的返回值会赋值到成员属性 [result] 之上.
     */
    fun merge(): Result

    /**
     * 标记该会话是否已经被冻结.
     *
     * 被冻结的会话将不再能够调用 [merge].
     */
    val frozen: Boolean

    /**
     * 封装了一次合并操作的结果.
     */
    interface Result : Examinable {
        /**
         * 本次合并是否成功.
         *
         * 如果成功, [item] 将会是合并后的物品, [cost] 将会是合并所消耗的资源.
         * 如果失败, [item] 和 [cost] 将会是空的.
         */
        val successful: Boolean

        /**
         * 合并后的物品.
         *
         * 用户应该始终先检查 [successful] 是否为 `true`, 然后再使用 [item].
         */
        val item: NekoStack

        /**
         * 合并所消耗的资源.
         */
        val cost: Cost
    }

    /**
     * 封装了合并操作所需要消耗的*资源*.
     *
     * *资源*可以是任何东西, 例如货币、经验等.
     */
    interface Cost : Examinable {
        /**
         * 从玩家那里拿走本次合并所需要的资源.
         */
        fun take(viewer: Player)

        /**
         * 检查玩家否有足够的资源完成本次合并.
         */
        fun test(viewer: Player): Boolean
    }
}