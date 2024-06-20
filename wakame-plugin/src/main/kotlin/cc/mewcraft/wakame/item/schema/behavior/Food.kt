package cc.mewcraft.wakame.item.schema.behavior

import cc.mewcraft.commons.provider.Provider
import cc.mewcraft.commons.provider.immutable.orElse
import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.config.optionalEntry
import cc.mewcraft.wakame.item.binary.tryNekoStack
import cc.mewcraft.wakame.item.schema.NekoItem
import cc.mewcraft.wakame.skill.*
import cc.mewcraft.wakame.skill.context.SkillCastContext
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * 可以食用的物品。
 */
interface Food : ItemBehavior {

    /**
     * 食用后玩家将会执行的技能。
     */
    val skills: List<Skill>

    companion object Factory : ItemBehaviorFactory<Food> {
        override fun create(item: NekoItem, config: ConfigProvider): Food {
            val skills = config.optionalEntry<List<Skill>>("skills").orElse(emptyList())
            return Default(skills)
        }
    }

    private class Default(
        skills: Provider<List<Skill>>,
    ) : Food, KoinComponent {
        private val skillCastManager: SkillCastManager by inject()

        override val skills: List<Skill> by skills

        override fun handleConsume(player: Player, itemStack: ItemStack, event: PlayerItemConsumeEvent) {
            skills.forEach { skill ->
                val result = skillCastManager.tryCast(
                    skill,
                    SkillCastContext(
                        CasterAdapter.adapt(player),
                        TargetAdapter.adapt(player),
                        itemStack.tryNekoStack
                    )
                )
                SkillTicker.addChildren(result.skillTick)
            }
        }
    }
}


