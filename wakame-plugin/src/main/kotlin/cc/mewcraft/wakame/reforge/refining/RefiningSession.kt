package cc.mewcraft.wakame.reforge.refining

import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.components.cells.Cell
import cc.mewcraft.wakame.item.components.cells.Core
import cc.mewcraft.wakame.item.components.cells.Curse
import net.kyori.adventure.text.Component
import org.jetbrains.annotations.Contract

/**
 * 代表一个物品定制的过程, 封装了一次定制所需要的所有状态.
 *
 * 当玩家将需要定制的物品 *X* 放入定制台的输入的时候, 将产生一个 [RefiningSession].
 * 如果此时玩家把物品 *X* 取出, 则 [RefiningSession] 将被销毁 (然后让其自然被 GC).
 */
interface RefiningSession {
    /**
     * 被定制的物品, 也就是玩家放入定制台的物品.
     *
     * **该对象的状态不应该由定制台以任何方式修改**; 定制台只能读取该对象的状态, 或对其克隆实例进行写入操作.
     * 这是因为当一个定制的过程被中途取消时, 玩家放入定制台的所有物品都应该被原封不动地放回到玩家的背包中.
     * 因此, 我们在这里暂存玩家放入定制台的物品的原始状态.
     */
    val inputStackSnapshot: NekoStack

    /**
     * 被定制的物品, 也就是玩家放入定制台的物品.
     *
     * 该对象用于实时预览定制后的物品, GUI 应该实时更新该对象的状态.
     */
    val inputStackPreview: NekoStack

    /**
     * 当前所有关于 *核心* 的改动.
     */
    val coreRecipes: Map<String, RefineRecipe<Core>>

    /**
     * 当前所有关于 *诅咒* 的改动.
     */
    val curseRecipes: Map<String, RefineRecipe<Curse>>

    /**
     * 以当前状态定制 [inputStackSnapshot], 并返回一个 [RefineResult].
     *
     * 该函数不会修改 [inputStackSnapshot] 的状态.
     */
    fun reforge(): RefineResult
}

/**
 * 代表*单个*定制所应用的改动, 封装了*单次*替换所需要的所有状态.
 *
 * 对于一个物品一次完整的定制过程, 可以看成是对该物品上的每个词条栏分别进行定制.
 * 玩家可以选择定制他想定制的词条栏; 选择方式就是往定制台上的特定槽位放入特定的物品.
 * *放入*这个操作在代码里的体现, 就是设置 [inputStackSnapshot] 为放入的物品.
 * 一旦放入了一个*合法的*物品, 那么 [inputStackSnapshot] 就不再为 `null`.
 *
 * @param T 定制的类型
 */
interface RefineRecipe<T> {
    /**
     * 要修改的目标词条栏.
     */
    val cell: Cell

    /**
     * 该定制的显示名字, 将充当该定制在定制台GUI上的物品名字.
     */
    val displayName: Component

    /**
     * 该定制的显示描述, 将充当该定制在定制台GUI上的物品描述.
     */
    val displayLore: List<Component>

    /**
     * 玩家当前输入的物品堆叠; **该对象不应该被定制台以任何方式修改**!
     *
     * 这里将输入的物品储存起来, 以便在定制过程被中途取消时放回到玩家背包.
     *
     * - 如果为 `null`, 则说明玩家还没有放入任何物品.
     * - 如果不为 `null`, 则说明已经放入了一个*合法的*物品.
     *
     * 实现必须确保 [inputStackSnapshot] 不为 `null` 时, 其是符合要求的.
     */
    val inputStackSnapshot: NekoStack?

    /**
     * 测试 [replacement] 是否可以应用到 [cell] 上.
     */
    @Contract(pure = true)
    fun test(replacement: T): TestResult

    /**
     * 将 [replacement] 应用到 [cell] 上, 并返回一个新的 [Cell] 对象.
     */
    @Contract(pure = true)
    fun apply(replacement: T): ApplyResult

    /**
     * 一个 [RefineRecipe] 测试输入的结果.
     */
    interface TestResult {
        /**
         * 表示输入的 [T] 可以用在 [cell] 上.
         */
        val isSuccess: Boolean

        /**
         * 表示输入的 [T] 不能用在 [cell] 上.
         */
        val isFailure: Boolean

        /**
         * 该测试结果的文字描述.
         */
        val reason: String // 等设计稳定后, 到时候应该换成一个枚举类
    }

    /**
     * 一个 [RefineRecipe] 应用到词条栏的结果.
     */
    interface ApplyResult {
        /**
         * 返回*修改后*的词条栏.
         */
        val cell: Cell
    }
}

/**
 * 代表一个物品定制的结果. 当玩家按下“定制”按钮后, 将产生一个 [RefineResult].
 */
interface RefineResult {
    /**
     * 定制后的物品.
     *
     * 该物品应该是一个克隆 (新对象).
     */
    val outputStack: NekoStack
}
