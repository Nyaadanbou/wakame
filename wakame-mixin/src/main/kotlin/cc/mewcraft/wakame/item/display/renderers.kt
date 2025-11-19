package cc.mewcraft.wakame.item.display

import cc.mewcraft.wakame.item.isKoish
import cc.mewcraft.wakame.util.MojangStack
import org.bukkit.inventory.ItemStack
import java.nio.file.Path

/**
 * 该接口是其他系统与渲染系统进行交互的主要接口之一.
 *
 * 物品渲染器 [ItemRenderer] 负责修改物品的 *可见组件*,
 * 以便让玩家在特定的情景中, 能够了解到这个物品的基本信息.
 *
 * *可见组件* 包括这些 *可定义任意文本* 的组件:
 * - `minecraft:custom_name`
 * - `minecraft:item_name`
 * - `minecraft:lore`
 * - ...
 *
 * 也包括下面这些 *自带文本内容* 的组件:
 * - `minecraft:attribute_modifiers`
 * - `minecraft:enchantments`
 * - `minecraft:trim`
 * - ...
 *
 * @param T 被渲染的物品的类型
 * @param C 渲染的上下文的类型
 */
interface ItemRenderer<in T, in C> {
    /**
     * 初始化该渲染器.
     *
     * 实现必须读取配置文件, 然后更新实例的相应状态.
     *
     * @param formatPath *渲染格式* 的配置文件路径, 相对于插件数据文件夹
     * @param layoutPath *渲染布局* 的配置文件路径, 相对于插件数据文件夹
     */
    fun initialize(formatPath: Path, layoutPath: Path)

    /**
     * 原地渲染物品 [item], 不使用任何上下文.
     *
     * @param item 服务端上的物品堆叠, 也是需要被渲染的物品
     */
    // 该函数仅仅用于简化 Java 侧的调用 (不然总得带个 null)
    fun render(item: T) = render(item, null)

    /**
     * 原地渲染物品 [item].
     *
     * 实现上, 如果有需要, 可以根据 [context] 产生不同的渲染结果.
     *
     * @param item 服务端上的物品堆叠, 也是需要被渲染的物品
     * @param context 本次渲染的上下文, 是否可为 `null` 取决于实现
     */
    fun render(item: T, context: C? = null)
}

/**
 * 用于渲染从服务端发送到客户端的网络物品堆叠的 [ItemRenderer].
 */
object NetworkRenderer {

    @get:JvmStatic
    @get:JvmName("getInstance")
    lateinit var INSTANCE: ItemRenderer<ItemStack, Nothing>
        private set

    fun register(renderer: ItemRenderer<ItemStack, Nothing>) {
        INSTANCE = renderer
    }

    ///

    /**
     * 检查指定的物品堆叠是否应该由网络渲染处理.
     */
    @JvmStatic
    fun responsible(item: ItemStack): Boolean = item.isKoish

    /**
     * 检查指定的物品堆叠是否应该由网络渲染处理.
     */
    @JvmStatic
    fun responsible(item: MojangStack): Boolean = item.isKoish
}
