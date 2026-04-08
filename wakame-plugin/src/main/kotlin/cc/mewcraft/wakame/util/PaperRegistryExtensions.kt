package cc.mewcraft.wakame.util

import io.papermc.paper.registry.tag.Tag
import io.papermc.paper.registry.tag.TagKey
import org.bukkit.Keyed
import org.bukkit.Registry

fun <T : Keyed> Registry<T>.getTagOrNull(tagKey: TagKey<T>): Tag<T>? {
    return runCatching { getTag(tagKey) }.getOrNull()
}