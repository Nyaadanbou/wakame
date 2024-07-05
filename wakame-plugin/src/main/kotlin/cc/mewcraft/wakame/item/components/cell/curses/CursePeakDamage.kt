package cc.mewcraft.wakame.item.components.cell.curses

import cc.mewcraft.nbt.CompoundTag
import cc.mewcraft.nbt.Tag
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.CurseBinaryKeys
import cc.mewcraft.wakame.item.CurseConstants
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.components.cell.Curse
import cc.mewcraft.wakame.item.components.cell.CurseType
import cc.mewcraft.wakame.registry.ElementRegistry
import cc.mewcraft.wakame.util.CompoundTag
import cc.mewcraft.wakame.util.toStableShort
import net.kyori.adventure.key.Key

/**
 * 从 NBT 创建一个 [CursePeakDamage].
 */
fun CursePeakDamage(nbt: CompoundTag): CursePeakDamage {
    val element = nbt.getElement(CursePeakDamage.TAG_ELEMENT)
    val amount = nbt.getInt(CursePeakDamage.TAG_AMOUNT)
    return CursePeakDamage(element, amount)
}

/**
 * Checks the highest damage dealt by the item.
 *
 * @property element the required source of damage
 * @property amount the required amount of damage
 */
data class CursePeakDamage(
    val element: Element,
    val amount: Int,
) : Curse {
    override val key: Key = CurseConstants.createKey { PEAK_DAMAGE }
    override val type: CurseType<CursePeakDamage> = Type
    override val isEmpty: Boolean = false

    override fun isLocked(context: NekoStack): Boolean {
        return !isUnlocked(context)
    }

    override fun isUnlocked(context: NekoStack): Boolean {
        // TODO 完成组件 ItemTracks
        // return context.statistics.PEAK_DAMAGE[element] >= amount
        return true
    }

    override fun serializeAsTag(): Tag = CompoundTag {
        putString(CurseBinaryKeys.CURSE_IDENTIFIER, key.asString())
        putShort(TAG_AMOUNT, amount.toStableShort())
        putByte(TAG_ELEMENT, element.binaryId)
    }

    internal companion object Type : CurseType<CursePeakDamage> {
        const val TAG_AMOUNT = "amount"
        const val TAG_ELEMENT = "element"
    }
}

private fun CompoundTag.getElement(key: String): Element {
    return ElementRegistry.getBy(this.getByte(key))
}