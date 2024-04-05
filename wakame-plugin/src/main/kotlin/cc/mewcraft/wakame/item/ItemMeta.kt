package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.adventure.Keyed
import net.kyori.adventure.key.Key

interface ItemMeta : Keyed {
    /**
     * The key of this item meta.
     */
    override val key: Key
}