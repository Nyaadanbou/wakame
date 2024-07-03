package cc.mewcraft.wakame.item.behaviors

import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.behavior.ItemBehavior
import cc.mewcraft.wakame.item.behavior.ItemBehaviorType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.template.ItemTemplateTypes
import cc.mewcraft.wakame.item.toNekoStack
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerItemDamageEvent
import org.bukkit.inventory.ItemStack
import cc.mewcraft.wakame.item.components.Damageable as DamageableComponent

interface Damageable : ItemBehavior {
    private object Default : Damageable {
        override fun handleDamage(player: Player, itemStack: ItemStack, event: PlayerItemDamageEvent) {
            if (event.isCancelled) {
                return
            }

            val stack: NekoStack = itemStack.toNekoStack
            val damageable: DamageableComponent = stack.components.get(ItemComponentTypes.DAMAGEABLE) ?: return

            // 获取当前损耗
            val damage: Int = damageable.damage
            // 获取最大损耗
            val maxDamage: Int = damageable.maxDamage
            // 获取达到最大损耗时物品是否消失
            val disappearWhenBroken: Boolean = stack.templates.get(ItemTemplateTypes.DAMAGEABLE)?.disappearWhenBroken ?: return

            if (damage >= maxDamage && !disappearWhenBroken) {
                // 已达到最大损耗, 物品设置了达到最大损耗时不消失

                // 回滚损耗让物品不坏掉
                stack.components.set(ItemComponentTypes.DAMAGE, damage - event.damage)
            }
        }
    }

    companion object Type : ItemBehaviorType<Damageable> {
        override fun create(): Damageable {
            return Default
        }
    }
}
