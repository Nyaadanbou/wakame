package cc.mewcraft.wakame.item.behaviors

import cc.mewcraft.wakame.item.behavior.ItemBehavior
import cc.mewcraft.wakame.item.behavior.ItemBehaviorType
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerItemDamageEvent
import org.bukkit.inventory.ItemStack

interface MaxDamage : ItemBehavior {
    private object Default : MaxDamage {
        override fun handleDamage(player: Player, itemStack: ItemStack, event: PlayerItemDamageEvent) {
            // TODO 应该由那些需要操作 damage/max_damage 的代码来操作物品的耐久度
            // if (event.isCancelled) {
            //     return
            // }
            //
            // val nekoStack = itemStack.toNekoStack
            // val damageableComponent = nekoStack.components.get(ItemComponentTypes.DAMAGEABLE) ?: return
            // val damageableTemplate = nekoStack.templates.get(ItemTemplateTypes.DAMAGEABLE) ?: return
            //
            // // 如果有损耗, 设置损耗为固定值 1
            // event.damage = 1
            //
            // val currentDamage = damageableComponent.damage
            // val maximumDamage = damageableComponent.maxDamage
            // val disappearWhenBroken = damageableTemplate.disappearWhenBroken
            // if (currentDamage >= maximumDamage && !disappearWhenBroken) {
            //     // 物品将会在下一 tick 消失, 但是萌芽设置了不消失, 于是取消事件
            //     event.isCancelled = true
            // }
        }
    }

    companion object Type : ItemBehaviorType<MaxDamage> {
        override fun create(): MaxDamage {
            return Default
        }
    }
}
