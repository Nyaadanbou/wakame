package cc.mewcraft.wakame.item.schema.behavior

import cc.mewcraft.commons.provider.Provider
import cc.mewcraft.commons.provider.immutable.orElse
import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.config.optionalEntry
import cc.mewcraft.wakame.item.schema.NekoItem
import cc.mewcraft.wakame.item.schema.meta.SchemaItemMeta
import cc.mewcraft.wakame.skill.Skill
import kotlin.reflect.KClass

/**
 * 可以食用的物品。
 */
interface Food : ItemBehavior {

    override val requiredMetaTypes: Array<KClass<out SchemaItemMeta<*>>>
        get() = emptyArray()

    // 原版组建实现 - start

    /**
     * 食用后回复的饱食度。
     */
    val nutrition: Int

    /**
     * 食用后回复的饱和度。
     */
    val saturation: Float

    /**
     * 食用所需的时间，单位秒。
     */
    val eatSeconds: Float

    /**
     * 满饱食度时是否可以食用。
     */
    val canAlwaysEat: Boolean

    // 原版组建实现 - end

    /**
     * 食用所需的代价。
     */
    val eatCostType: EatCost

    /**
     * 食用后玩家将会执行的操作。
     */
    val skills: List<Skill>

    companion object Factory : ItemBehaviorFactory<Food> {
        override fun create(item: NekoItem, behaviorConfig: ConfigProvider): Food {
            val nutrition = behaviorConfig.optionalEntry<Int>("nutrition").orElse(0)
            val saturation = behaviorConfig.optionalEntry<Float>("saturation").orElse(0F)
            val eatSeconds = behaviorConfig.optionalEntry<Float>("eat_seconds").orElse(1.6F)
            val canAlwaysEat = behaviorConfig.optionalEntry<Boolean>("can_always_eat").orElse(false)
            val eatCostType = behaviorConfig.optionalEntry<EatCost>("eat_cost").orElse(EatCost.ShrinkAmount(1))
            val abilities = behaviorConfig.optionalEntry<List<Skill>>("skills").orElse(emptyList())
            return Default(nutrition, saturation, eatSeconds, canAlwaysEat, eatCostType, abilities)
        }
    }

    private class Default(
        nutrition: Provider<Int>,
        saturation: Provider<Float>,
        eatSeconds: Provider<Float>,
        canAlwaysEat: Provider<Boolean>,
        eatCostType: Provider<EatCost>,
        abilities: Provider<List<Skill>>,
    ) : Food {
        override val nutrition: Int by nutrition
        override val saturation: Float by saturation
        override val eatSeconds: Float by eatSeconds
        override val canAlwaysEat: Boolean by canAlwaysEat
        override val eatCostType: EatCost by eatCostType
        override val skills: List<Skill> by abilities
    }
}

sealed interface EatCost {
    /**
     * 无代价，即可以无限食用。
     */
    data object Nothing : EatCost

    /**
     * 食用后物品数量减少 [amount]。
     */
    data class ShrinkAmount(val amount: Int) : EatCost

    /**
     * 食用后物品耐久度减少 [amount]。
     */
    data class ReduceDurability(val amount: Int) : EatCost
}

