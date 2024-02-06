package cc.mewcraft.wakame.item.binary.curse

import cc.mewcraft.wakame.item.Curse
import me.lucko.helper.shadows.nbt.CompoundShadowTag
import me.lucko.helper.shadows.nbt.ShadowTag
import net.kyori.adventure.key.Key

internal object EmptyBinaryCurse : BinaryCurse {
    override val key: Key = Key.key(Curse.NAMESPACE, "empty")

    override fun test(context: BinaryCurseContext): Boolean = true
    override fun asShadowTag(): ShadowTag = CompoundShadowTag.create()
}