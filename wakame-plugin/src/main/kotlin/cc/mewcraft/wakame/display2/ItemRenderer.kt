package cc.mewcraft.wakame.display2

import cc.mewcraft.wakame.display2.implementation.*
import java.nio.file.Path

/**
 * 所有的 [ItemRenderers] 实例.
 */
internal object ItemRenderers {
    @JvmField // 省去无用的函数调用
    val STANDARD = StandardItemRenderer

    @JvmField
    val CRAFTING_STATION = CraftingStationItemRenderer

    @JvmField
    val MERGING_TABLE = MergingTableItemRenderer

    @JvmField
    val MODDING_TABLE = ModdingTableItemRenderer

    @JvmField
    val REROLLING_TABLE = RerollingTableItemRenderer

    @JvmField
    val RECYCLING_STATION = RecyclingStationItemRenderer
}

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
 *
 * 也包括下面这些 *自带文本内容* 的组件:
 * - `minecraft:attribute_modifiers`
 * - `minecraft:enchantments`
 * - `minecraft:trim`
 *
 * @param T 被渲染的物品的类型
 * @param C 渲染的上下文的类型
 */
internal interface ItemRenderer<in T, in C> {
    /**
     * 初始化该渲染器.
     *
     * 实现必须读取配置文件, 然后更新实例的相应状态.
     *
     * @param formatPath *渲染格式* 的配置文件路径, 相对于插件数据文件夹
     * @param layoutPath *渲染布局* 的配置文件路径, 相对于插件数据文件夹
     */
    fun initialize(
        formatPath: Path,
        layoutPath: Path,
    )

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

// 开发日记 2024/9/22
// ItemRenderer 是一个顶级接口, 实现之间会存在较大的区别.
// 例如对于 Standard, 它是基于 ItemComponent, 也就是每个 ItemComponentMap 产生若干个 IndexedText.
// 但对于 CraftingStation, 它是基于 ItemTemplate, 并且可能存在多个 Template 结合产生若干个 IndexedText.
// 而有些 ItemRenderer 可能是基于 ItemComponent 和 ItemTemplate 两者的结合产生若干个 IndexedText.
//
// 作为 ItemRenderer 的用户, 99% 的情况下只需要 render 函数.
// 当然, 用户必须传入正确的 context, 否则渲染不会生效 (NoOp+Log).
