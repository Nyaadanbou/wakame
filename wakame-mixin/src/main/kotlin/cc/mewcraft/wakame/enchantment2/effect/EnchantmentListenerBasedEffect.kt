package cc.mewcraft.wakame.enchantment2.effect

import cc.mewcraft.wakame.item.ItemSlot
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.EntityComponentContext

/**
 * 代表一个基于 [org.bukkit.event.Listener] 实现的魔咒效果组件.
 *
 * 这些魔咒的共同点是在实现上同时使用了 ecs 和 listener 两大系统.
 * 这两大系统各自发挥的作用大致如下.
 *
 * ### 往 ecs entity 上添加 ecs component
 * - 当玩家背包里的物品堆叠发生变化时:
 *    - 筛选出需要进行后续处理的物品堆叠.
 *    - 读取物品堆叠上的 *魔咒效果组件*.
 *    - 创建一个与该组件对应的 ecs component, 这个 ecs component 包含了能够使魔咒效果正常运行的所有信息.
 *      例如, 爆破采矿的 ecs component 可以包含“爆破等级”和“最大爆破硬度”.
 *    - 将该 component 添加到 ecs player entity 上.
 *
 * ### listener 执行具体的运行逻辑
 * - 每一个附魔效果组件都有它对应的 listener, 这些 listener 始终在监听, 直到游戏关闭
 * - 当一个受关注的事件发生时, listener 会读取 ecs player entity 上的 component (之前添加的), 并根据其包含的信息执行具体的逻辑.
 *   如果 ecs player entity 上没有 component, 则 listener 不会执行任何逻辑 (相当于这名玩家没有相应的魔咒效果).
 */
interface EnchantmentListenerBasedEffect {

    context(EntityComponentContext)
    fun apply(entity: Entity, level: Int, slot: ItemSlot)

    context(EntityComponentContext)
    fun remove(entity: Entity, level: Int, slot: ItemSlot)

}