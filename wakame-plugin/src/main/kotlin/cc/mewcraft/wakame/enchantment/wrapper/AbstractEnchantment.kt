package cc.mewcraft.wakame.enchantment.wrapper

import cc.mewcraft.wakame.enchantment.CustomEnchantment
import net.kyori.adventure.key.Key
import org.bukkit.enchantments.Enchantment

internal abstract class AbstractEnchantment(
    override val handle: Enchantment,
) : CustomEnchantment {
    override val key: Key get() = handle.key
}