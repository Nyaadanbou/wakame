package cc.mewcraft.wakame.item

import me.lucko.helper.shadows.nbt.ShadowTag

interface ShadowTagLike {
    fun asShadowTag(): ShadowTag
}