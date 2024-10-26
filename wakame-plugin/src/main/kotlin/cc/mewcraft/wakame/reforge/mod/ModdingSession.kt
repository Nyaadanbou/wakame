package cc.mewcraft.wakame.reforge.mod

import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.components.PortableCore
import cc.mewcraft.wakame.item.components.cells.Cell
import cc.mewcraft.wakame.item.components.cells.Core
import cc.mewcraft.wakame.reforge.common.VariableByPlayer
import cc.mewcraft.wakame.reforge.mod.ModdingSession.ReforgeResult
import net.kyori.adventure.text.Component
import net.kyori.examination.Examinable
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.jetbrains.annotations.Contract
import team.unnamed.mocha.runtime.MochaFunction

/**
 * 代表了一个定制核孔的全过程, 封装了一次定制所需要的所有状态.
 *
 * ## 流程概述
 * 当玩家把要定制的物品 X 放进输入容器的时候, X 应该赋值到 [sourceItem] 属性上.
 * 这时候如果玩家要对某个核孔进行定制, 则需要更新 [replaceParams] 的相应状态.
 *
 * 待一切准备就绪, 调用 [executeReforge] 函数即可进行一次完整的定制;
 * 该函数会基于 [sourceItem] 和 [replaceParams] 的状态进行计算,
 * 并且会在最后返回一个 [ReforgeResult] 对象, 用来表示当前定制的结果.
 *
 * 如果 [executeReforge] 函数返回成功, 那么 [latestResult] 属性也会被重新赋值.
 *
 * ## 其他
 * 当玩家关闭菜单后, 本会话将会被冻结, 无法再次使用.
 */
interface ModdingSession : Examinable {
    /**
     * 本会话使用的定制台.
     */
    val table: ModdingTable

    /**
     * 使用本会话的玩家.
     */
    val viewer: Player

    /**
     * 需要被定制的物品.
     *
     * ### 契约
     * - 当为 `null` 时, 说明此时玩家还没有放入需要定制的物品.
     * - 当不为 `null` 时, 说明此时玩家已经放入了需要定制的物品.
     * - 该物品不一定是合法的可定制物品! 玩家输入什么就是什么.
     * - 该物品不应该被任何形式修改, 应该完整保存输入时的状态.
     * - 访问该物品始终会返回一个克隆.
     *
     * ### 副作用
     * 为该物品赋值将自动执行一次完整的重铸流程, 具体如下:
     * - 如果物品是合法的, [sourceItem] 将不再返回 `null`
     * - 生成新的 [ReplaceMap] 并赋值给 [replaceParams]
     * - 生成新的 [ReforgeResult] 并赋值给 [latestResult]
     */
    @VariableByPlayer
    var inputItem: ItemStack?

    /**
     * 当 [inputItem] 允许被定制时, 该属性将不会返回 `null`.
     * 否则, 当 [inputItem] 为 `null` 或不允许被定制时, 该属性将返回 `null`.
     *
     * ### 契约
     * - 当为 `null` 时, 说明此时没有需要定制的物品, 或者是需要定制的物品不合法.
     * - 当不为 `null` 时, 说明此时有一个合法的物品可以被定制.
     * - 访问该物品始终会返回一个克隆.
     */
    @VariableByPlayer
    val sourceItem: NekoStack?

    /**
     * 储存了每个核孔的定制参数.
     */
    @VariableByPlayer
    val replaceParams: ReplaceMap

    /**
     * 一个 [MochaFunction], 用于计算定制当前物品所需要的货币数量.
     * 该 [MochaFunction] 的返回值会实时反映当前会话的所有状态.
     */
    val totalFunction: MochaFunction

    /**
     * 储存了最新的定制结果.
     */
    @VariableByPlayer
    val latestResult: ReforgeResult

    /**
     * 标记该会话是否已经被冻结.
     *
     * 被冻结的会话不应该再被使用, 也不能更新其状态.
     */
    @VariableByPlayer
    var frozen: Boolean

    /**
     * 以当前参数定制一次 [sourceItem] 并返回一个 [ReforgeResult].
     *
     * 该函数的返回值会重新赋值到 [ModdingSession.latestResult].
     */
    fun executeReforge(): ReforgeResult

    /**
     * 返回输入进本会话的所有物品, 无论这些物品是否应该被消耗.
     * 该函数用于将玩家放入菜单的物品原封不动的归还给玩家.
     */
    fun getAllInputs(): Array<ItemStack>

    /**
     * 返回本会话产生的最终物品输出, 无论是定制失败还是成功.
     * 如果定制成功, 这包括了定制成功后的物品, 以及所有未使用的耗材.
     * 如果定制失败, 则包括了需要被定制的物品, 以及输入的所有耗材.
     */
    fun getFinalOutputs(): Array<ItemStack>

    /**
     * 重置本次会话的所有状态.
     */
    fun reset()

    /**
     * 获取 [sourceItem] 的物品等级.
     * 若不存在则返回 `0`.
     */
    fun getSourceItemLevel(): Int

    /**
     * 获取 [sourceItem] 的物品稀有度所映射的数值.
     * 若不存在则返回 `0`.
     */
    fun getSourceItemRarityNumber(): Double

    /**
     * 获取 [sourceItem] 的总核孔数量.
     * 若不存在则返回 `0`.
     */
    fun getSourceItemTotalCellCount(): Int

    /**
     * 获取当前可以参与定制的核孔的数量.
     * 若不存在则返回 `0`.
     */
    fun getSourceItemChangeableCellCount(): Int

