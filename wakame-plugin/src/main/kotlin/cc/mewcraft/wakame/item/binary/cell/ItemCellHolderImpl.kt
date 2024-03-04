package cc.mewcraft.wakame.item.binary.cell

import cc.mewcraft.wakame.NekoTags
import cc.mewcraft.wakame.ability.Ability
import cc.mewcraft.wakame.ability.BinaryAbilityValue
import cc.mewcraft.wakame.attribute.Attribute
import cc.mewcraft.wakame.attribute.AttributeModifier
import cc.mewcraft.wakame.item.binary.NekoItemStackImpl
import cc.mewcraft.wakame.item.binary.core.BinaryAttributeCore
import cc.mewcraft.wakame.util.getCompoundOrNull
import cc.mewcraft.wakame.util.getOrPut
import com.google.common.collect.ImmutableListMultimap
import com.google.common.collect.Multimap
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap
import it.unimi.dsi.fastutil.objects.Object2ObjectMap
import me.lucko.helper.shadows.nbt.CompoundShadowTag

internal class ItemCellHolderImpl(
    private val base: NekoItemStackImpl,
) : ItemCellHolder {

    /* Getters */

    // FIXME do we really need it?
    //  the caching mechanism should be implemented properly
    //  in a wider scope of the project to make it effective,
    //  not just here
    private val cache: Object2ObjectMap<String, BinaryCell> = Object2ObjectArrayMap() // cache binary cells

    private val rootOrNull: CompoundShadowTag?
        get() = base.tags.getCompoundOrNull(NekoTags.Cell.ROOT)
    private val rootOrCreate: CompoundShadowTag
        get() = base.tags.getOrPut(NekoTags.Cell.ROOT, CompoundShadowTag::create)

    override val map: Map<String, BinaryCell>
        get() {
            val root = rootOrNull ?: return emptyMap()
            val ret = Object2ObjectArrayMap<String, BinaryCell>(root.size())
            for (key in root.keySet()) {
                get(key)?.let { ret.put(key, it) }
            }
            return ret
        }

    override fun get(id: String): BinaryCell? {
        val compoundTag = rootOrNull?.getCompoundOrNull(id) ?: return null
        return cache.getOrPut(id) { BinaryCellFactory.decode(compoundTag) }
    }

    override fun getModifiers(): Multimap<out Attribute, AttributeModifier> {
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

    override fun getAbilities(): Map<out Ability, BinaryAbilityValue> {
        TODO("Not yet implemented")
    }

    /* Setters */

    override fun put(id: String, cell: BinaryCell) {
        cache.remove(id) // invalidate cache
        rootOrCreate.put(id, cell.asShadowTag())
    }

    override fun edit(id: String, setter: BinaryCell?.() -> BinaryCell) {
        cache.remove(id) // invalidate cache
        val oldCell = get(id)
        val newCell = oldCell.setter()
        rootOrCreate.put(id, newCell.asShadowTag())
    }

    override fun remove(id: String) {
        cache.remove(id) // invalidate cache
        rootOrNull?.remove(id)
    }
}