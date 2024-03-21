package cc.mewcraft.wakame.item.binary.curse

import cc.mewcraft.wakame.NekoNamespaces
import cc.mewcraft.wakame.annotation.InternalApi
import cc.mewcraft.wakame.item.binary.NekoStack
import me.lucko.helper.shadows.nbt.CompoundShadowTag
import me.lucko.helper.shadows.nbt.ShadowTag
import net.kyori.adventure.key.Key

@InternalApi
internal object EmptyBinaryCurse : BinaryCurse {
    override val key: Key = Key.key(NekoNamespaces.CURSE, "empty")
    override fun test(context: NekoStack): Boolean = true
    override fun asShadowTag(): ShadowTag = CompoundShadowTag.create()
}