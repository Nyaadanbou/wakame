package cc.mewcraft.wakame.ecs.system

import cc.mewcraft.wakame.attribute.AttributeProvider
import cc.mewcraft.wakame.ecs.Families
import cc.mewcraft.wakame.ecs.component.AttributeMapComponent
import cc.mewcraft.wakame.ecs.component.Mana
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.FamilyOnAdd
import com.github.quillraven.fleks.IteratingSystem

class ManaSystem : IteratingSystem(
    family = Families.BUKKIT_PLAYER
), FamilyOnAdd {
    override fun onTickEntity(entity: Entity) {
        val mana = entity[Mana]
        mana += 1
    }

    override fun onAddEntity(entity: Entity) {
        val attributeMap = entity[AttributeMapComponent]()
        val maxManaAttribute = AttributeProvider.instance().get("max_mana") ?: return
        entity.configure { it += Mana(attributeMap.getValue(maxManaAttribute).toInt()) }
    }
}