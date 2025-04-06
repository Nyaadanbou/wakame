package cc.mewcraft.wakame.entity.player.component

import com.github.quillraven.fleks.EntityTag

/**
 * 标记 Koish 是否应该处理该玩家背包里的物品变化.
 *
 * 当该值为 `false` 时, Koish 不应该处理背包物品的变化.
 * 这意味着玩家背包里的任何物品都不会提供任何来自 Koish 的效果 (例如: 属性, 技能).
 * 当该值为 `true` 时, Koish 应该处理背包物品的变化.
 * 也就是说 Koish 会分析背包里的物品变化并将对应的效果提供给玩家 (或从玩家身上移除).
 *
 * 设计该 [EntityTag] 的原因是: 并不是所有时刻 Koish 都应该处理玩家背包物品的变化.
 */
data object InventoryListenable : EntityTag()