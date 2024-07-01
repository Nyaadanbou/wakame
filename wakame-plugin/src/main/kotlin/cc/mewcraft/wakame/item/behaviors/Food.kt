package cc.mewcraft.wakame.item.behaviors

import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.behavior.ItemBehavior
import cc.mewcraft.wakame.item.behavior.ItemBehaviorType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.components.FoodProperties
import cc.mewcraft.wakame.item.toNekoStack
import net.kyori.adventure.key.Key
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
            val skills: List<Key> = food.skills

            // 释放食物组件上记录的技能
        }
    }

    companion object Type : ItemBehaviorType<Food> {
        override fun create(): Food {
            return Default
        }
    }
}


