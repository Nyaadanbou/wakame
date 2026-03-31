package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.item.property.ItemPropTypes
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.mixin.support.KoishItemBridge
import cc.mewcraft.wakame.util.MojangEnchantment
import cc.mewcraft.wakame.util.MojangStack
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import net.kyori.adventure.key.Key
import net.minecraft.core.registries.Registries
import net.minecraft.server.MinecraftServer
import org.bukkit.enchantments.Enchantment

@Init(InitStage.BOOTSTRAP)
object KoishItemBridgeImpl : KoishItemBridge {

    @InitFun
    fun init() {
        KoishItemBridge.setImplementation(this)
    }

    override fun isKoish(stack: MojangStack): Boolean {
        return stack.isKoish
    }

    override fun isPrimaryEnchantment(stack: MojangStack, enchantment: MojangEnchantment): Boolean {
        val primaryEnchantments = stack.getProp(ItemPropTypes.PRIMARY_ENCHANTMENTS) ?: return false
        return primaryEnchantments.contains(minecraftEnchantmentToBukkit(enchantment))
    }

    override fun isSupportedEnchantment(stack: MojangStack, enchantment: MojangEnchantment): Boolean {
        val supportedEnchantments = stack.getProp(ItemPropTypes.SUPPORTED_ENCHANTMENTS) ?: return false
        return supportedEnchantments.contains(minecraftEnchantmentToBukkit(enchantment))
    }

    private fun minecraftEnchantmentToBukkit(enchantment: net.minecraft.world.item.enchantment.Enchantment): Enchantment? {
        val id = MinecraftServer.getServer().registryAccess()
            .lookupOrThrow(Registries.ENCHANTMENT)
            .getKey(enchantment)
        requireNotNull(id)
        return RegistryAccess.registryAccess()
            .getRegistry(RegistryKey.ENCHANTMENT)
            .get(Key.key(id.namespace, id.path))
    }
}