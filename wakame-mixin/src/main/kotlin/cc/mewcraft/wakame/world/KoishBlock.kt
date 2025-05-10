package cc.mewcraft.wakame.world

import cc.mewcraft.wakame.registry2.BuiltInRegistries
import cc.mewcraft.wakame.util.Identifiers
import net.kyori.adventure.key.Key
import net.kyori.adventure.key.Keyed

class KoishBlock(
    val test: String,
) : Keyed {

    override fun key(): Key {
        return BuiltInRegistries.BLOCK.getId(this) ?: Identifiers.of("unregistered")
    }

}