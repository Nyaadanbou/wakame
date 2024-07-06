package cc.mewcraft.wakame.item.behaviors

import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.behavior.ItemBehavior
import cc.mewcraft.wakame.item.behavior.ItemBehaviorType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.components.FoodProperties
import cc.mewcraft.wakame.item.toNekoStack
import cc.mewcraft.wakame.skill.CasterAdapter
import cc.mewcraft.wakame.skill.Skill
import cc.mewcraft.wakame.skill.TargetAdapter
import cc.mewcraft.wakame.skill.context.SkillContext
import cc.mewcraft.wakame.tick.Ticker
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent

interface Food : ItemBehavior {
    private object Default : Food, KoinComponent {
        override fun handleConsume(player: Player, itemStack: ItemStack, event: PlayerItemConsumeEvent) {
            if (event.isCancelled) {
                return
            }

            val nekoStack: NekoStack = itemStack.toNekoStack
            val food: FoodProperties = nekoStack.components.get(ItemComponentTypes.FOOD) ?: return
            val skills: List<Skill> = food.skills
            val castContext = SkillContext(CasterAdapter.adapt(player), TargetAdapter.adapt(player), nekoStack = nekoStack)
            skills.forEach { Ticker.addTick(it.cast(castContext)) }
        }
    }

    companion object Type : ItemBehaviorType<Food> {
        override fun create(): Food {
            return Default
        }
    }
}


