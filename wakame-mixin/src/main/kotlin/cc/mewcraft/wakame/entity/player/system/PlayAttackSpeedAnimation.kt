package cc.mewcraft.wakame.entity.player.system

import cc.mewcraft.wakame.PluginHolder
import cc.mewcraft.wakame.ecs.bridge.EWorld
import cc.mewcraft.wakame.ecs.component.BukkitObject
import cc.mewcraft.wakame.ecs.component.BukkitPlayer
import cc.mewcraft.wakame.entity.player.AttackSpeed
import cc.mewcraft.wakame.item.ItemSlotChanges
import cc.mewcraft.wakame.item.getProperty
import cc.mewcraft.wakame.item.property.ItemPropertyTypes
import cc.mewcraft.wakame.registry.entry.RegistryEntry
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import org.bukkit.NamespacedKey
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeInstance
import org.bukkit.attribute.AttributeModifier

/**
 * 将 [AttackSpeed] 同步到 Minecraft 的 `minecraft:attack_speed` 属性上.
 * 这样可以让玩家在以第一人称视角挥动武器时看到与 [AttackSpeed] 相匹配的动画效果.
 */
object PlayAttackSpeedAnimation : IteratingSystem(
    family = EWorld.family { all(BukkitObject, BukkitPlayer, ItemSlotChanges) }
) {

    private val MODIFIER_ID = NamespacedKey(PluginHolder.instance, "animation")

    override fun onTickEntity(entity: Entity) {
        val player = entity[BukkitPlayer]
        val slotChanges = entity[ItemSlotChanges]
        slotChanges.forEachChangingEntry { slot, curr, prev ->
            val attributeinstance = player.unwrap().getAttribute(Attribute.ATTACK_SPEED)
            if (prev != null &&
                ItemSlotChanges.testSlot(slot, prev)
            ) {
                val attackspeed = prev.getProperty(ItemPropertyTypes.ATTACK_SPEED)
                if (attackspeed != null) {
                    attributeinstance?.removeModifier(MODIFIER_ID)
                }
            }
            if (curr != null &&
                ItemSlotChanges.testSlot(slot, curr)
            ) {
                val attackspeed = curr.getProperty(ItemPropertyTypes.ATTACK_SPEED)
                if (attackspeed != null) {
                    attributeinstance?.addTransientModifier(computeAttackSpeedModifier(attributeinstance, attackspeed))
                }
            }
        }
    }

    private fun computeAttackSpeedModifier(attributeinstance: AttributeInstance, attackspeed: RegistryEntry<AttackSpeed>): AttributeModifier {
        val c = attributeinstance.value
        val m = (20.0 / attackspeed.unwrap().cooldown / c) - 1
        return AttributeModifier(MODIFIER_ID, m, AttributeModifier.Operation.MULTIPLY_SCALAR_1)
    }
}