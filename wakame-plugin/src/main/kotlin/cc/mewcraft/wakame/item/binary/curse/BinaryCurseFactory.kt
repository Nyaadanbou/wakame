package cc.mewcraft.wakame.item.binary.curse

import cc.mewcraft.wakame.NekoTags
import cc.mewcraft.wakame.item.CurseKeys
import cc.mewcraft.wakame.registry.ElementRegistry
import cc.mewcraft.wakame.registry.EntityReferenceRegistry
import cc.mewcraft.wakame.registry.getByOrThrow
import me.lucko.helper.shadows.nbt.CompoundShadowTag
import cc.mewcraft.wakame.item.binary.curse.EntityKillsCurse as BEntityKillsCurse
import cc.mewcraft.wakame.item.binary.curse.PeakDamageCurse as BPeakDamageCurse

object BinaryCurseFactory {

    fun decode(compoundTag: CompoundShadowTag): BinaryCurse {
        if (compoundTag.isEmpty) {
            return emptyBinaryCurse()
        }

        val id = compoundTag.getString(NekoTags.Cell.CURSE_KEY)
        val ret: BinaryCurse = when (id) {
            CurseKeys.ENTITY_KILLS.value() -> {
                val index = EntityReferenceRegistry.getOrThrow(compoundTag.getString(BEntityKillsCurse.INDEX_TAG_NAME))
                val count = compoundTag.getInt(BEntityKillsCurse.COUNT_TAG_NAME)
                BEntityKillsCurse(index, count)
            }

            CurseKeys.PEAK_DAMAGE.value() -> {
                val element = ElementRegistry.getByOrThrow(compoundTag.getByte(BPeakDamageCurse.ELEMENT_TAG_NAME))
                val amount = compoundTag.getInt(BPeakDamageCurse.AMOUNT_TAG_NAME)
                BPeakDamageCurse(element, amount)
            }

            else -> emptyBinaryCurse()
        }

        return ret
    }

}