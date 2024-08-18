package cc.mewcraft.wakame.reforge.mod

import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.components.PortableCore
import cc.mewcraft.wakame.item.components.cells.Cell
import cc.mewcraft.wakame.reforge.common.VariableByPlayer
import net.kyori.adventure.text.Component
import net.kyori.examination.Examinable
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.jetbrains.annotations.Contract
import team.unnamed.mocha.runtime.MochaFunction

/**
 * 代表一个定制词条栏核心的过程, 封装了一次定制所需要的所有状态.
 *
 * ## 流程概述
 *
 * 当玩家把要定制的物品 X 放进输入容器的时候, X 应该赋值到 [sourceItem] 属性上.
 * 这时候, 如果玩家要对某个词条栏进行定制, 则需要更新 [replaceParams] 的相应状态.
 *
 * 待一切就绪, 调用 [executeReforge] 函数即可进行一次定制;
 * 该函数会基于 [sourceItem] 和 [replaceParams] 的状态来进行一次定制,
 * 并且最后会返回一个 [Result] 对象.
 *
 * 如果 [executeReforge] 函数返回成功, 那么 [latestResult] 属性也会被重新赋值.
 *
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
     * 源物品, 也就是需要被定制的物品.
     *
     * ## 契约
     * - 当为 `null` 时, 说明此时玩家还没有放入需要定制的物品
     * - 当不为 `null` 时, 说明此时玩家已经放入了需要定制的物品
     * - 该物品不一定是合法的定制物品! 玩家输入的什么, 就是什么.
     * - 访问该物品始终会返回一个克隆, 而不是原物品.
     *
     * 给该属性赋值会触发一系列副作用, 具体为:
     * - 生成新的 [ReplaceMap] 并赋值给 [replaceParams]
     * - 生成新的 [Result] 并赋值给 [latestResult]
     */
    @set:Contract("value -> param")
    @get:Contract(" -> new")
    @VariableByPlayer
    var sourceItem: NekoStack?

    /**
     * 检查 [sourceItem] 是否可以被定制.
     */
    @VariableByPlayer
    val isSourceItemLegit: Boolean

    /**
     * 储存了每个词条栏的定制参数.
     */
    @VariableByPlayer
    val replaceParams: ReplaceMap

    /**
     * 定制这个物品所需要的货币数量.
     */
    @VariableByPlayer
    val totalFunction: MochaFunction

    /**
     * 储存了最新的定制结果.
     */
    @VariableByPlayer
    val latestResult: Result

    /**
     * 标记该会话是否已经被冻结.
     *
     * 被冻结的会话不应该再被使用, 也不能更新其状态.
     */
    @VariableByPlayer
    var frozen: Boolean

    /**
     * 以当前参数定制一次 [sourceItem] 并返回一个 [Result].
     *
     * ## 副作用
     *
     * 该函数还会将其返回值赋值给 [ModdingSession.latestResult].
     */
    fun executeReforge(): Result

    /**
     * 获取玩家放入本会话的所有物品, 无论这些物品是否可以参与定制.
     *
     * 例如, 玩家放入了需要定制的一把长剑, 以及几个任意的便携核心, 那么这些物品都会被返回.
     */
    fun getAllPlayerInputs(): Collection<ItemStack>

    /**
     * 获取玩家放入本会话的无法使用的物品.
     *
     * 例如, 玩家放入了需要定制的一把长剑, 以及几个便携核心, 但是这些核心不符合定制规则 (不会参与定制), 那么这些核心就会被返回.
     */
    fun getInapplicablePlayerInputs(): Collection<ItemStack>

    /**
     * 重置本次会话的所有状态.
     */
    fun reset()

    /**
     * 获取 [sourceItem] 的物品等级. 若不存在则返回 `0`.
     */
    fun getSourceItemLevel(): Int

    /**
     * 获取 [sourceItem] 的物品稀有度所映射的数值. 若不存在则返回 `0`.
     */
    fun getSourceItemRarityNumber(): Double

    /**
     * 获取 [sourceItem] 的总词条栏数量. 若不存在则返回 `0`.
     */
    fun getSourceItemTotalCellCount(): Int

    /**
     * 获取当前可以参与定制的词条栏的数量. 若不存在则返回 `0`.
     */
    fun getSourceItemChangeableCellCount(): Int

    /**
     * 获取当前参与了定制的词条栏的数量. 若不存在则返回 `0`.
     */
    fun getSourceItemChangedCellCount(): Int

    /**
     * 获取当前参与了定制的词条栏的定制花费的总和. 若不存在则返回 `0`.
     */
    fun getSourceItemChangedCellCost(): Double

    /**
     * 代表一个物品定制的结果.
     */
    interface Result : Examinable {
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
        val successful: Boolean

        /**
         * 本结果的描述信息, 将用于展示给玩家.
         */
        val description: List<Component>

        /**
         * 本次定制输出的物品, 也就是被定制后的物品.
         *
         * 该属性在以下情况下为 `null`:
         * - 本次定制失败 ([Result.successful] = `false`)
         * - 源物品不存在 ([ModdingSession.sourceItem] = `null`)
         */
        @get:Contract(" -> new")
        val outputItem: NekoStack?

        /**
         * 本次定制所消耗的资源.
         */
        val cost: Cost
    }

    /**
     * 封装了定制操作所消耗的资源.
     */
    interface Cost : Examinable {
        fun take(viewer: Player)
        fun test(viewer: Player): Boolean
        val description: List<Component>
    }

    /**
     * 代表替换*单个*词条栏核心的过程, 封装了替换过程中所需要的所有状态.
     *
     * 对于一个物品一次完整的定制过程, 可以看成是对该物品上的每个词条栏分别进行修改.
     * 玩家可以选择定制他想定制的词条栏; 选择方式就是往定制台上的特定槽位*放入*特定的物品.
     * *放入*这个操作在代码里的抽象就是: 将玩家放入的物品传入函数 [Replace.executeReplace].
     */
    interface Replace : Examinable {
        /**
         * 本定制所绑定的会话.
         */
        val session: ModdingSession

        /**
         * 被定制的词条栏的唯一标识.
         */
        val id: String

        /**
         * 被定制的词条栏.
         */
        val cell: Cell

        /**
         * 被定制的词条栏所对应的定制规则.
         */
        val rule: ModdingTable.CellRule

        /**
         * 被定制的词条栏在菜单中的图标, 用于告诉玩家这个词条栏是什么.
         */
        val display: ItemStack

        // 开发日记 2024/8/17
        // 当一个 Replace 实例被创建时, 其对应的 cell & rule & session 也都确定了.
        // 因此, 我们可以在这个时候就编译这个词条栏的定制花费, 并且将其缓存起来.
        // 这样只要 sourceItem 没有变化, 这个函数就不需要重新编译以节约性能.
        /**
         * 自定义函数, 计算定制这个词条栏所需要的货币数量.
         */
        val totalFunction: MochaFunction

        /**
         * 本定制是否修改过 (即是否有耗材放入).
         */
        @VariableByPlayer
        val changed: Boolean

        /**
         * 储存了当前最新的定制结果.
         *
         * 当函数 [Replace.executeReplace] 被调用后, 该属性将被重新赋值.
         */
        @VariableByPlayer
        val latestResult: Result

        /**
         * 尝试将耗材 [ingredient] 应用到这个词条栏上.
         *
         * ## 副作用
         *
         * 该函数还会将其返回值赋值到属性 [Replace.latestResult].
         *
         * @return 一个新的结果
         */
        @VariableByPlayer
        fun executeReplace(ingredient: NekoStack?): Result

        /**
         * 获取当前 [Result.ingredient] 的物品等级. 若不存在则返回 `0`.
         */
        @VariableByPlayer
        fun getIngredientLevel(): Int

        /**
         * 获取当前 [Result.ingredient] 的物品稀有度所映射的数值. 若不存在则返回 `0`.
         */
        @VariableByPlayer
        fun getIngredientRarityNumber(): Double

        /**
         * 封装了定制单个词条栏的结果.
         */
        interface Result : Examinable {
            /**
             * 储存了当前用于定制词条栏核心的耗材.
             *
             * 该物品不一定是合法的耗材! 玩家输入的什么, 就是什么.
             * 如需判断耗材是否合法, 调用 [Result.applicable].
             *
             * ## 契约
             *
             * - 当该属性为 `null` 时, 说明此时玩家还没有放入用于定制的耗材
             * - 当该属性不为 `null` 时, 说明此时玩家已经放入了用于定制的耗材
             */
            @get:Contract(" -> new")
            val ingredient: NekoStack?

            /**
             * 获取 [Result.ingredient] 中包含的便携核心.
             */
            fun getPortableCore(): PortableCore?

            /**
             * [ingredient] 是否可以应用在词条栏上.
             */
            val applicable: Boolean

            /**
             * 本定制结果的文字描述, 用于展示给玩家.
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
        operator fun set(id: String, replace: Replace)
        operator fun contains(id: String): Boolean

        /**
         * 获取玩家放入本会话的所有物品. 主要用于快速归还物品.
         */
        fun getPlayerInputs(): Collection<ItemStack>
    }
}
