package cc.mewcraft.wakame.reforge.merge

import cc.mewcraft.wakame.attribute.AttributeModifier
import cc.mewcraft.wakame.item.NekoStack
import net.kyori.adventure.text.Component
import net.kyori.examination.Examinable
import org.bukkit.entity.Player
import org.jetbrains.annotations.Contract
import team.unnamed.mocha.runtime.MochaFunction

/**
 * 封装了一次合并*属性核心*的操作, 包含了合并中产生的所有状态.
 */
interface MergingSession : Examinable {
    /**
     * 正在使用该会话的玩家.
     */
    val viewer: Player

    /**
     * 该会话所绑定的合并台.
     */
    val table: MergingTable

    /**
     * 输入的第一个物品.
     */
    @get:Contract(" -> new")
    var inputItemX: NekoStack?

    /**
     * 输入的第二个物品.
     */
    @get:Contract(" -> new")
    var inputItemY: NekoStack?

    /**
     * 将 [inputItemX] (的克隆) 归还给玩家.
     *
     * 该函数返回后, [inputItemX] 将被设置为 `null`.
     */
    fun returnInputItemX(viewer: Player)

    /**
     * 将 [inputItemY] (的克隆) 归还给玩家.
     *
     * 该函数返回后, [inputItemY] 将被设置为 `null`.
     */
    fun returnInputItemY(viewer: Player)

    /**
     * 合并 [inputItemX] 和 [inputItemY] 的结果.
     *
     * 每次调用函数 [merge] 之后, 该属性会被重新赋值.
     */
    val result: Result

    /**
     * 用于计算当前输出核心的合并数值.
     */
    val numberMergeFunction: (MergingTable.NumberMergeFunction.Type) -> MochaFunction

    /**
     * 用于计算当前输出核心的物品等级.
     */
    val outputLevelFunction: MochaFunction

    /**
     * 用于计算当前输出核心的惩罚值.
     */
    val outputPenaltyFunction: MochaFunction

    /**
     * 用于计算当前合并所消耗的金币资源.
     */
    val currencyCostFunction: MochaFunction

    /**
     * 尝试合并 [inputItemX] 和 [inputItemY].
     *
     * 该函数会检查 [inputItemX] 和 [inputItemY] 是否可以合并, 如果可以, 则合并它们.
     *
     * 如果合并成功, [inputItemX] 和 [inputItemY] 将会被设置为 `null` (表示已消耗).
     * 如果合并失败, [inputItemX] 和 [inputItemY] 将会保持不变.
     *
     * 函数的返回值会赋值到成员属性 [result] 之上.
     */
    fun merge(): Result

    /**
     * 标记该会话是否已经被冻结.
     *
     * 被冻结的会话将不再能够调用 [merge].
     */
    var frozen: Boolean

    //<editor-fold desc="获取输入物品的各种数值">

    /* 如果这些值无法获取, 返回 0 */

    /**
     * 获取 [inputItemX] 的属性数值. 若不存在则返回 `0`.
     */
    fun getValueX(): Double

    /**
     * 获取 [inputItemY] 的属性数值. 若不存在则返回 `0`.
     */
    fun getValueY(): Double

    /**
     * 获取 [inputItemX] 的物品等级. 若不存在则返回 `0`.
     */
    fun getLevelX(): Double

    /**
     * 获取 [inputItemY] 的物品等级. 若不存在则返回 `0`.
     */
    fun getLevelY(): Double

    /**
     * 获取 [inputItemX] 的稀有度对应的数值. 若不存在则返回 `0`.
     */
    fun getRarityNumberX(): Double

    /**
     * 获取 [inputItemY] 的稀有度对应的数值. 若不存在则返回 `0`.
     */
    fun getRarityNumberY(): Double

    /**
     * 获取 [inputItemX] 的惩罚值. 若不存在则返回 `0`.
     */
    fun getPenaltyX(): Double

    /**
     * 获取 [inputItemY] 的惩罚值. 若不存在则返回 `0`.
     */
    fun getPenaltyY(): Double
    //</editor-fold>

    /**
     * 封装了一次合并操作的结果.
     */
    sealed interface Result : Examinable {
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
         * 合并的类型.
         *
         * 会根据属性修饰符的运算模式, 大致分为三种.
         */
        val type: Type

        /**
         * 合并所消耗的资源.
         *
         * 资源可以是一切玩家可以拥有和提供的东西.
         */
        val cost: Cost
    }

    /**
     * 封装了合并操作所属的*类型*.
     */
    sealed interface Type : Examinable {
        /**
         * 本类型所对应的属性运算模式.
         */
        val operation: AttributeModifier.Operation

        /**
         * 所属的类型的文字描述, 可用于构建描述信息.
         */
        val description: List<Component>
    }

    /**
     * 封装了合并操作所消耗的*资源*.
     *
     * *资源*可以是任何东西, 例如货币、经验等.
     */
    sealed interface Cost : Examinable {
        /**
         * 从玩家那里拿走本次合并所需要的资源.
         */
        fun take(viewer: Player)

        /**
         * 检查玩家否有足够的资源完成本次合并.
         */
        fun test(viewer: Player): Boolean

        /**
         * 所消耗的资源的文字描述, 可用于构建描述信息.
         */
        val description: List<Component>
    }
}