package cc.mewcraft.wakame.reforge.modding.session

import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.reforge.modding.config.ModdingTable
import net.kyori.adventure.text.Component
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.jetbrains.annotations.Contract
import java.util.stream.Stream

/**
 * 代表一个物品定制的过程, 封装了一次定制所需要的所有状态.
 *
 * 当玩家将需要定制的物品 *X* 放入定制台的输入的时候, 将产生一个 [ModdingSession].
 * 如果此时玩家把物品 *X* 取出, 则 [ModdingSession] 将被销毁 (然后让其自然被 GC).
 *
 * @param T 定制的类型
 */
interface ModdingSession<T> : Examinable {
    /**
     * 包含了 [ModdingSession] 实例的构造方式.
     */
    companion object {
        /**
         * 从一个输入的物品栈创建一个 [ModdingSession] 实例.
         */
        fun <T> of(input: NekoStack): ModdingSession<T> {
            // 1. 从配置文件中读取定制规则
            // 2. 根据规则创建 [Recipe] 实例
            // 3. 返回 [ModdingSession] 实例
        }
    }

    /**
     * 当前使用该会话的玩家.
     */
    val viewer: Player

    /**
     * 被定制的物品, 也就是玩家放入定制台的物品.
     *
     * 当一个 [ModdingSession] 实例被创建时, [input] 就已经同时确定.
     *
     * **该对象的状态不应该由定制台以任何方式修改**; 定制台只能读取该对象的状态, 或对其克隆实例进行写入操作.
     * 这是因为当一个定制的过程被中途取消时, 玩家放入定制台的所有物品都应该被原封不动地放回到玩家的背包中.
     * 因此, 我们在这里暂存玩家放入定制台的物品的原始状态.
     */
    val input: NekoStack

    /**
     * 被定制后的物品, 也就是 [input] 被修改后的样子.
     *
     * 每当玩家对定制台上的物品进行修改时 (例如放了个新的材料到词条栏里), [output] 将被实时更新.
     */
    var output: NekoStack?

    /**
     * 当前所有的修改.
     */
    val recipes: RecipeMap<T>

    /**
     * 标记玩家是否已确认要结束定制.
     */
    var confirmed: Boolean

    /**
     * 标记该会话是否被冻结.
     */
    var frozen: Boolean

    /**
     * 以当前状态修改 [input] (的副本), 并返回一个新的 [Result].
     *
     * 该函数不应该修改 [input] 本身的状态.
     */
    fun reforge(): Result

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("viewer", viewer.name),
        ExaminableProperty.of("input", input),
        ExaminableProperty.of("output", output),
    )

    /**
     * 代表一个物品定制的结果.
     */
    interface Result {
        /**
         * 定制后的物品.
         *
         * 该物品是一个新对象, 不是原物品对象修改后的状态.
         */
        val modded: NekoStack
    }

    /**
     * 代表一个修改*单个*词条栏的过程, 封装了单次修改所需要的所有状态.
     *
     * 对于一个物品一次完整的定制过程, 可以看成是对该物品上的每个词条栏分别进行修改.
     * 玩家可以选择定制他想定制的词条栏; 选择方式就是往定制台上的特定槽位放入特定的物品.
     * *放入*这个操作在代码里的体现, 就是设置 [input] 为放入的物品.
     * 一旦放入了一个*合法的*物品, 那么 [input] 将不再为 `null`.
     *
     * @param T 定制的类型
     */
    interface Recipe<T> {
        /**
         * 该定制对应的词条栏的 id.
         */
        val id: String

        /**
         * 该定制的规则, 由配置文件指定.
         */
        val rule: ModdingTable.CellRule

        /**
         * 该定制在 GUI 中的样子.
         */
        val display: Display

        /**
         * 玩家当前输入的物品堆叠; **该对象不应该被定制台以任何方式修改**!
         *
         * 这里将输入的物品储存起来, 以便在定制过程被中途取消时放回到玩家背包.
         *
         * - 如果为 `null`, 则说明玩家还没有放入任何物品.
         * - 如果不为 `null`, 则说明已经放入了一个*合法的*物品.
         *
         * 实现必须确保 [input] 不为 `null` 时, 它是符合要求的.
         */
        var input: NekoStack?

        /**
         * 测试 [replacement] 是否可以应用到词条栏上.
         */
        @Contract(pure = true)
        fun test(replacement: T): TestResult

        /**
         * 该定制在 GUI 中的样子.
         */
        interface Display {
            val name: Component
            val lore: List<Component>

            /**
             * 将该 [Display] 应用到一个物品上.
             */
            @Contract(pure = false)
            fun apply(item: ItemStack)
        }

        /**
         * 一个 [Recipe] 测试输入的结果.
         */
        interface TestResult {
            /**
             * 表示输入的 [T] 可以用在词条栏上.
             */
            val isSuccess: Boolean

            /**
             * 该测试结果的文字描述.
             */
            val resultType: String // 等设计稳定后, 到时候应该换成一个枚举类
        }
    }

    /**
     * [Recipe] 的映射. 从词条栏 id 映射到对应的 [Recipe].
     */
    interface RecipeMap<T> : Iterable<Map.Entry<String, Recipe<T>>> {
        val size: Int
        fun get(id: String): Recipe<T>?
        fun put(id: String, recipe: Recipe<T>)
        fun contains(id: String): Boolean

        /**
         * 获取所有的由玩家放入输入容器的物品.
         */
        fun getInputItems(): List<ItemStack>
    }
}
