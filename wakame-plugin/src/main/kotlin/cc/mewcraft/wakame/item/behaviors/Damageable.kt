package cc.mewcraft.wakame.item.behaviors

import cc.mewcraft.wakame.item.behavior.ItemBehavior
import cc.mewcraft.wakame.item.behavior.ItemBehaviorFactory
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.template.ItemTemplateTypes
import cc.mewcraft.wakame.item.toNekoStack
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerItemDamageEvent
import org.bukkit.inventory.ItemStack

/**
 * 物品受损的逻辑.
 */
interface Damageable : ItemBehavior {
    object Default : Damageable {
        override fun handleDamage(player: Player, itemStack: ItemStack, event: PlayerItemDamageEvent) {
            if (event.isCancelled) {
                return
            }

            val nekoStack = itemStack.toNekoStack
            val damageable = nekoStack.components.get(ItemComponentTypes.DAMAGEABLE) ?: return

            // 获取当前损耗
            val damage = damageable.damage
            // 获取最大损耗
            val maxDamage = damageable.maxDamage
            // 获取达到最大损耗时物品是否消失
            val disappearWhenBroken = nekoStack.templates.get(ItemTemplateTypes.DAMAGEABLE).disappearWhenBroken

            if (damage >= maxDamage) {
                // 已达到最大损耗

                if (!disappearWhenBroken) {
                    // 物品设置了达到最大损耗时不消失

                    // 回滚损耗让物品不坏掉
                    nekoStack.components.set(ItemComponentTypes.DAMAGE, damage - 1)
                }
            }
        }
    }

    companion object : ItemBehaviorFactory<Damageable> {
        override fun create(): Damageable {
            return Default
        }
    }
}
