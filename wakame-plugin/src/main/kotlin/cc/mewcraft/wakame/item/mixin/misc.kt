package cc.mewcraft.wakame.item.mixin

import cc.mewcraft.wakame.bridge.KoishItemBridge
import cc.mewcraft.wakame.bridge.MojangEnchantment
import cc.mewcraft.wakame.bridge.MojangStack
import cc.mewcraft.wakame.bridge.item.ServerItemRef
import cc.mewcraft.wakame.item.*
import cc.mewcraft.wakame.item.display.ItemRenderers
import cc.mewcraft.wakame.item.display.ShowItemRenderer
import cc.mewcraft.wakame.item.property.ItemPropTypes
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import io.papermc.paper.adventure.PaperAdventure
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import net.kyori.adventure.key.Key
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.server.MinecraftServer
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player

@Init(InitStage.BOOTSTRAP)
internal object KoishItemBridgeBootstrap {
    @InitFun
    fun init() {
        KoishItemBridge.setImplementation(KoishItemBridgeImpl)
    }
}

// ------------
// 内部实现
// ------------

private object KoishItemBridgeImpl : KoishItemBridge {
    override fun getTypeId(stack: MojangStack): Key {
        return stack.typeId
    }

    override fun getItemRef(key: Key): ServerItemRef? {
        return ItemRef.create(key)
    }

    override fun getClientItemName(stack: MojangStack): Component? {
        return HotfixItemName.getItemName(stack)
    }

    override fun getClientItemModel(stack: MojangStack): Identifier? {
        return HotfixItemModel.getItemModel(stack)
    }

    override fun isKoish(stack: MojangStack): Boolean {
        return stack.isKoish
    }

    override fun isExactKoish(stack: MojangStack): Boolean {
        return stack.isExactKoish
    }

    override fun isProxyKoish(stack: MojangStack): Boolean {
        return stack.isProxyKoish
    }

    override fun isPrimaryEnchantment(stack: MojangStack, enchantment: MojangEnchantment): Boolean {
        val primaryEnchantments = stack.getProp(ItemPropTypes.PRIMARY_ENCHANTMENTS) ?: return false
        return primaryEnchantments.contains(minecraftEnchantmentToBukkit(enchantment))
    }

    override fun isSupportedEnchantment(stack: MojangStack, enchantment: MojangEnchantment): Boolean {
        val supportedEnchantments = stack.getProp(ItemPropTypes.SUPPORTED_ENCHANTMENTS) ?: return false
        return supportedEnchantments.contains(minecraftEnchantmentToBukkit(enchantment))
    }

    override fun craftingRemainder(stack: MojangStack): MojangStack? {
        return stack.getProp(ItemPropTypes.CRAFTING_REMAINDER)?.remainder(stack)
    }

    override fun transformToClientStack(stack: MojangStack, player: Player?) {
        return ItemRenderers.STANDARD.render(stack, player)
    }

    override fun createShowItemComponent(component: Component): Component {
        return PaperAdventure.asVanilla(ShowItemRenderer.render(PaperAdventure.asAdventure(component)))
    }

    override fun onlyCompareTypeIdForRecipeBook(stack: MojangStack): Boolean {
        return stack.onlyCompareIdInRecipeBook
    }

    override fun onlyCompareTypeIdForRecipeBook(stack: MojangStack, bool: Boolean) {
        stack.onlyCompareIdInRecipeBook = bool
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
