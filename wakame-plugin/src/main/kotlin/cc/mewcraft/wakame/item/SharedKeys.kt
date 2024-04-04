package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.NekoNamespaces
import cc.mewcraft.wakame.util.Key
import net.kyori.adventure.key.Key

/**
 * Curse 的资源路径。
 */
object CurseKeys {
    val ENTITY_KILLS: Key = Key(NekoNamespaces.CURSE, "entity_kills")
    val PEAK_DAMAGE: Key = Key(NekoNamespaces.CURSE, "peak_damage")
}

/**
 * ItemMeta 的资源路径。
 *
 * 这些资源路径被广泛用于配置序列化，NBT序列化，以及任何需要唯一识别 ItemMeta 的地方。
 */
object ItemMetaKeys {
    val DISPLAY_LORE: Key = createKey("lore")
    val DISPLAY_NAME: Key = createKey("name")
    val DURABILITY: Key = createKey("durability")
    val ELEMENT: Key = createKey("element")
    val KIZAMI: Key = createKey("kizami")
    val LEVEL: Key = createKey("level")
    val RARITY: Key = createKey("rarity")
    val SKIN: Key = createKey("skin")
    val SKIN_OWNER: Key = createKey("skin_owner")

    private fun createKey(value: String): Key {
        return Key(NekoNamespaces.ITEM_META, value)
    }
}