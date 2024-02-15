package cc.mewcraft.wakame.item.binary.curse

import cc.mewcraft.wakame.NekoTags
import cc.mewcraft.wakame.annotation.InternalApi
import cc.mewcraft.wakame.item.CurseKeys
import cc.mewcraft.wakame.item.scheme.SchemeGenerationContext
import cc.mewcraft.wakame.item.scheme.curse.EmptySchemeCurse
import cc.mewcraft.wakame.item.scheme.curse.SchemeCurse
import cc.mewcraft.wakame.registry.ElementRegistry
import cc.mewcraft.wakame.registry.EntityReferenceRegistry
import cc.mewcraft.wakame.registry.getByOrThrow
import me.lucko.helper.shadows.nbt.CompoundShadowTag
import cc.mewcraft.wakame.item.binary.curse.EntityKillsCurse as BEntityKillsCurse
import cc.mewcraft.wakame.item.binary.curse.PeakDamageCurse as BPeakDamageCurse
import cc.mewcraft.wakame.item.scheme.curse.EntityKillsCurse as SEntityKillsCurse
import cc.mewcraft.wakame.item.scheme.curse.PeakDamageCurse as SPeakDamageCurse

object BinaryCurseFactory {
    fun decode(compoundTag: CompoundShadowTag): BinaryCurse {
        if (compoundTag.isEmpty) {
            return emptyBinaryCurse()
        }

        val id = compoundTag.getString(NekoTags.Cell.CURSE_ID)
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

    @OptIn(InternalApi::class)
    fun generate(context: SchemeGenerationContext, schemeCurse: SchemeCurse): BinaryCurse {
        val ret: BinaryCurse = when (schemeCurse) {
            is EmptySchemeCurse -> {
                emptyBinaryCurse()
            }

            is SEntityKillsCurse -> {
                schemeCurse.generate(context.itemLevel)
            }

            is SPeakDamageCurse -> {
                schemeCurse.generate(context.itemLevel)
            }
        }

        return ret
    }
}