package cc.mewcraft.wakame.item.binary.cell

import cc.mewcraft.wakame.attribute.Attribute
import cc.mewcraft.wakame.attribute.AttributeModifier
import cc.mewcraft.wakame.item.CellBinaryKeys
import cc.mewcraft.wakame.item.binary.BaseNekoStack
import cc.mewcraft.wakame.item.binary.cell.core.attribute.BinaryAttributeCore
import cc.mewcraft.wakame.item.binary.cell.core.skill.BinarySkillCore
import cc.mewcraft.wakame.skill.Skill
import cc.mewcraft.wakame.skill.SkillTrigger
import cc.mewcraft.wakame.util.getCompoundOrNull
import cc.mewcraft.wakame.util.getOrPut
import com.google.common.collect.ImmutableListMultimap
import com.google.common.collect.Multimap
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap
import me.lucko.helper.shadows.nbt.CompoundShadowTag

@JvmInline
internal value class ItemCellAccessorImpl(
    private val base: BaseNekoStack,
) : ItemCellAccessor {

    /* Getters */

    private val rootOrNull: CompoundShadowTag?
        get() = base.tags.getCompoundOrNull(CellBinaryKeys.BASE)
    private val rootOrCreate: CompoundShadowTag
        get() = base.tags.getOrPut(CellBinaryKeys.BASE, CompoundShadowTag::create)

    override val snapshot: Map<String, BinaryCell>
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
        return BinaryCellFactory.wrap(compoundTag)
    }

    override fun getAttributeModifiers(neglectCurse: Boolean): Multimap<Attribute, AttributeModifier> {
        // 注意这里不能用 Map 必须用 Multimap
        // 因为会存在同一个属性 Attribute 下的 AttributeModifier
        // 有不同 Operation 的情况

        val ret = ImmutableListMultimap.builder<Attribute, AttributeModifier>()
        for (cell in snapshot.values) {
            // Truth Table
            //   a = curse is neglected
            //   b = test is passed
            //     a,b -> f
            //     !a,b -> f
            //     a,!b -> f
            //     !a,!b -> t
            if (!neglectCurse && !cell.curse.test(base)) {
                continue // the curse has not been unlocked yet
            }

            val core = cell.core
            if (core is BinaryAttributeCore) {
                val modifiers = core.provideAttributeModifiers(base.uuid)
                val modifiersEntries = modifiers.entries
                ret.putAll(modifiersEntries)
            }
        }
        return ret.build()
    }

    override fun getSkills(neglectCurse: Boolean): Multimap<SkillTrigger, Skill> {
        val ret = ImmutableListMultimap.builder<SkillTrigger, Skill>()
        for (cell in snapshot.values) {
            if (!neglectCurse && !cell.curse.test(base)) {
                continue
            }

            val core = cell.core
            if (core is BinarySkillCore) {
                val trigger = core.trigger
                val skill = core.instance
                ret.put(trigger, skill)
            }
        }
        return ret.build()
    }

    /* Setters */

    override fun put(id: String, cell: BinaryCell) {
        rootOrCreate.put(id, cell.asTag())
    }

    override fun edit(id: String, setter: BinaryCell?.() -> BinaryCell) {
        val oldCell = find(id)
        val newCell = oldCell.setter()
        rootOrCreate.put(id, newCell.asTag())
    }

    override fun remove(id: String) {
        rootOrNull?.remove(id)
    }
}