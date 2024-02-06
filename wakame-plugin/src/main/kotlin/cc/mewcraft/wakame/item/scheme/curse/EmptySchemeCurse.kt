package cc.mewcraft.wakame.item.scheme.curse

import cc.mewcraft.wakame.item.Curse
import cc.mewcraft.wakame.item.binary.curse.BinaryCurse
import cc.mewcraft.wakame.item.binary.curse.emptyBinaryCurse
import net.kyori.adventure.key.Key

internal data object EmptySchemeCurse : SchemeCurse {
    override fun generate(scalingFactor: Int): BinaryCurse = emptyBinaryCurse()

    override val key: Key = Key.key(Curse.NAMESPACE, "empty")
}