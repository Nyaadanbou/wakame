package cc.mewcraft.wakame.item.binary.cell

import cc.mewcraft.wakame.NekoTags
import cc.mewcraft.wakame.ability.Ability
import cc.mewcraft.wakame.ability.BinaryAbilityValue
import cc.mewcraft.wakame.attribute.base.Attribute
import cc.mewcraft.wakame.attribute.base.AttributeModifier
import cc.mewcraft.wakame.item.binary.NekoItemStackImpl
import cc.mewcraft.wakame.item.binary.core.BinaryAttributeCore
import cc.mewcraft.wakame.util.getCompoundOrNull
import cc.mewcraft.wakame.util.getOrPut
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
    private val cache: Object2ObjectMap<String, BinaryCell> by lazy(LazyThreadSafetyMode.NONE) { Object2ObjectArrayMap() } // cache binary cells

    private val rootOrNull: CompoundShadowTag?
        get() = base.tags.getCompoundOrNull(NekoTags.Cell.ROOT)
    private val rootOrCreate: CompoundShadowTag
        get() = base.tags.getOrPut(NekoTags.Cell.ROOT, CompoundShadowTag::create)

    override fun get(id: String): BinaryCell? {
        val compoundTag = rootOrNull
            ?.getCompoundOrNull(id)
            ?: return null
        // don't use computeIfAbsent to avoid creating non-capturing lambda
        var cell = cache[id]
        if (cell == null) {
            cell = BinaryCellFactory.decode(compoundTag)
            cache[id] = cell
            return cell
        }
        return cell
    }

    override fun asMap(): Map<String, BinaryCell> {
        val tags = rootOrNull
            ?: return emptyMap()
        val ret = Object2ObjectArrayMap<String, BinaryCell>(tags.size()) // pre-allocate
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

    override fun put(id: String, cell: BinaryCell) {
        cache.remove(id) // remove cache
        rootOrCreate.put(id, cell.asShadowTag())
    }

    override fun edit(id: String, setter: BinaryCell?.() -> BinaryCell) {
        cache.remove(id)
        val oldCell = get(id)
        val newCell = oldCell.setter()
        rootOrCreate.put(id, newCell.asShadowTag())
    }

    override fun remove(id: String) {
        cache.remove(id) // remove cache
        rootOrNull?.remove(id)
    }
}