package cc.mewcraft.wakame.catalog.enchantment

import cc.mewcraft.wakame.item.ItemRef
import cc.mewcraft.wakame.util.KoishKey
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * 附魔所支持的单个物品的封装, 代表原版 [org.bukkit.inventory.ItemType] 或 Koish 物品.
 *
 * 只记录 ID, 不持有 [ItemStack]. 调用 [resolve] 按需生成.
 */
class EnchantmentItemResolver(
    /** 物品的唯一标识 */
    val id: KoishKey,
) {
    /** 展示名称 */
    val name: Component
        get() = ItemRef.create(id)?.name ?: Component.text(id.value())

    /**
     * 按需生成该物品类型的 [ItemStack].
     */
    fun resolve(amount: Int = 1, player: Player? = null): ItemStack? {
        return ItemRef.create(id)?.createItemStack(amount, player)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is EnchantmentItemResolver) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
