package cc.mewcraft.wakame.item.scheme.curse

import cc.mewcraft.wakame.NekoNamespaces
import cc.mewcraft.wakame.annotation.InternalApi
import cc.mewcraft.wakame.item.binary.curse.BinaryCurse
import cc.mewcraft.wakame.item.binary.curse.emptyBinaryCurse
import cc.mewcraft.wakame.item.scheme.SchemeGenerationContext
import net.kyori.adventure.key.Key

@InternalApi
internal data object EmptySchemeCurse : SchemeCurse {
    override fun generate(context: SchemeGenerationContext): BinaryCurse = emptyBinaryCurse()

    override val key: Key = Key.key(NekoNamespaces.CURSE, "empty")
}