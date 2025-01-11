package cc.mewcraft.wakame.enchantment.wrapper

import cc.mewcraft.wakame.core.RegistryEntry
import cc.mewcraft.wakame.core.registries.KoishRegistries
import cc.mewcraft.wakame.element.ElementType
import cc.mewcraft.wakame.enchantment.CustomEnchantment
import cc.mewcraft.wakame.enchantment.Enchantments
import net.kyori.adventure.key.Key
import org.bukkit.enchantments.Enchantment

internal abstract class ElementEnchantment(
    identity: Identity,
) : CustomEnchantment {
    final override val handle: Enchantment = Enchantments.getBy(identity.registryId)
    final override val key: Key = handle.key

    protected val element: RegistryEntry<ElementType> = KoishRegistries.ELEMENT.createEntry(identity.elementId)

    internal data class Identity(
        val elementId: String,
        val enchantId: String,
    ) {
        val registryId: String = elementId + "_" + enchantId
    }
}