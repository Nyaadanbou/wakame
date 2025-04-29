package cc.mewcraft.wakame.item2.data.impl

import org.spongepowered.configurate.objectmapping.ConfigSerializable

/**
 * 记录了一个酒酿配方.
 *
 * 只记录一个 [recipeId] 是因为我们会将 [recipeId] 作为索引去获取完整的配方信息.
 *
 * 这里的属性 [learned] 只是为了实现不同的物品渲染效果. 具体来说:
 * - 当 [learned] 为 `false` 时, 我们会将物品渲染为一个未学习的配方.
 * - 当 [learned] 为 `true` 时, 我们会将物品渲染为一个已学习的配方, 包含完整的配方信息.
 *
 * @property recipeId 配方 id
 * @property learned 该配方的学习状态
 */
@ConfigSerializable
data class ItemBrewRecipe(
    val recipeId: String,
    val learned: Boolean,
)
