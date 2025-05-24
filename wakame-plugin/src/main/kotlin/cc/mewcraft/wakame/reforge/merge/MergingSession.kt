package cc.mewcraft.wakame.reforge.merge

import cc.mewcraft.wakame.entity.attribute.AttributeModifier
import net.kyori.adventure.text.Component
import net.kyori.examination.Examinable
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
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
    var inputItem1: ItemStack?

    /**
     * 输入的第二个物品.
     */
    @get:Contract(" -> new")
    var inputItem2: ItemStack?

    /**
     * 将 [inputItem1] (的克隆) 归还给玩家.
     *
     * 该函数返回后, [inputItem1] 将被设置为 `null`.
     */
    fun returnInputItem1(viewer: Player)

    /**
     * 将 [inputItem2] (的克隆) 归还给玩家.
     *
     * 该函数返回后, [inputItem2] 将被设置为 `null`.
     */
    fun returnInputItem2(viewer: Player)

    /**
     * 返回本会话产生的最终物品输出, 无论是合并失败还是成功.
     * 如果合并成功, 将返回一个包含合并后物品的数组.
     * 如果合并失败, 将返回一个空数组.
     */
    fun getFinalOutputs(): Array<ItemStack>

    /**
     * 合并 [inputItem1] 和 [inputItem2] 的结果.
     *
     * 每次调用函数 [executeReforge] 之后, 该属性会被重新赋值.
     */
    val latestResult: ReforgeResult

    /**
     * 用于计算当前输出核心的合并数值.
     */
    val valueMergeFunction: (AttributeModifier.Operation) -> MergingTable.ValueMergeMethod.Algorithm

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
     * 尝试合并 [inputItem1] 和 [inputItem2].
     *
     * 该函数会检查 [inputItem1] 和 [inputItem2] 是否可以合并, 如果可以, 则合并它们.
     *
     * 如果合并成功, [inputItem1] 和 [inputItem2] 将会被设置为 `null` (表示已消耗).
     * 如果合并失败, [inputItem1] 和 [inputItem2] 将会保持不变.
     *
     * ## 副作用
     * 函数的返回值会自动赋值给属性 [latestResult].
     */
    fun executeReforge(): ReforgeResult

    /**
     * 重置当前会话的状态. 该函数:
     * - 会把 [inputItem1] 和 [inputItem2] 设置为 `null`.
     * - 会把 [latestResult] 设置为“空”的结果.
     */
    fun reset()

    /**
     * 标记该会话是否已经被冻结.
     *
     * 被冻结的会话将不再能够调用 [executeReforge].
     */
    var frozen: Boolean

    //<editor-fold desc="获取输入物品的各种数值">

    /* 如果这些值无法获取, 返回 0 */

    /**
     * 获取 [inputItem1] 的属性数值. 若不存在则返回 `0`.
     */
    fun getValue1(): Double

    /**
     * 获取 [inputItem2] 的属性数值. 若不存在则返回 `0`.
     */
    fun getValue2(): Double

    /**
     * 获取 [inputItem1] 的物品等级. 若不存在则返回 `0`.
     */
    fun getLevel1(): Double

    /**
     * 获取 [inputItem2] 的物品等级. 若不存在则返回 `0`.
     */
    fun getLevel2(): Double

    /**
     * 获取 [inputItem1] 的稀有度对应的数值. 若不存在则返回 `0`.
     */
    fun getRarityNumber1(): Double

    /**
     * 获取 [inputItem2] 的稀有度对应的数值. 若不存在则返回 `0`.
     */
    fun getRarityNumber2(): Double

    /**
     * 获取 [inputItem1] 的惩罚值. 若不存在则返回 `0`.
     */
    fun getPenalty1(): Double

    /**
     * 获取 [inputItem2] 的惩罚值. 若不存在则返回 `0`.
     */
    fun getPenalty2(): Double
    //</editor-fold>

    /**
     * 封装了一次合并操作的结果.
     */
    sealed interface ReforgeResult {
        /**
         * 本次合并是否成功.
         *
         * 如果成功 (`true`), [output] 将会是合并成功后的物品, [reforgeCost] 将会是合并所消耗的资源,
         * 并且 [output] 和 [reforgeCost] 的所有函数都会正常返回.
         *
         * 如果失败 (`false`), [output] 和 [reforgeCost] 的部分函数将不会正常返回 (会抛异常).
         */
        val isSuccess: Boolean

        /**
         * 本次合并结果的描述.
         */
        val description: Component

        /**
         * 合并的类型.
         *
         * 会根据属性修饰符的运算模式, 大致分为三种.
         */
        val reforgeType: ReforgeType

        /**
         * 合并所消耗的资源.
         *
         * 资源可以是一切玩家可以拥有和提供的东西.
         */
        val reforgeCost: ReforgeCost

        /**
         * 合并后的物品.
         *
         * 如果要将该物品给予玩家, 应该先确保 [isSuccess] 为 `true` 后再进行操作.
         * 当 [isSuccess] 为 `false` 时, 该对象实际上是 [ItemStack.empty].
         */
        @get:Contract(" -> new")
        val output: ItemStack
    }

    /**
     * 封装了合并操作所属的*类型*.
     */
    sealed interface ReforgeType {
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
    sealed interface ReforgeCost {
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