package cc.mewcraft.wakame.item.schema.behavior

import cc.mewcraft.commons.provider.Provider
import cc.mewcraft.commons.provider.immutable.orElse
import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.config.optionalEntry
import cc.mewcraft.wakame.item.binary.NekoStackFactory
import cc.mewcraft.wakame.item.binary.meta
import cc.mewcraft.wakame.item.binary.meta.BDurabilityMeta
import cc.mewcraft.wakame.item.binary.meta.BFoodMeta
import cc.mewcraft.wakame.item.schema.NekoItem
import cc.mewcraft.wakame.item.schema.meta.SDurabilityMeta
import cc.mewcraft.wakame.item.schema.meta.SFoodMeta
import cc.mewcraft.wakame.item.schema.meta.SchemaItemMeta
import cc.mewcraft.wakame.skill.Skill
import cc.mewcraft.wakame.skill.Target
import cc.mewcraft.wakame.skill.TargetAdapter
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger
import kotlin.random.Random
import kotlin.reflect.KClass

/**
 * 可以食用的物品。
 */
interface Food : ItemBehavior {

    override val requiredMetaTypes: Array<KClass<out SchemaItemMeta<*>>>
        get() = arrayOf(SFoodMeta::class)

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
            val eatCostType = behaviorConfig.optionalEntry<EatCost>("eat_cost").orElse(EatCost.ShrinkAmount)
            val abilities = behaviorConfig.optionalEntry<List<Skill>>("skills").orElse(emptyList())
            return Default(eatCostType, abilities)
        }
    }

    private class Default(
        eatCostType: Provider<EatCost>,
        skills: Provider<List<Skill>>,
    ) : Food, KoinComponent {
        private val logger: Logger by inject()
        override val eatCostType: EatCost by eatCostType
        override val skills: List<Skill> by skills

        override fun handleConsume(player: Player, itemStack: ItemStack, event: PlayerItemConsumeEvent) {
            val nekoStack = NekoStackFactory.wrap(itemStack)
            when (eatCostType) {
                EatCost.ShrinkAmount -> {}
                EatCost.Nothing -> {
                    event.isCancelled = true
                    val foodMeta = nekoStack.meta<BFoodMeta>()
                    player.foodLevel += foodMeta.nutrition()
                    player.saturation += foodMeta.saturationModifier() * foodMeta.nutrition() * 2
                    val random = Random
                    foodMeta.effects().forEach { (potionEffect, possibility) ->
                        if (random.nextDouble() < possibility) {
                            potionEffect.apply(player)
                        }
                    }
                }

                is EatCost.ReduceDurability -> {
                    val durabilityMeta = nekoStack.meta<BDurabilityMeta>()
                    if (!durabilityMeta.exists) {
                        logger.warn("物品 ${nekoStack.schema.key} 没有耐久度元数据，无法扣除耐久") //TODO 改为直接加上元数据
                        return
                    }
                    durabilityMeta.changeDurabilityNaturally((eatCostType as EatCost.ReduceDurability).amount)
                }
            }
            skills.forEach { skill ->
                skill.castAt(TargetAdapter.adapt(player))
            }
        }
    }
}

sealed interface EatCost {
    /**
     * 无代价，即可以无限食用。
     */
    data object Nothing : EatCost

    /**
     * 食用后物品数量减少 1。
     */
    data object ShrinkAmount : EatCost

    /**
     * 食用后物品耐久度减少 [amount]。
     */
    data class ReduceDurability(val amount: Int) : EatCost
}

