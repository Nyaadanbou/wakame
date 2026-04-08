package cc.mewcraft.wakame.bridge

import cc.mewcraft.wakame.bridge.item.ServerItemRef
import net.kyori.adventure.key.Key
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import org.bukkit.entity.Player

interface KoishItemBridge {
    companion object Impl : KoishItemBridge {
        private var implementation: KoishItemBridge = object : KoishItemBridge {
            override fun getTypeId(stack: MojangStack): Key = throw NotImplementedError()
            override fun getItemRef(key: Key): ServerItemRef = throw NotImplementedError()
            override fun getClientItemName(stack: MojangStack): Component = throw NotImplementedError()
            override fun getClientItemModel(stack: MojangStack): Identifier = throw NotImplementedError()
            override fun isKoish(stack: MojangStack): Boolean = throw NotImplementedError()
            override fun isExactKoish(stack: MojangStack): Boolean = throw NotImplementedError()
            override fun isProxyKoish(stack: MojangStack): Boolean = throw NotImplementedError()
            override fun isPrimaryEnchantment(stack: MojangStack, enchantment: MojangEnchantment): Boolean = throw NotImplementedError()
            override fun isSupportedEnchantment(stack: MojangStack, enchantment: MojangEnchantment): Boolean = throw NotImplementedError()
            override fun craftingRemainder(stack: MojangStack): MojangStack = throw NotImplementedError()
            override fun transformToClientStack(stack: MojangStack, player: Player?) = throw NotImplementedError()
            override fun createShowItemComponent(component: Component): Component = throw NotImplementedError()
            override fun onlyCompareTypeIdForRecipeBook(stack: MojangStack): Boolean = throw NotImplementedError()
            override fun onlyCompareTypeIdForRecipeBook(stack: MojangStack, bool: Boolean): Unit = throw NotImplementedError()
        }

        fun setImplementation(impl: KoishItemBridge) {
            implementation = impl
        }

        override fun getTypeId(stack: MojangStack): Key =
            implementation.getTypeId(stack)

        override fun getItemRef(key: Key): ServerItemRef? =
            implementation.getItemRef(key)

        override fun getClientItemName(stack: MojangStack): Component? =
            implementation.getClientItemName(stack)

        override fun getClientItemModel(stack: MojangStack): Identifier? =
            implementation.getClientItemModel(stack)

        override fun isKoish(stack: MojangStack): Boolean =
            implementation.isKoish(stack)

        override fun isExactKoish(stack: MojangStack): Boolean =
            implementation.isExactKoish(stack)

        override fun isProxyKoish(stack: MojangStack): Boolean =
            implementation.isProxyKoish(stack)

        override fun isPrimaryEnchantment(stack: MojangStack, enchantment: MojangEnchantment): Boolean =
            implementation.isPrimaryEnchantment(stack, enchantment)

        override fun isSupportedEnchantment(stack: MojangStack, enchantment: MojangEnchantment): Boolean =
            implementation.isSupportedEnchantment(stack, enchantment)

        override fun craftingRemainder(stack: MojangStack): MojangStack? =
            implementation.craftingRemainder(stack)

        override fun transformToClientStack(stack: MojangStack, player: Player?) =
            implementation.transformToClientStack(stack, player)

        override fun createShowItemComponent(component: Component): Component =
            implementation.createShowItemComponent(component)

        override fun onlyCompareTypeIdForRecipeBook(stack: MojangStack): Boolean =
            implementation.onlyCompareTypeIdForRecipeBook(stack)

        override fun onlyCompareTypeIdForRecipeBook(stack: MojangStack, bool: Boolean) {
            implementation.onlyCompareTypeIdForRecipeBook(stack, bool)
        }
    }

    fun getTypeId(stack: MojangStack): Key //KoishStackData.getTypeId(stack);
    fun getItemRef(key: Key): ServerItemRef?
    fun getClientItemName(stack: MojangStack): Component? // HotfixItemName.INSTANCE.getItemName(itemStack)
    fun getClientItemModel(stack: MojangStack): Identifier? // HotfixItemModel.INSTANCE.transform(copy)
    fun isKoish(stack: MojangStack): Boolean
    fun isExactKoish(stack: MojangStack): Boolean
    fun isProxyKoish(stack: MojangStack): Boolean
    fun isPrimaryEnchantment(stack: MojangStack, enchantment: MojangEnchantment): Boolean
    fun isSupportedEnchantment(stack: MojangStack, enchantment: MojangEnchantment): Boolean
    fun craftingRemainder(stack: MojangStack): MojangStack?
    fun transformToClientStack(stack: MojangStack, player: Player?) //ItemStackRenderer.getInstance().render(copy, ServerboundPacketSession.INSTANCE.player());
    fun createShowItemComponent(component: Component): Component
    fun onlyCompareTypeIdForRecipeBook(stack: MojangStack): Boolean
    fun onlyCompareTypeIdForRecipeBook(stack: MojangStack, bool: Boolean)
}