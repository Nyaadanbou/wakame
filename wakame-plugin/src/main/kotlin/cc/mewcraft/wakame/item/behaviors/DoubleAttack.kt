package cc.mewcraft.wakame.item.behaviors

import cc.mewcraft.wakame.damage.hurt
import cc.mewcraft.wakame.event.bukkit.NekoPostprocessDamageEvent
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.behavior.ItemBehavior
import cc.mewcraft.wakame.item.behavior.ItemBehaviorType
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

// FIXME #363: 临时代码, 仅用于测试
interface DoubleAttack : ItemBehavior {
    private object Default : DoubleAttack {
        override fun handleAttackEntity(player: Player, itemStack: ItemStack, koishStack: NekoStack, damagee: Entity, event: NekoPostprocessDamageEvent) {
            if (damagee is LivingEntity) {
                // 再次造成一次相同的伤害
                val damageMetadata = event.damageMetadata
                damagee.hurt(damageMetadata = damageMetadata, source = player)
            }
        }
    }

    companion object Type : ItemBehaviorType<DoubleAttack> {
        override fun create(): DoubleAttack = Default
    }
}