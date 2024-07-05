package cc.mewcraft.wakame.item.binary.cell

import cc.mewcraft.nbt.CompoundTag
import cc.mewcraft.nbt.Tag
import cc.mewcraft.wakame.display.LoreLine
import cc.mewcraft.wakame.item.CoreBinaryKeys
import cc.mewcraft.wakame.item.CurseBinaryKeys
import cc.mewcraft.wakame.item.ReforgeBinaryKeys
import cc.mewcraft.wakame.item.binary.cell.core.BinaryCore
import cc.mewcraft.wakame.item.binary.cell.core.BinaryCoreFactory
import cc.mewcraft.wakame.item.binary.cell.curse.BinaryCurse
import cc.mewcraft.wakame.item.binary.cell.curse.BinaryCurseFactory
import cc.mewcraft.wakame.item.binary.cell.reforge.ReforgeDataFactory
import cc.mewcraft.wakame.item.binary.cell.reforge.ReforgeDataHolder
import cc.mewcraft.wakame.util.CompoundTag
import kotlin.reflect.KClass
import kotlin.reflect.full.safeCast

private object CellSupport {
    fun <T : BinaryCore> typedCore(instance: BinaryCell, clazz: KClass<T>): T? {
        return clazz.safeCast(instance.core)
    }

    fun <T : BinaryCurse> typedCurse(instance: BinaryCell, clazz: KClass<T>): T? {
        return clazz.safeCast(instance.curse)
    }
}

internal data class BinaryCellDataHolder(
    override var core: BinaryCore,
    override var curse: BinaryCurse,
    override var reforge: ReforgeDataHolder,
) : BinaryCell {
    override fun <T : BinaryCore> typedCore(clazz: KClass<T>): T? {
        return CellSupport.typedCore(this, clazz)
    }

    override fun <T : BinaryCurse> typedCurse(clazz: KClass<T>): T? {
        return CellSupport.typedCurse(this, clazz)
    }

    override fun serializeAsTag(): Tag = CompoundTag {
        // 当对应的数据不存在时，这里的每个 asShadowTag()
        // 应该返回一个没有内容的空 Compound（不是 null）
        put(CoreBinaryKeys.BASE, core.serializeAsTag())
        put(CurseBinaryKeys.BASE, curse.serializeAsTag())
        put(ReforgeBinaryKeys.BASE, reforge.serializeAsTag())
    }

    override fun provideDisplayLore(): LoreLine {
        throw UnsupportedOperationException()
    }
}

internal data class BinaryCellTagWrapper(
    private val compound: CompoundTag,
) : BinaryCell {
    override var core: BinaryCore
        get() {
            return BinaryCoreFactory.wrap(compound.getCompound(CoreBinaryKeys.BASE))
        }
        set(value) {
            compound.put(CoreBinaryKeys.BASE, value.serializeAsTag())
        }

    override var curse: BinaryCurse
        get() {
            return BinaryCurseFactory.wrap(compound.getCompound(CurseBinaryKeys.BASE))
        }
        set(value) {
            compound.put(CurseBinaryKeys.BASE, value.serializeAsTag())
        }

    override var reforge: ReforgeDataHolder
        get() {
            return ReforgeDataFactory.wrap(compound.getCompound(ReforgeBinaryKeys.BASE))
        }
        set(value) {
            compound.put(ReforgeBinaryKeys.BASE, value.serializeAsTag())
        }

    override fun <T : BinaryCore> typedCore(clazz: KClass<T>): T? {
        return CellSupport.typedCore(this, clazz)
    }

    override fun <T : BinaryCurse> typedCurse(clazz: KClass<T>): T? {
        return CellSupport.typedCurse(this, clazz)
    }

    override fun serializeAsTag(): Tag {
        return compound
    }

    override fun provideDisplayLore(): LoreLine {
        return core.provideDisplayLore()
    }

    override fun toString(): String {
        return compound.asString()
    }
}
