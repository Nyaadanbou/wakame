package cc.mewcraft.wakame.item.behaviors

import cc.mewcraft.wakame.entity.EntityKeyLookup
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.behavior.ItemBehavior
import cc.mewcraft.wakame.item.behavior.ItemBehaviorType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.components.ItemTracks
import cc.mewcraft.wakame.item.toNekoStack
import net.kyori.adventure.key.Key
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface Trackable : ItemBehavior {
    private object Default : Trackable, KoinComponent {
        private val entityKeyLookup: EntityKeyLookup by inject()

        override fun handleAttackEntity(player: Player, itemStack: ItemStack, attacked: Entity, event: EntityDamageByEntityEvent) {
            if (attacked !is LivingEntity) {
                return
            }

            val key: Key = entityKeyLookup.get(attacked)
            val stack: NekoStack = itemStack.toNekoStack
            val tracks: ItemTracks = stack.components.get(ItemComponentTypes.TRACKS) ?: return

            //
        }
    }

    companion object Type : ItemBehaviorType<Trackable> {
        override fun create(): Trackable {
            return Default
        }
    }
}