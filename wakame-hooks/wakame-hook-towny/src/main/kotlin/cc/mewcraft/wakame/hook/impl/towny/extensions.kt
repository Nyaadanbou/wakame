package cc.mewcraft.wakame.hook.impl.towny

import com.palmergames.bukkit.towny.`object`.TownyObject
import com.palmergames.bukkit.towny.`object`.metadata.CustomDataField

inline fun <reified T : CustomDataField<*>> TownyObject.getMeta(key: String): T? {
    return this.getMetadata(key, T::class.java)
}

inline fun <reified T : CustomDataField<*>> TownyObject.hasMeta(key: String): Boolean {
    return this.hasMeta(key, T::class.java)
}