package cc.mewcraft.wakame.item.binary.cell

import cc.mewcraft.wakame.NekoTags
import cc.mewcraft.wakame.ability.Ability
import cc.mewcraft.wakame.ability.PlainAbilityData
import cc.mewcraft.wakame.attribute.Attribute
import cc.mewcraft.wakame.attribute.AttributeModifier
import cc.mewcraft.wakame.item.binary.NekoStackImpl
import cc.mewcraft.wakame.item.binary.core.BinaryAttributeCore
import cc.mewcraft.wakame.util.getCompoundOrNull
import cc.mewcraft.wakame.util.getOrPut
import com.google.common.collect.ImmutableListMultimap
import com.google.common.collect.Multimap
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap
import me.lucko.helper.shadows.nbt.CompoundShadowTag

internal class ItemCellHolderImpl(
    private val base: NekoStackImpl,
) : ItemCellHolder {

    /* Getters */

    private val rootOrNull: CompoundShadowTag?
        get() = base.tags.getCompoundOrNull(NekoTags.Cell.ROOT)
    private val rootOrCreate: CompoundShadowTag
        get() = base.tags.getOrPut(NekoTags.Cell.ROOT, CompoundShadowTag::create)

    override val map: Map<String, BinaryCell>
        get() {
            val root = rootOrNull ?: return emptyMap()
            val ret = Object2ObjectArrayMap<String, BinaryCell>(root.size())
            for (key in root.keySet()) {
                find(key)?.let { ret.put(key, it) }
            }
            return ret
        }

    override fun find(id: String): BinaryCell? {
        val compoundTag = rootOrNull?.getCompoundOrNull(id) ?: return null
        return BinaryCellFactory.decode(compoundTag)
    }

    override fun getAttributeModifiers(): Multimap<Attribute, AttributeModifier> {
        // 注意这里不能用 Map，必须用 Multimap
        // 因为会存在同一个属性 Attribute
        // 但有多个 AttributeModifier
        // 并且 Operation 不同的情况

        val multimap = ImmutableListMultimap.builder<Attribute, AttributeModifier>()
        for (binaryCell in map.values) {
            if (!binaryCell.binaryCurse.test(base)) {
                continue // curse has not been unlocked yet
            }

            val core = binaryCell.binaryCore
            if (core is BinaryAttributeCore) {
                val modifiers = core.provideAttributeModifiers(base.uuid)
                val modifiersEntries = modifiers.entries
                multimap.putAll(modifiersEntries)
            }
        }
        return multimap.build()
    }

    override fun getActiveAbilities(): Map<Ability, PlainAbilityData> {
        TODO("Not yet implemented")
    }

    /* Setters */

    override fun put(id: String, cell: BinaryCell) {
        rootOrCreate.put(id, cell.asShadowTag())
    }

    override fun edit(id: String, setter: BinaryCell?.() -> BinaryCell) {
        val oldCell = find(id)
        val newCell = oldCell.setter()
        rootOrCreate.put(id, newCell.asShadowTag())
    }

    override fun remove(id: String) {
        rootOrNull?.remove(id)
    }
}