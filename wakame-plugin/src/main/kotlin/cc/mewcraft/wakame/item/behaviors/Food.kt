package cc.mewcraft.wakame.item.behaviors

import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.behavior.ItemBehavior
import cc.mewcraft.wakame.item.behavior.ItemBehaviorType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.components.FoodProperties
import cc.mewcraft.wakame.item.toNekoStack
import cc.mewcraft.wakame.registry.SkillRegistry
import cc.mewcraft.wakame.skill2.MechanicWorldInteraction
import cc.mewcraft.wakame.skill2.Skill
import cc.mewcraft.wakame.skill2.character.CasterAdapter
import cc.mewcraft.wakame.skill2.character.TargetAdapter
import cc.mewcraft.wakame.skill2.character.toComposite
import cc.mewcraft.wakame.skill2.context.ImmutableSkillContext
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface Food : ItemBehavior {
    private object Default : Food, KoinComponent {
        private val mechanicWorldInteraction: MechanicWorldInteraction by inject()

        override fun handleConsume(player: Player, itemStack: ItemStack, event: PlayerItemConsumeEvent) {
            if (event.isCancelled) {
                return
            }

            val nekoStack: NekoStack = itemStack.toNekoStack
            val food: FoodProperties = nekoStack.components.get(ItemComponentTypes.FOOD) ?: return
            val skills: List<Skill> = food.skills.map { SkillRegistry.INSTANCES[it] }
            skills.forEach { mechanicWorldInteraction.addMechanic(ImmutableSkillContext(CasterAdapter.adapt(player).toComposite(), TargetAdapter.adapt(player), nekoStack)) }
        }
    }

    companion object Type : ItemBehaviorType<Food> {
        override fun create(): Food {
            return Default
        }
    }
}


