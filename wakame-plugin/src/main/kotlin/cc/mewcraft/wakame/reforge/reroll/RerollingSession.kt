package cc.mewcraft.wakame.reforge.reroll

import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.components.cells.template.TemplateCore
import cc.mewcraft.wakame.item.template.GenerationContext
import cc.mewcraft.wakame.random3.Group
import cc.mewcraft.wakame.reforge.common.VariableByPlayer
import net.kyori.adventure.text.Component
import net.kyori.examination.Examinable
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.jetbrains.annotations.Contract
import team.unnamed.mocha.runtime.MochaFunction

/**
 * 代表一个物品重造的过程, 封装了一次重造所需要的所有状态.
 */
interface RerollingSession : Examinable {
    /**
     * 玩家.
     */
    val viewer: Player

    /**
     * 重造台.
     */
    val table: RerollingTable

    /**
     * 重造操作的货币花费计算函数.
     */
    val total: MochaFunction

    /**
     * 要被重造的物品; 访问该物品会返回一个克隆.
     *
     * ## 副作用
     *
     * 对该属性赋值时:
     * - 会生成一个新的 [Result] 并赋值给 [RerollingSession.latestResult]
     * - 会生成一个新的 [SelectionMap] 并赋值给 [RerollingSession.selectionMap]
     */
    @VariableByPlayer
    @get:Contract(" -> new")
    @set:Contract("_ -> param")
    var sourceItem: NekoStack?

    /**
     * 每个词条栏的选择状态.
     */
    @VariableByPlayer
    val selectionMap: SelectionMap

    /**
     * 封装了重造后的结果.
     */
    @VariableByPlayer
    val latestResult: Result

    /**
     * 标记该会话是否已冻结.
     */
    @VariableByPlayer
    var frozen: Boolean

    /**
     * 根据会话的当前状态, 执行一次重造.
     *
     * ## 副作用
     * 该函数会将新的结果 [Result] 赋值到属性 [latestResult].
     */
    fun executeReforge(): Result

    /**
     * 重置会话的状态.
     */
    fun reset()

    /**
     * 返回玩家输入的所有物品.
     */
    fun getAllPlayerInputs(): Collection<ItemStack>

    /**
     * 返回玩家输入的未使用的物品.
     */
    fun getUnusedPlayerInputs(): Collection<ItemStack>

    /**
     * 封装了一次重造的结果.
     */
    interface Result : Examinable {
        /**
         * `true` 表示重造已准备就绪.
         */
        val successful: Boolean

        /**
         * 本次重造的结果的描述.
         */
        val description: List<Component>

        /**
         * 重造后的物品.
         */
        @get:Contract(" -> new")
        val item: NekoStack

        /**
         * 本次重造的总花费.
         */
        val cost: Cost
    }

    /**
     * 封装了一次重造所需要消耗的资源.
     */
    interface Cost : Examinable {
        /**
         * 扣除玩家身上相应的货币.
         */
        fun take(viewer: Player)

        /**
         * 检查玩家是否有足够的货币.
         */
        fun test(viewer: Player): Boolean

        /**
         * 本次重造所需资源的描述, 用于展示给玩家.
         */
        val description: List<Component>
    }

    /**
     * 封装了单个词条栏的选择状态.
     */
    interface Selection : Examinable {
        /**
         * 本对象所属的会话.
         */
        val session: RerollingSession

        /**
         * 词条栏的唯一标识.
         */
        val id: String

        /**
         * 词条栏的重造规则.
         */
        val rule: RerollingTable.CellRule

        /**
         * 用于重新随机词条栏核心的掉落表.
         */
        val template: Group<TemplateCore, GenerationContext>

        /**
         * 词条栏在菜单中的图标, 用于告诉玩家这个词条栏是什么.
         */
        val display: ItemStack

        /**
         * 词条栏花费的计算函数.
         */
        val total: MochaFunction

        /**
         * 词条栏是否可以被重造.
         */
        val changeable: Boolean

        /**
         * 记录了该词条栏是否被选择重造.
         *
         * `true` 表示该词条栏应该被重造.
         */
        @VariableByPlayer
        var selected: Boolean

        /**
         * 反转当前 [selected] 的状态.
         *
         * @return 反转后的状态
         */
        fun invert(): Boolean
    }

    /**
     * 封装了所有词条栏的选择状态 ([Selection]).
     *
     * ## 设计哲学
     * 本对象覆盖了物品上所有的词条栏, 无论这个词条栏能否被重造.
     * 对于能被重造的词条栏, 会有一个普通的 [Selection] 对象来表示.
     * 对于不能被重造的词条栏, 会有一个特殊的 [Selection] 对象来表示.
     * 这可以让我们优雅的分别处理这两种情况.
     */
    interface SelectionMap : Examinable, Iterable<Map.Entry<String, Selection>> {
        /**
         * 本对象所属的会话.
         */
        val session: RerollingSession

        val size: Int
        val keys: Set<String>
        val values: Collection<Selection>
        val isEmpty: Boolean

        operator fun get(id: String): Selection?
        operator fun contains(id: String): Boolean
    }
}