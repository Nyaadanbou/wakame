@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.enchantment

import cc.mewcraft.wakame.adventure.key.Keyed
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.TypedKey
import io.papermc.paper.registry.data.EnchantmentRegistryEntry
import io.papermc.paper.registry.event.RegistryFreezeEvent
import io.papermc.paper.registry.tag.TagKey
import net.kyori.adventure.key.Key
import org.bukkit.enchantments.Enchantment

class CustomEnchantment(
    /**
     * 用于注册附魔的 [TypedKey].
     */
    val enchantmentKey: TypedKey<Enchantment>,
    /**
     * 该附魔的标签.
     */
    val tags: Collection<TagKey<Enchantment>>,
    /**
     * 用于构建附魔的 [EnchantmentRegistryEntry.Builder].
     */
    private val builder: EnchantmentRegistryEntry.Builder.(RegistryFreezeEvent<Enchantment, EnchantmentRegistryEntry.Builder>) -> Unit,
) : Keyed {
    override val key: Key
        get() = enchantmentKey.key()

    val enchantment: Enchantment
        get() {
            return RegistryAccess.registryAccess()
                .getRegistry(RegistryKey.ENCHANTMENT)
                .getOrThrow(TypedKey.create(RegistryKey.ENCHANTMENT, key))
        }

    fun register(event: RegistryFreezeEvent<Enchantment, EnchantmentRegistryEntry.Builder>) {
        event.registry().register(enchantmentKey) {
            builder(it, event)
        }
    }
}