    /**
     * 获取当前参与了定制的核孔的数量.
     * 若不存在则返回 `0`.
     */
    fun getSourceItemChangedCellCount(): Int

    /**
     * 获取当前参与了定制的核孔的定制花费的总和.
     * 若不存在则返回 `0`.
     */
    fun getSourceItemChangedCellCost(): Double

    /**
     * 代表一个物品定制的结果.
     */
    interface ReforgeResult : Examinable {
        /**
         * 是否为空结果.
         */
        val isEmpty: Boolean

        /**
         * 本结果是成功还是失败.
         *
         * - 成功(true)  = 玩家可以取出定制后的物品.
         * - 失败(false) = 玩家无法取出定制后的物品.
         */
        val isSuccess: Boolean

        /**
         * 本结果的描述信息, 将用于展示给玩家.
         */
        val description: List<Component>

        /**
         * 本次定制所消耗的资源.
         */
        val reforgeCost: ReforgeCost

        /**
         * 本次定制输出的物品, 也就是被定制后的物品.
         *
         * 该属性在以下情况下为 `null`:
         * - 本次定制失败, i.e., [ReforgeResult.isSuccess] = `false`
         * - 源物品不存在, i.e., [ModdingSession.sourceItem] = `null`
         */
        @get:Contract(" -> new")
        val output: NekoStack?
    }

    /**
     * 封装了定制操作所消耗的资源.
     */
    interface ReforgeCost : Examinable {
        fun take(viewer: Player)
        fun test(viewer: Player): Boolean
        val description: List<Component>
    }

    /**
     * 代表了一个替换*单个*核心的全过程, 封装了替换过程中所需要的所有状态.
     *
     * 对于一个物品一次完整的定制过程, 可以看成是对该物品上的每个核孔分别进行修改.
     * 玩家可以选择定制他想定制的核孔; 选择方式就是往定制台上的特定槽位*放入*特定的物品.
     * *放入*这个操作在代码里的抽象就是: 将玩家放入的物品传入函数 [Replace.executeReplace].
     */
    interface Replace : Examinable {
        /**
         * 本定制所绑定的会话.
         */
        val session: ModdingSession

        /**
         * 被定制的核孔的唯一标识.
         */
        val id: String

        /**
         * 被定制的核孔.
         */
        val cell: Cell

        /**
         * 被定制的核孔是否可以修改.
         */
        val changeable: Boolean

        /**
         * 被定制的核孔所对应的定制规则.
         */
        val rule: ModdingTable.CellRule

        // 开发日记 2024/8/17
        // 当一个 Replace 实例被创建时, 其对应的 cell & rule & session 也都确定了.
        // 因此, 我们可以在这个时候就编译这个核孔的定制花费, 并且将其缓存起来.
        // 这样只要 sourceItem 没有变化, 这个函数就不需要重新编译以节约性能.
        /**
         * 自定义函数, 计算定制这个核孔所需要的货币数量.
         */
        val total: MochaFunction

        /**
         * 储存了当前最新的定制结果.
         *
         * 当函数 [executeReplace] 被调用后, 该属性将被重新赋值.
         */
        @VariableByPlayer
        val latestResult: Result

        /**
         * 检查是否有耗材放入, 包括非法的耗材.
         */
        @VariableByPlayer
        val hasInput: Boolean

        /**
         * 尝试将耗材 [ingredient] 应用到这个核孔上.
         *
         * ### 副作用
         * 该函数还会将其返回值赋值到属性 [latestResult].
         *
         * @return 一个新的结果
         */
        fun executeReplace(ingredient: NekoStack?): Result

        /**
         * 获取当前 [Result.ingredient] 的物品等级. 若不存在则返回 `0`.
         */
        fun getIngredientLevel(): Int

        /**
         * 获取当前 [Result.ingredient] 的物品稀有度所映射的数值. 若不存在则返回 `0`.
         */
        fun getIngredientRarityNumber(): Double

        /**
         * 封装了定制单个核孔的结果.
         */
        interface Result : Examinable {
            /**
             * 储存了当前用于定制核孔的耗材.
             *
             * 该物品不一定是合法的耗材! 玩家输入的什么, 就是什么.
             * 如需判断耗材是否合法, 调用 [Result.applicable].
             *
             * ### 契约
             * - 当该属性为 `null` 时, 说明此时玩家还没有放入用于定制的耗材
             * - 当该属性不为 `null` 时, 说明此时玩家已经放入了用于定制的耗材
             */
            // TODO 这个 property 应该移到 Replace 中
            @get:Contract(" -> new")
            val ingredient: NekoStack?

            /**
             * 方便函数.
             * 获取 [ingredient] 中包含的便携核心.
             */
            val augment: PortableCore?

            /**
             * [ingredient] 是否可以应用在核孔上.
             */
            val applicable: Boolean

            /**
             * 本定制结果的文字描述, 将展示给玩家.
             */
            val description: List<Component>
        }
    }

    /**
     * 封装了 [ModdingSession] 中所有的 [Replace] 实例.
     */
    interface ReplaceMap : Examinable, Iterable<Map.Entry<String, Replace>> {
        val size: Int
        val keys: Set<String>
        val values: Collection<Replace>

        operator fun get(id: String): Replace?
        operator fun contains(id: String): Boolean

        /**
         * 检查是否有相似的核心已放入该容器.
         */
        fun containsCoreSimilarTo(core: Core): Boolean

        /**
         * 获取玩家放入本会话的所有物品. 主要用于快速归还物品.
         */
        fun getAllInputs(): Array<ItemStack>
    }
}
