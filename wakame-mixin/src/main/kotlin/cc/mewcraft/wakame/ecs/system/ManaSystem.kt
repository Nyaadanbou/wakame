package cc.mewcraft.wakame.ecs.system

import cc.mewcraft.wakame.ecs.component.BukkitObject
import cc.mewcraft.wakame.ecs.component.BukkitPlayer
import cc.mewcraft.wakame.ecs.component.Mana
import cc.mewcraft.wakame.entity.attribute.AttributeMap
import cc.mewcraft.wakame.entity.attribute.Attributes
import cc.mewcraft.wakame.entity.player.component.InventoryListenable
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.FamilyOnAdd
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World

class ManaSystem : IteratingSystem(
    family = World.family { all(BukkitObject, BukkitPlayer, InventoryListenable) }
), FamilyOnAdd {
    override fun onTickEntity(entity: Entity) {
        entity[Mana] += 1
    }

    override fun onAddEntity(entity: Entity) {
        val attributeContainer = entity[AttributeMap]
        entity.configure {
            it += Mana(attributeContainer.getValue(Attributes.MAX_MANA).toInt())
        }
    }
}