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
import cc.mewcraft.wakame.item.schema.meta.SFoodMeta
import cc.mewcraft.wakame.item.schema.meta.SchemaItemMeta
import cc.mewcraft.wakame.skill.Skill
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
     * 食用后玩家将会执行的技能。
     */
    val skills: List<Skill>

    companion object Factory : ItemBehaviorFactory<Food> {
        override fun create(item: NekoItem, behaviorConfig: ConfigProvider): Food {
            val abilities = behaviorConfig.optionalEntry<List<Skill>>("skills").orElse(emptyList())
            return Default(abilities)
        }
    }

    private class Default(
        skills: Provider<List<Skill>>,
    ) : Food {
        override val skills: List<Skill> by skills

        override fun handleConsume(player: Player, itemStack: ItemStack, event: PlayerItemConsumeEvent) {
            skills.forEach { skill ->
                skill.castAt(TargetAdapter.adapt(player))
            }
        }
    }
}


