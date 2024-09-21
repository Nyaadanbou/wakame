@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.enchantment

import io.papermc.paper.plugin.bootstrap.BootstrapContext
import io.papermc.paper.plugin.lifecycle.event.registrar.ReloadableRegistrarEvent
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.TypedKey
import io.papermc.paper.registry.data.EnchantmentRegistryEntry
import io.papermc.paper.registry.event.RegistryEvents
import io.papermc.paper.registry.event.RegistryFreezeEvent
import io.papermc.paper.registry.keys.tags.EnchantmentTagKeys
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys
import io.papermc.paper.tag.PostFlattenTagRegistrar
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.EquipmentSlotGroup

class WakameEnchantments(
    private val context: BootstrapContext,
) {
    companion object {
        val CUSTOM_ENCHANT: TypedKey<Enchantment> = TypedKey.create(RegistryKey.ENCHANTMENT, Key.key("test", "enchant"))
    }

    fun register() {
        val manager = context.lifecycleManager
        manager.registerEventHandler(RegistryEvents.ENCHANTMENT.freeze()) { event: RegistryFreezeEvent<Enchantment, EnchantmentRegistryEntry.Builder> -> this.registerEnchantment(event) }

        removeUnsupportedVanillaEnchantment()
    }

    private fun registerEnchantment(event: RegistryFreezeEvent<Enchantment, EnchantmentRegistryEntry.Builder>) {
        event.registry().register(CUSTOM_ENCHANT) { builder: EnchantmentRegistryEntry.Builder ->
            builder
                .description(Component.text("CUSTOM", NamedTextColor.BLUE))
                .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.SWORDS))
                .weight(100)
                .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(1, 1))
                .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(1, 1))
                .anvilCost(1)
                .maxLevel(10)
                .activeSlots(EquipmentSlotGroup.ANY)
        }
    }

    private fun removeUnsupportedVanillaEnchantment() {
        // Remove unsupported vanilla enchantments here
        context.lifecycleManager.registerEventHandler(LifecycleEvents.TAGS.postFlatten(RegistryKey.ENCHANTMENT)) { event: ReloadableRegistrarEvent<PostFlattenTagRegistrar<Enchantment>?> ->
            val registrar = event.registrar()
            registrar.setTag(
                EnchantmentTagKeys.IN_ENCHANTING_TABLE,
                setOf(CUSTOM_ENCHANT)
            )
        }
    }
}