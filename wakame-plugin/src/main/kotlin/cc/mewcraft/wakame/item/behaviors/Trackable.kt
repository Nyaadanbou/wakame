package cc.mewcraft.wakame.item.behaviors

import cc.mewcraft.wakame.entity.EntityKeyLookup
import cc.mewcraft.wakame.item.behavior.ItemBehavior
import cc.mewcraft.wakame.item.behavior.ItemBehaviorFactory
import cc.mewcraft.wakame.item.toNekoStack
import cc.mewcraft.wakame.registry.ElementRegistry
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * 物品跟踪信息的逻辑.
 */
interface Trackable : ItemBehavior {
    private object Default : Trackable, KoinComponent {
        private val lookup: EntityKeyLookup by inject()

        override fun handleAttackEntity(player: Player, itemStack: ItemStack, attacked: Entity, event: EntityDamageByEntityEvent) {
            // TODO: 这个只是一个 POC，要判断一个生物是否被一个物品击杀需要考虑很多情况（比如法杖的间接伤害统计）
            if (attacked !is LivingEntity) return
            val key = lookup.get(attacked)
            val nekoStack = itemStack.toNekoStack

            val statistics = nekoStack.statistics
            val finalDamage = event.finalDamage
            if (attacked.health - finalDamage <= 0.0) {
                statistics.ENTITY_KILLS[key] += 1
            }
            statistics.PEAK_DAMAGE[ElementRegistry.DEFAULT] = maxOf(statistics.PEAK_DAMAGE[ElementRegistry.DEFAULT], finalDamage.toInt())
        }
    }

    companion object Factory : ItemBehaviorFactory<Trackable> {
        override fun create(): Trackable {
            return Default
        }
    }
}