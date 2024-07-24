package cc.mewcraft.wakame.item.components.cells.curses

import cc.mewcraft.nbt.CompoundTag
import cc.mewcraft.nbt.Tag
import cc.mewcraft.wakame.display.LoreLine
import cc.mewcraft.wakame.display.NameLine
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.CurseBinaryKeys
import cc.mewcraft.wakame.item.CurseConstants
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.components.cells.Curse
import cc.mewcraft.wakame.item.components.cells.CurseConfig
import cc.mewcraft.wakame.item.components.cells.CurseType
import cc.mewcraft.wakame.item.components.tracks.TrackTypes
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
    override val key: Key
        get() = Type.key
    override val type: CurseType<CursePeakDamage> = Type
    override val isEmpty: Boolean = false

    override fun isLocked(context: NekoStack): Boolean {
        return !isUnlocked(context)
    }

    override fun isUnlocked(context: NekoStack): Boolean {
        val tracks = context.components.get(ItemComponentTypes.TRACKS) ?: return false
        val track = tracks.get(TrackTypes.PEAK_DAMAGE) ?: return false // 如果没有统计数据, 则返回锁定状态
        val peakDamage = track.get(element)
        return peakDamage >= amount
    }

    override fun serializeAsTag(): Tag = CompoundTag {
        putString(CurseBinaryKeys.CURSE_IDENTIFIER, key.asString())
        putShort(TAG_AMOUNT, amount.toStableShort())
        putByte(TAG_ELEMENT, element.binaryId)
    }

    override fun provideTooltipName(): NameLine {
        return NameLine.simple(config.displayName)
    }

    override fun provideTooltipLore(): LoreLine {
        return LoreLine.simple(key, listOf(tooltip.render()))
    }

    companion object Type : CurseType<CursePeakDamage> {
        const val TAG_AMOUNT = "amount"
        const val TAG_ELEMENT = "element"
        val key = CurseConstants.createKey { PEAK_DAMAGE }

        private val config = CurseConfig(CurseConstants.PEAK_DAMAGE)
        private val tooltip = config.SingleTooltip()
    }
}

private fun CompoundTag.getElement(key: String): Element {
    return ElementRegistry.getBy(this.getByte(key))
}