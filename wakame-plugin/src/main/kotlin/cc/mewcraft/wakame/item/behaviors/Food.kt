package cc.mewcraft.wakame.item.behaviors

import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.behavior.ItemBehavior
import cc.mewcraft.wakame.item.behavior.ItemBehaviorType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.components.FoodProperties
import cc.mewcraft.wakame.item.projectNeko
import cc.mewcraft.wakame.registry.SkillRegistry
import cc.mewcraft.wakame.skill2.Skill
import cc.mewcraft.wakame.skill2.character.CasterAdapter
import cc.mewcraft.wakame.skill2.character.TargetAdapter
import cc.mewcraft.wakame.skill2.context.skillInput
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.inventory.ItemStack

interface Food : ItemBehavior {
    private object Default : Food {
        override fun handleConsume(player: Player, itemStack: ItemStack, event: PlayerItemConsumeEvent) {
            if (event.isCancelled) {
                return
            }

            val nekoStack: NekoStack = itemStack.projectNeko(false)
            val food: FoodProperties = nekoStack.components.get(ItemComponentTypes.FOOD) ?: return
            val skills: List<Skill> = food.skills.map { SkillRegistry.INSTANCES[it] }

            for (skill in skills) {
                val input = skillInput(CasterAdapter.adapt(player)) {
                    target(TargetAdapter.adapt(player))
                    castItem(nekoStack)
                }
                skill.cast(input)
            }
        }
    }

    companion object Type : ItemBehaviorType<Food> {
        override fun create(): Food {
            return Default
        }
    }
}


