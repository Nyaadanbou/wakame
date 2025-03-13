package cc.mewcraft.wakame.item.behaviors

import cc.mewcraft.wakame.ability.context.abilityInput
import cc.mewcraft.wakame.ecs.bridge.koishify
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.behavior.ItemBehavior
import cc.mewcraft.wakame.item.behavior.ItemBehaviorType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
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
                val input = abilityInput(player.koishify(), player.koishify())
                ability.cast(input)
            }
        }
    }

    companion object Type : ItemBehaviorType<Food> {
        override fun create(): Food = Default
    }
}


