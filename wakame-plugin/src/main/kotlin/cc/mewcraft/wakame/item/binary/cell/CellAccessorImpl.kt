package cc.mewcraft.wakame.item.binary.cell

import cc.mewcraft.wakame.NekoTags
import cc.mewcraft.wakame.ability.Ability
import cc.mewcraft.wakame.ability.BinaryAbilityValue
import cc.mewcraft.wakame.annotation.InternalApi
import cc.mewcraft.wakame.attribute.base.Attribute
import cc.mewcraft.wakame.attribute.base.AttributeModifier
import cc.mewcraft.wakame.item.binary.NekoItemStackImpl
import cc.mewcraft.wakame.item.binary.core.BinaryAttributeCore
import cc.mewcraft.wakame.util.getCompoundOrNull
import com.google.common.collect.ImmutableListMultimap
import com.google.common.collect.Multimap
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap
import it.unimi.dsi.fastutil.objects.Object2ObjectMap
import me.lucko.helper.shadows.nbt.CompoundShadowTag

internal class CellAccessorImpl(
    private val base: NekoItemStackImpl,
) : CellAccessor {
    ////// CellAccessor //////

    // FIXME do we really need it?
    //  the caching mechanism should be implemented properly
    //  in a wider scope of the project to make it effective,
    //  not just here
    private val cache: Object2ObjectMap<String, BinaryCell> = Object2ObjectArrayMap(tags.size()) // cache binary cells

    @OptIn(InternalApi::class)
    override val tags: CompoundShadowTag
        get() = base.tags.getCompound(NekoTags.Cell.ROOT)

    override fun get(id: String): BinaryCell? {
        val compoundTag = tags.getCompoundOrNull(id) ?: return null
        // don't use computeIfAbsent to avoid creating non-capturing lambda
        val cell = cache[id]
        if (cell == null) {
            val cell1 = BinaryCellFactory.decode(compoundTag)
            cache[id] = cell1
            return cell1
        }
        return cell
    }

    override fun asMap(): Map<String, BinaryCell> {
        val ret = Object2ObjectArrayMap<String, BinaryCell>(tags.size())
        for (key in tags.keySet()) {
            get(key)?.let { ret.put(key, it) }
        }
        return ret
    }

    override fun getModifiers(): Multimap<out Attribute, AttributeModifier> {
        // 注意这里不能用 Map，必须用 Multimap
        // 因为会存在同一个属性 Attribute
        // 但有多个 AttributeModifier
        // 并且 Operation 不同的情况

        val multimap = ImmutableListMultimap.builder<Attribute, AttributeModifier>()

        for (binaryCell in asMap().values) {
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

    ////// CellMapSetter //////

    private fun edit(consumer: CompoundShadowTag.() -> Unit) {
        tags.consumer()
    }

    override fun put(id: String, cell: BinaryCell) {
        cache.remove(id) // remove cache
        edit {
            put(id, cell.asShadowTag())
        }
    }

    override fun edit(id: String, setter: BinaryCell?.() -> BinaryCell) {
        cache.remove(id)
        val oldCell = get(id)
        val newCell = oldCell.setter()
        edit {
            put(id, newCell.asShadowTag())
        }
    }

    override fun remove(id: String) {
        cache.remove(id) // remove cache
        edit {
            remove(id)
        }
    }
}