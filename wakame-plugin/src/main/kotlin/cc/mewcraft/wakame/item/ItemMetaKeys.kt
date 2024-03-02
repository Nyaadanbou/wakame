package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.NekoNamespaces
import net.kyori.adventure.key.Key

object ItemMetaKeys {
    private fun createKey(value: String): Key {
        return Key.key(NekoNamespaces.ITEM_META, value)
    }

    val DISPLAY_LORE = createKey("lore")
    val DISPLAY_NAME = createKey("name")
    val DURABILITY = createKey("durability")
    val ELEMENT = createKey("element")
    val KIZAMI = createKey("kizami")
    val LEVEL = createKey("level")
    val RARITY = createKey("rarity")
    val SKIN = createKey("skin")
    val SKIN_OWNER = createKey("skin_owner")
}