package cc.mewcraft.wakame.item.schema.behavior

import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.entity.EntityKeyLookup
import cc.mewcraft.wakame.item.binary.NekoStackFactory
import cc.mewcraft.wakame.item.schema.NekoItem
import cc.mewcraft.wakame.registry.ElementRegistry
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface Statistical : ItemBehavior {

    companion object Factory : ItemBehaviorFactory<Statistical> {
        override fun create(item: NekoItem, behaviorConfig: ConfigProvider): Statistical {
            return Default()
        }
    }

    private class Default : Statistical, KoinComponent {
        private val lookup: EntityKeyLookup by inject()

        override fun handleAttackEntity(player: Player, itemStack: ItemStack, attacked: Entity, event: EntityDamageByEntityEvent) {
            // TODO: 这个只是一个 POC，要判断一个生物是否被一个物品击杀需要考虑很多情况（比如法杖的间接伤害统计）
            if (attacked !is LivingEntity) return
            val key = lookup.getKey(attacked) ?: return
            val nekoStack = NekoStackFactory.wrap(itemStack)

            val statistics = nekoStack.statistics
            val finalDamage = event.finalDamage
            if (attacked.health - finalDamage <= 0.0) {
                statistics.ENTITY_KILLS[key] += 1
            }
            statistics.PEAK_DAMAGE[ElementRegistry.DEFAULT] = maxOf(statistics.PEAK_DAMAGE[ElementRegistry.DEFAULT], finalDamage.toInt())
        }
    }
}