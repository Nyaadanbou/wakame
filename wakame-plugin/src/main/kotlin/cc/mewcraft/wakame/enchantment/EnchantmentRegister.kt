@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.enchantment

import io.papermc.paper.plugin.bootstrap.BootstrapContext
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.TypedKey
import io.papermc.paper.registry.event.RegistryEvents
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.enchantments.Enchantment

class EnchantmentRegister(
    private val context: BootstrapContext,
) {
    fun register() {
        val manager = context.lifecycleManager
        manager.registerEventHandler(RegistryEvents.ENCHANTMENT.freeze()) {
            for (enchantment in WakameEnchantments.ALL) {
                enchantment.getRegister().register(it)
            }
        }

        registerCustomEnchantmentTags()

        manager.registerEventHandler(
            RegistryEvents.ENCHANTMENT.entryAdd()
                .newHandler { event ->
                    event.builder()
                        .description(
                            Component.text()
                                .content("DO_NOT_USE")
                                .color(NamedTextColor.RED)
                                .append(Component.text("-"))
                                .append(Component.translatable(WakameEnchantmentsSupport.getTranslateKey(event.key())).color(NamedTextColor.GRAY))
                                .build()
                        )
                }
                .filter { !isAllowedEnchantment(it) }
        )
    }

    private fun registerCustomEnchantmentTags() {
        context.lifecycleManager.registerEventHandler(LifecycleEvents.TAGS.postFlatten(RegistryKey.ENCHANTMENT)) { event ->
            val registrar = event.registrar()
            WakameEnchantmentsSupport.ENCHANTMENT_EFFECT_TAGS.forEach {
                val enchantments = registrar.getTag(it)
                registrar.setTag(it, enchantments.filter(::isAllowedEnchantment))
            }
            // Clear enchantment banned tags
            for (customEnchantment in WakameEnchantments.ALL) {
                customEnchantment.tags.forEach { registrar.addToTag(it, listOf(customEnchantment.enchantmentKey)) }
            }
        }
    }


    private fun isAllowedEnchantment(enchantmentKey: TypedKey<Enchantment>): Boolean {
        if (WakameEnchantments.ALL.any { it.enchantmentKey == enchantmentKey }) {
            // 自定义附魔肯定是允许的
            return true
        }

        if (enchantmentKey in WakameEnchantmentsSupport.ALLOWED_ENCHANTMENTS) {
            // 在允许的附魔内
            return true
        }

        return false
    }
}