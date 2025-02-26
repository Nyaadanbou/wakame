package cc.mewcraft.wakame.item.extension

import cc.mewcraft.wakame.item.NekoItem
import cc.mewcraft.wakame.item.NekoStack
import net.kyori.adventure.key.Key

fun NekoItem.makeItemModelKey(): Key = Key.key("koish", makeCanonicalId())
fun NekoStack.makeItemModelKey(): Key = Key.key("koish", makeCanonicalId())
fun NekoStack.makeCanonicalId(): String = prototype.makeCanonicalId()
fun NekoItem.makeCanonicalId(): String = id.let { xid -> "${xid.namespace()}/${xid.value()}" }
