package cc.mewcraft.wakame.ecs.system

import cc.mewcraft.wakame.ecs.bridge.EWorld
import cc.mewcraft.wakame.ecs.component.BukkitObject
import cc.mewcraft.wakame.ecs.component.BukkitPlayer
import cc.mewcraft.wakame.ecs.component.Mana
import cc.mewcraft.wakame.entity.attribute.AttributeMap
import cc.mewcraft.wakame.entity.attribute.Attributes
import cc.mewcraft.wakame.entity.player.component.InventoryListenable
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.Fixed
import com.github.quillraven.fleks.IteratingSystem

object UpdateMaxMana : IteratingSystem(
    family = EWorld.family { all(BukkitObject, BukkitPlayer, Mana, InventoryListenable) },
    interval = Fixed(20f)
) {
    override fun onTickEntity(entity: Entity) {
        val attributeContainer = entity[AttributeMap]
        val maxMana = attributeContainer.getValue(Attributes.MAX_MANA).toInt()
        val mana = entity[Mana]
        if (mana.maximum != maxMana) {
            mana.maximum = maxMana
        }
        mana.ensureMana()
    }
}