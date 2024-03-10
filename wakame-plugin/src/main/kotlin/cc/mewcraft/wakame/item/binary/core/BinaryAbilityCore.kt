package cc.mewcraft.wakame.item.binary.core

import cc.mewcraft.wakame.util.compoundShadowTag
import me.lucko.helper.shadows.nbt.ShadowTag
import net.kyori.adventure.key.Key

data class BinaryAbilityCore(
    override val key: Key,
) : BinaryCore {

    override fun asShadowTag(): ShadowTag = compoundShadowTag {
        putString("key", key.asString())
    }
}