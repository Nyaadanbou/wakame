package cc.mewcraft.wakame.item.behaviors

import cc.mewcraft.wakame.Injector
import cc.mewcraft.wakame.entity.typeref.EntityRefLookup
import cc.mewcraft.wakame.event.bukkit.PostprocessDamageEvent
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.behavior.ItemBehavior
import cc.mewcraft.wakame.item.behavior.ItemBehaviorType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.components.tracks.TrackTypes
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

interface Trackable : ItemBehavior {
    private object Default : Trackable {
        private val entityRefLookup: EntityRefLookup by Injector.inject<EntityRefLookup>()

        override fun handleAttackEntity(player: Player, itemStack: ItemStack, koishStack: NekoStack, damagee: Entity, event: PostprocessDamageEvent) {
            if (damagee !is LivingEntity) {
                return
            }

            // 以下代码只是个 POC, 用作在游戏内测试 ItemTracks 有没有正常工作

            val entityKey = entityRefLookup.get(damagee)
            val components = koishStack.components
            val trackMap = components.get(ItemComponentTypes.TRACKS) ?: return

            if (damagee.isDead) {
                val newTracks = trackMap.modify(TrackTypes.ENTITY_KILLS) { it.grow(entityKey, 1) }
                components.set(ItemComponentTypes.TRACKS, newTracks)
            }
        }
    }

    companion object Type : ItemBehaviorType<Trackable> {
        override fun create(): Trackable = Default
    }
}