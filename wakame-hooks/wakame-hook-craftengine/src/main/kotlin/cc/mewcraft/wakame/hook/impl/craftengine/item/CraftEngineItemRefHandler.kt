package cc.mewcraft.wakame.hook.impl.craftengine.item

import cc.mewcraft.wakame.hook.impl.craftengine.CKey
import cc.mewcraft.wakame.hook.impl.craftengine.toCraftEngine
import cc.mewcraft.wakame.item.ItemRefHandler
import cc.mewcraft.wakame.util.Identifier
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.momirealms.craftengine.bukkit.api.CraftEngineItems
import net.momirealms.craftengine.bukkit.item.BukkitCustomItem
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class CraftEngineItemRefHandler : ItemRefHandler<BukkitCustomItem> {

    companion object {
        const val NAMESPACE = "craftengine"
    }

    override val systemName: String = "CraftEngine"

    override fun accepts(id: Identifier): Boolean {
        return getInternalType(id) != null
    }

    override fun getId(stack: ItemStack): Identifier? {
        return convertKeyToRef(CraftEngineItems.getCustomItemId(stack))
    }

    override fun getName(id: Identifier): Component? {
        return getInternalType(id)?.id()?.toString()?.let(Component::text)
    }

    override fun getInternalType(id: Identifier): BukkitCustomItem? {
        return convertRefToKey(id)?.let(CraftEngineItems::byId) as? BukkitCustomItem
    }

    override fun createItemStack(id: Identifier, amount: Int, player: Player?): ItemStack? {
        return if (player != null) {
            getInternalType(id)?.buildItem(player.toCraftEngine())?.item?.asQuantity(amount)
        } else {
            getInternalType(id)?.buildItemStack()?.asQuantity(amount)
        }
    }

    /**
     * 将 [Key] 转换为 CraftEngine 可识别的 [CKey].
     *
     * 例如:
     * - `craftengine:koish/example` -> `koish:example`.
     * - `craftengine:koish/example/abc` -> `koish:example/abc`.
     *
     * @return 可作为参数传入 CraftEngine API 的 [CKey], 转换失败则返回 null
     */
    private fun convertRefToKey(key: Key?): CKey? {
        if (key == null) return null
        if (key.namespace() != NAMESPACE) return null
        val split = key.value().split("/", limit = 2)
        val namespace = split.getOrNull(0) ?: return null
        val value = split.getOrNull(1) ?: return null
        return CKey.of(namespace, value)
    }

    /**
     * 将 [CKey] 转换为 Koish 可识别的 [Key].
     *
     * 例如:
     * - `koish:example` -> `craftengine:koish/example`.
     * - `koish:example/abc` -> `craftengine:koish/example/abc`.
     */
    private fun convertKeyToRef(key: CKey?): Key? {
        if (key == null) return null
        return Key.key(NAMESPACE, "${key.namespace()}/${key.value()}")
    }
}