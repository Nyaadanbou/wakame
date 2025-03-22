package cc.mewcraft.wakame.ability.trigger

/**
 * 代表一个可以触发此技能的物品变体.
 *
 * ## 用途
 * 触发技能的逻辑在检测一个玩家动作是否能触发技能时,
 * 还会额外检测技能的变体 ([cc.mewcraft.wakame.ability.PlayerAbility.variant])
 * 是否与物品的变体 ([cc.mewcraft.wakame.item.NekoStack.variant]) 相匹配.
 * 如果物品变体不匹配, 即使玩家按对了触发器 ([AbilityTrigger]), 技能最终也不会释放.
 */
interface TriggerVariant {
    /**
     * 变体的唯一标识, 会直接与物品上的变体做比较.
     */
    val id: Int

    companion object {
        /**
         * 返回一个代表任意 [TriggerVariant] 的实例.
         */
        fun any(): TriggerVariant = Any

        /**
         * 从整数创建一个 [TriggerVariant].
         */
        fun of(variant: Int): TriggerVariant = Impl(variant)
    }

    private data class Impl(override val id: Int) : TriggerVariant {
        init {
            require(id != -1) {
                "Cannot create a variant with id '-1'. Use Variant.any() if you want to create an variant that represents any variant"
            }
        }
    }

    private data object Any : TriggerVariant {
        override val id: Int = -1 // magic value
    }
}