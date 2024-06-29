package cc.mewcraft.wakame.item.binary.cell.core

import cc.mewcraft.nbt.CompoundTag
import cc.mewcraft.wakame.GenericKeys
import cc.mewcraft.wakame.Namespaces
import cc.mewcraft.wakame.item.CoreBinaryKeys
import cc.mewcraft.wakame.item.binary.cell.core.attribute.BinaryAttributeCore
import cc.mewcraft.wakame.item.binary.cell.core.empty.BinaryEmptyCore
import cc.mewcraft.wakame.item.binary.cell.core.noop.BinaryNoopCore
import cc.mewcraft.wakame.item.binary.cell.core.skill.BinarySkillCore
import cc.mewcraft.wakame.util.Key

/**
 * A factory used to create [BinaryCore].
 */
object BinaryCoreFactory {

    /**
     * Wraps a NBT tag as [BinaryCore].
     *
     * @param compound the compound
     * @return a new instance of [BinaryCore]
     * @throws IllegalArgumentException if the NBT is malformed
     */
    fun wrap(compound: CompoundTag): BinaryCore {
        if (compound.isEmpty) {
            // There's nothing in the compound,
            // so we consider it an empty core.
            return BinaryEmptyCore()
        }

        val key = Key(compound.getString(CoreBinaryKeys.CORE_IDENTIFIER))

        val ret = when {
            key == GenericKeys.NOOP -> BinaryNoopCore()
            key == GenericKeys.EMPTY -> BinaryEmptyCore()
            key.namespace() == Namespaces.ATTRIBUTE -> BinaryAttributeCore(compound)
            key.namespace() == Namespaces.SKILL -> BinarySkillCore(compound)
            else -> throw IllegalArgumentException("Failed to parse NBT tag ${compound.asString()}")
        }

        return ret
    }

}
