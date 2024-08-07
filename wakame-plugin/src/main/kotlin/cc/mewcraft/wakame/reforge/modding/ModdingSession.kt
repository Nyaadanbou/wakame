package cc.mewcraft.wakame.reforge.modding

import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.components.cells.Core
import net.kyori.adventure.text.Component
import net.kyori.examination.Examinable
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.jetbrains.annotations.Contract

/**
 * 代表一个定制词条栏核心的过程, 封装了一次定制所需要的所有状态.
 *
 * 当玩家将需要定制的物品 *X* 放入定制台的输入的时候, 将产生一个 [ModdingSession].
 * 如果此时玩家把物品 *X* 取出, 则 [ModdingSession] 将被永久冻结, 不应再被使用.
 */
interface ModdingSession : Examinable {
    /**
     * 当前使用该会话的玩家.
     */
    val viewer: Player

    /**
     * 获取被定制物品的*克隆*, 也就是玩家放入定制台的物品.
     *
     * 当一个 [ModdingSession] 实例被创建时, [inputItem] 就已经同时确定.
     *
     * **该对象的状态不应该由定制台以任何方式修改**; 定制台只能读取该对象的状态, 或对其克隆实例进行写入操作.
     * 这是因为当一个定制的过程被中途取消时, 玩家放入定制台的所有物品都应该被原封不动地放回到玩家的背包中.
     * 因此, 我们在这里暂存玩家放入定制台的物品的原始状态.
     */
    val inputItem: NekoStack

    /**
     * 被定制后的物品, 也就是 [inputItem] 基于当前状态被修改后的样子.
     * 每当玩家对定制台上的物品进行修改时, [outputItem] 将被实时更新.
     * 初始为 [inputItem] 的克隆.
     */
    var outputItem: NekoStack

    /**
     * 当前每个词条栏的会话.
     */
    val replaceMap: ReplaceMap

    /**
     * 玩家的确认状态.
     *
     * 如果确认状态已经为 `true`, 则玩家可以直接获取 [outputItem] 物品.
     * 否则玩家需要先将确认状态设置为 `true`, 然后再获取 [outputItem] 物品.
     */
    var confirmed: Boolean

    /**
     * 标记该会话是否已经被冻结.
     */
    var frozen: Boolean

    /**
     * 以当前状态修改 [inputItem] (的副本), 并返回一个新的 [Result].
     *
     * 该函数不应该修改 [inputItem] 本身的状态.
     */
    fun reforge(): Result

    /**
     * 代表一个物品定制的结果.
     */
    interface Result {
        /**
         * 定制后的物品.
         *
         * 该物品是一个新对象, 不是原物品对象修改后的状态.
         */
        val item: NekoStack
    }

    /**
     * 代表一个修改*单个*词条栏的过程, 封装了单次修改所需要的所有状态.
     *
     * 对于一个物品一次完整的定制过程, 可以看成是对该物品上的每个词条栏分别进行修改.
     * 玩家可以选择定制他想定制的词条栏; 选择方式就是往定制台上的特定槽位放入特定的物品.
     * *放入*这个操作在代码里的体现, 就是设置 [input] 为玩家放入的(合法)物品.
     * 一旦放入了一个*合法的*物品, 那么 [input] 将不再为 `null`.
     */
    interface Replace {
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
         * 玩家当前输入的定制材料; **该对象不应该被定制台以任何方式修改**!
         *
         * 这里将输入的物品储存起来, 以便在定制过程被中途取消时放回到玩家背包.
         *
         * - 如果为 `null`, 则说明玩家还没有放入任何物品.
         * - 如果不为 `null`, 则说明已经放入了一个*合法的*物品.
         *
         * 实现必须确保当 [inputItem] 不为 `null` 时, 它符合 [rule] 的规则.
         */
        var input: NekoStack?

        /**
         * 测试 [replacement] 是否可以应用到词条栏上.
         */
        @Contract(pure = true)
        fun test(replacement: Core): TestResult

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
         * 一个 [Replace] 测试输入的结果.
         */
        interface TestResult {
            /**
             * 表示输入的核心可以用在词条栏上.
             */
            val successful: Boolean

            /**
             * 该测试结果的文字描述.
             */
            val message: String // 等设计稳定后, 到时候应该换成一个枚举类
        }
    }

    /**
     * [Replace] 的映射. 从词条栏的唯一标识映射到对应的 [Replace].
     */
    interface ReplaceMap : Iterable<Map.Entry<String, Replace>> {
        val size: Int
        fun get(id: String): Replace?
        fun put(id: String, replace: Replace)
        fun contains(id: String): Boolean

        /**
         * 获取所有的由玩家放入输入容器的物品.
         */
        fun getInputItems(): List<ItemStack>
    }
}
