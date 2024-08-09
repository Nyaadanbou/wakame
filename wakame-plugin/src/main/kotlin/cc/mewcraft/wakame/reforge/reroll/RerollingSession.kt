package cc.mewcraft.wakame.reforge.reroll

import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.components.cells.template.TemplateCore
import cc.mewcraft.wakame.item.template.GenerationContext
import cc.mewcraft.wakame.random3.Group
import net.kyori.adventure.text.Component
import net.kyori.examination.Examinable
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.jetbrains.annotations.Contract

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
     * 重造后的结果.
     *
     * 每当调用函数 [reforge], 该对象会被替换成最新的结果.
     */
    val result: Result

    /**
     * 要被重造的物品.
     */
    @get:Contract(" -> new")
    val inputItem: NekoStack

    /**
     * 每个词条栏的选择状态.
     */
    val selections: SelectionMap

    /**
     * 标记玩家是否已确认重造.
     */
    var confirmed: Boolean

    /**
     * 标记该会话是否已冻结.
     */
    var frozen: Boolean

    /**
     * 标记一个词条栏为“已选择”.
     */
    fun select(id: String)

    /**
     * 标记一个词条栏为“未选择”.
     */
    fun unselected(id: String)

    /**
     * 根据 [selections] 的当前状态, 对 [inputItem] 执行一次重造.
     *
     * 该函数在每次执行后 (其返回前), 会将新的重造结果赋值到 [result].
     */
    fun reforge(): Result

    /**
     * 把输入的原物品还给玩家 [viewer].
     */
    fun returnInput(viewer: Player)

    /**
     * 封装了一次重造的结果.
     */
    interface Result : Examinable {
        /**
         * `true` 表示重造成功.
         */
        val successful: Boolean

        /**
         * 本次重造的总花费.
         */
        val cost: TotalCost

        /**
         * 重造后的物品.
         */
        @get:Contract(" -> new")
        val item: NekoStack

        interface TotalCost : Examinable {
            /**
             * 默认货币的花费.
             */
            val default: Double

            /**
             * 其他货币的花费.
             */
            fun get(currency: String): Double

            /**
             * 检查玩家是否有足够的货币.
             */
            fun test(viewer: Player): Boolean
        }
    }

    /**
     * 封装了单个词条栏的选择状态.
     */
    interface Selection : Examinable {
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
         * 渲染词条栏的逻辑.
         */
        val display: Display

        /**
         * 记录了该词条栏是否被选择重造.
         *
         * `true` 表示该词条栏应该被重造.
         */
        var selected: Boolean

        /**
         * 反转当前的选择 [selected].
         */
        fun invertSelect()

        interface Display : Examinable {
            val name: Component
            val lore: List<Component>

            @Contract(pure = false)
            fun apply(item: ItemStack)
        }
    }

    /**
     * 封装了所有词条栏的选择状态.
     */
    interface SelectionMap : Examinable, Iterable<Map.Entry<String, Selection>> {
        val size: Int
        operator fun get(id: String): Selection?
        operator fun set(id: String, session: Selection)
        operator fun contains(id: String): Boolean
        fun count(predicate: (Selection) -> Boolean): Int
    }
}