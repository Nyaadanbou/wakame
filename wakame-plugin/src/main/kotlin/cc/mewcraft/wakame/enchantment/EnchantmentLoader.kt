package cc.mewcraft.wakame.enchantment

import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.item.ItemSlotRegistry
import cc.mewcraft.wakame.item.VanillaItemSlot

object EnchantmentLoader : Initializable {
    override fun onPreWorld() {
        WakameEnchantments.all()
            .flatMap { it.equipmentSlotGroups }
            .flatMap { VanillaItemSlot.fromEquipmentSlotGroup(it) }
            .distinct()
            .forEach { ItemSlotRegistry.register(it) }
    }
}