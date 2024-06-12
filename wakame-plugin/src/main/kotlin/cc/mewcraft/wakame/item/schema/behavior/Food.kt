package cc.mewcraft.wakame.item.schema.behavior

import cc.mewcraft.commons.provider.Provider
import cc.mewcraft.commons.provider.immutable.orElse
import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.config.optionalEntry
import cc.mewcraft.wakame.item.schema.NekoItem
import cc.mewcraft.wakame.item.schema.meta.SFoodMeta
import cc.mewcraft.wakame.item.schema.meta.SchemaItemMeta
import cc.mewcraft.wakame.skill.*
import cc.mewcraft.wakame.skill.context.SkillCastContextBuilder
import cc.mewcraft.wakame.skill.context.setCaster
import cc.mewcraft.wakame.skill.context.setItemStack
import cc.mewcraft.wakame.skill.context.setTarget
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.reflect.KClass

/**
 * 可以食用的物品。
 */
interface Food : ItemBehavior {

    override val requiredItemMeta: Array<KClass<out SchemaItemMeta<*>>>
        get() = arrayOf(SFoodMeta::class)

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
                skillCastManager.tryCast(skill,
                    SkillCastContextBuilder.create {
                        setCaster(CasterAdapter.adapt(player))
                        setTarget(TargetAdapter.adapt(player))
                        setItemStack(itemStack)
                    }
                )
            }
        }
    }
}


