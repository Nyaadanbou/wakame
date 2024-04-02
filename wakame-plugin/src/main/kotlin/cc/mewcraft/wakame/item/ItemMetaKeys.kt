package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.NekoNamespaces
import cc.mewcraft.wakame.util.Key
import net.kyori.adventure.key.Key

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