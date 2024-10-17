package cc.mewcraft.wakame.enchantment.wrapper

import cc.mewcraft.commons.provider.Provider
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.enchantment.CustomEnchantment
import cc.mewcraft.wakame.enchantment.Enchantments
import cc.mewcraft.wakame.registry.ElementRegistry
import net.kyori.adventure.key.Key
import org.bukkit.enchantments.Enchantment

internal abstract class ElementEnchantment(
    identity: Identity,
) : CustomEnchantment {
    final override val handle: Enchantment = Enchantments.getBy(identity.registryId)
    final override val key: Key = handle.key

    protected val element: Provider<Element> = ElementRegistry.getProvider(identity.elementId)

    internal data class Identity(
        val elementId: String,
        val enchantId: String,
    ) {
        val registryId: String = elementId + "_" + enchantId
    }
}