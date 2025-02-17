package cc.mewcraft.wakame.item.behaviors

import cc.mewcraft.wakame.Injector
import cc.mewcraft.wakame.event.bukkit.NekoEntityDamageEvent
import cc.mewcraft.wakame.item.behavior.ItemBehavior
import cc.mewcraft.wakame.item.behavior.ItemBehaviorType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.components.tracks.TrackTypes
import cc.mewcraft.wakame.item.projectNeko
import cc.mewcraft.wakame.world.entity.EntityKeyLookup
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

interface Trackable : ItemBehavior {
    private object Default : Trackable {
        private val entityKeyLookup: EntityKeyLookup by Injector.inject<EntityKeyLookup>()

        override fun handleAttackEntity(player: Player, itemStack: ItemStack, damagee: Entity, event: NekoEntityDamageEvent) {
            if (damagee !is LivingEntity) {
                return
            }

            // 以下代码只是个 POC, 用作在游戏内测试 ItemTracks 有没有正常工作

            val entityKey = entityKeyLookup.get(damagee)
            val nekoStack = itemStack.projectNeko(false)
            val components = nekoStack.components
            val trackMap = components.get(ItemComponentTypes.TRACKS) ?: return

            if (damagee.isDead) {
                val newTracks = trackMap.modify(TrackTypes.ENTITY_KILLS) { it.grow(entityKey, 1) }
                components.set(ItemComponentTypes.TRACKS, newTracks)
            }
        }
    }

    companion object Type : ItemBehaviorType<Trackable> {
        override fun create(): Trackable {
            return Default
        }
    }
}