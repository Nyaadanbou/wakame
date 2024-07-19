package cc.mewcraft.wakame.reforge.refining

import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.components.cells.Cell
import cc.mewcraft.wakame.item.components.cells.Core
import org.jetbrains.annotations.Contract

/**
 * 代表一个物品定制的过程, 封装了一次定制所需要的所有状态.
 */
interface RefiningSession {

    /**
     * 被定制的物品.
     */
    val inputStack: NekoStack

    /**
     * 当前所有关于核心的改动.
     */
    val coreRecipes: Map<String, Recipe>

    /**
     * 当前所有关于诅咒的改动.
     */
    val curseRecipes: Map<String, Recipe>

    /**
     * 以当前状态定制 [inputStack], 并返回一个 [RefineResult].
     */
    fun reforge(): RefineResult

    /**
     * 代表一个定制所应用的改动, 封装了一次替换所需要的所有状态.
     */
    interface Recipe {
        /**
         * 要修改的目标词条栏.
         */
        val cell: Cell

        /**
         * 玩家当前输入的带有便携核心的物品堆叠.
         *
         * 储存起来以便在定制过程被取消时放回到玩家背包.
         *
         * - 如果为 `null`, 则说明玩家还没有放入任何物品.
         * - 如果不为 `null`, 则说明已经放入了一个合法的物品.
         */
        val inputStack: NekoStack?

        /**
         * 测试 [core] 是否可以应用到 [cell] 上.
         */
        @Contract(pure = true)
        fun test(core: Core): TestResult

        /**
         * 将 [core] 应用到 [cell] 上, 并返回一个新的 [Cell] 对象.
         */
        @Contract(pure = true)
        fun apply(core: Core): ApplyResult

        /**
         * 一个 [Recipe] 测试输入的结果.
         */
        interface TestResult {
            /**
             * 表示输入的核心可以用在 [cell] 上.
             */
            val isSuccess: Boolean

            /**
             * 表示输入的核心不能用在 [cell] 上.
             */
            val isFailure: Boolean

            /**
             * 该测试结果的文字描述.
             */
            val reason: String // 等设计稳定后, 到时候应该换成一个枚举类
        }

        /**
         * 一个 [Recipe] 应用到词条栏的结果.
         */
        interface ApplyResult {
            /**
             * 返回*修改后*的词条栏.
             */
            val cell: Cell
        }
    }
}

/**
 * 代表一个物品定制的结果. 当玩家按下“定制”按钮后, 将产生一个 [RefineResult].
 */
interface RefineResult {
    /**
     * 定制后的物品.
     */
    val outputStack: NekoStack
}
