package cc.mewcraft.wakame.enchantment.wrapper

import cc.mewcraft.commons.provider.Provider
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.enchantment.CustomEnchantment
import cc.mewcraft.wakame.enchantment.Enchantments
import cc.mewcraft.wakame.registry.ElementRegistry
import net.kyori.adventure.key.Key
import org.bukkit.enchantments.Enchantment

internal abstract class ElementEnchantment(
    enchantmentId: ElementEnchantmentId,
) : CustomEnchantment {
    protected val elementProvider: Provider<Element> = ElementRegistry.getProvider(enchantmentId.elementId)
    final override val handle: Enchantment = Enchantments.getBy(enchantmentId.id)
    final override val key: Key = handle.key
}

internal data class ElementEnchantmentId(
    val elementId: String,
    val enchantId: String,
) {
    val id: String = elementId + "_" + enchantId
}
