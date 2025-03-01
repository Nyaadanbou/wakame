package cc.mewcraft.wakame.item.behaviors

import cc.mewcraft.wakame.ability.Ability
import cc.mewcraft.wakame.ability.character.CasterAdapter
import cc.mewcraft.wakame.ability.character.TargetAdapter
import cc.mewcraft.wakame.ability.context.abilityInput
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.VanillaItemSlot
import cc.mewcraft.wakame.item.behavior.ItemBehavior
import cc.mewcraft.wakame.item.behavior.ItemBehaviorType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.components.FoodProperties
import cc.mewcraft.wakame.item.projectNeko
import cc.mewcraft.wakame.registry2.KoishRegistries
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.inventory.ItemStack

interface Food : ItemBehavior {
    private object Default : Food {
        override fun handleConsume(player: Player, itemStack: ItemStack, koishStack: NekoStack, event: PlayerItemConsumeEvent) {
            if (event.isCancelled) return
            val foodProperties = koishStack.components.get(ItemComponentTypes.FOOD) ?: return
            val abilities = foodProperties.abilities.map { KoishRegistries.ABILITY.getOrThrow(it) }
            for (ability in abilities) {
                val input = abilityInput(TargetAdapter.adapt(player)) {
                    castBy(CasterAdapter.adapt(player))
                    holdBy(VanillaItemSlot.fromEquipmentSlot(event.hand)!! to koishStack)
                }
                ability.recordBy(input)
            }
        }
    }

    companion object Type : ItemBehaviorType<Food> {
        override fun create(): Food = Default
    }
}


