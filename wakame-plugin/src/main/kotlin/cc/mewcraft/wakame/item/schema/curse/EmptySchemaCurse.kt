package cc.mewcraft.wakame.item.schema.curse

import cc.mewcraft.wakame.NekoNamespaces
import cc.mewcraft.wakame.annotation.InternalApi
import cc.mewcraft.wakame.item.binary.curse.BinaryCurse
import cc.mewcraft.wakame.item.binary.curse.emptyBinaryCurse
import cc.mewcraft.wakame.item.schema.SchemaGenerationContext
import net.kyori.adventure.key.Key

@InternalApi
internal data object EmptySchemaCurse : SchemaCurse {
    override fun generate(context: SchemaGenerationContext): BinaryCurse = emptyBinaryCurse()

    override val key: Key = Key.key(NekoNamespaces.CURSE, "empty")
}