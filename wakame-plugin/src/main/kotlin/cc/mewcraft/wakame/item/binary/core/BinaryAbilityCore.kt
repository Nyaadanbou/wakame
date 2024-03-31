package cc.mewcraft.wakame.item.binary.core

import cc.mewcraft.wakame.NekoTags
import cc.mewcraft.wakame.util.CompoundShadowTag
import me.lucko.helper.shadows.nbt.ShadowTag
import net.kyori.adventure.key.Key

data class BinaryAbilityCore(
    override val key: Key,
) : BinaryCore {
    override fun asShadowTag(): ShadowTag = CompoundShadowTag {
        putString(NekoTags.Cell.CORE_KEY, key.asString())
    }
}