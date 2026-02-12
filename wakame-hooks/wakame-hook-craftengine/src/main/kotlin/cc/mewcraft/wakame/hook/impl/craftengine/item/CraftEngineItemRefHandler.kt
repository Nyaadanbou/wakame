package cc.mewcraft.wakame.hook.impl.craftengine.item

import cc.mewcraft.wakame.hook.impl.craftengine.CKey
import cc.mewcraft.wakame.hook.impl.craftengine.toCraftEngine
import cc.mewcraft.wakame.item.ItemRefHandler
import cc.mewcraft.wakame.util.KoishKey
import io.papermc.paper.datacomponent.DataComponentTypes
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

    override fun accepts(id: KoishKey): Boolean {
        return getInternalType(id) != null
    }

    override fun getId(stack: ItemStack): KoishKey? {
        return convertKeyToRef(CraftEngineItems.getCustomItemId(stack))
    }

    override fun getName(id: KoishKey): Component? {
        // CraftEngine 无法直接从 ID 获取得到物品的名字
        // 我们只能先生成一个 ItemStack 然后再从中获取名字
        val item = getInternalType(id)?.buildItemStack(1) ?: return null
        val data = item.getData(DataComponentTypes.ITEM_NAME)
        return data
    }

    override fun getInternalType(id: KoishKey): BukkitCustomItem? {
        return convertRefToKey(id)?.let(CraftEngineItems::byId) as? BukkitCustomItem
    }

    override fun createItemStack(id: KoishKey, amount: Int, player: Player?): ItemStack? {
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