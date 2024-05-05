package cc.mewcraft.wakame.item.binary.cell

import cc.mewcraft.wakame.item.CoreBinaryKeys
import cc.mewcraft.wakame.item.CurseBinaryKeys
import cc.mewcraft.wakame.item.ReforgeBinaryKeys
import cc.mewcraft.wakame.item.binary.cell.core.BinaryCore
import cc.mewcraft.wakame.item.binary.cell.core.BinaryCoreFactory
import cc.mewcraft.wakame.item.binary.cell.curse.BinaryCurse
import cc.mewcraft.wakame.item.binary.cell.curse.BinaryCurseFactory
import cc.mewcraft.wakame.item.binary.cell.reforge.ReforgeDataFactory
import cc.mewcraft.wakame.item.binary.cell.reforge.ReforgeDataHolder
import cc.mewcraft.wakame.util.CompoundShadowTag
import me.lucko.helper.shadows.nbt.CompoundShadowTag
import me.lucko.helper.shadows.nbt.ShadowTag
import kotlin.reflect.KClass
import kotlin.reflect.full.safeCast

private object Impl {
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
    override fun <T : BinaryCore> typedCore(clazz: KClass<T>): T? = Impl.typedCore(this, clazz)
    override fun <T : BinaryCurse> typedCurse(clazz: KClass<T>): T? = Impl.typedCurse(this, clazz)

    override fun asTag(): ShadowTag = CompoundShadowTag {
        // 当对应的数据不存在时，这里的每个 asShadowTag()
        // 应该返回一个没有内容的空 Compound（不是 null）
        put(CoreBinaryKeys.BASE, core.asTag())
        put(CurseBinaryKeys.BASE, curse.asTag())
        put(ReforgeBinaryKeys.BASE, reforge.asTag())
    }
}

internal data class BinaryCellNBTWrapper(
    private val compound: CompoundShadowTag,
) : BinaryCell {
    override var core: BinaryCore
        get() {
            return BinaryCoreFactory.wrap(compound.getCompound(CoreBinaryKeys.BASE))
        }
        set(value) {
            compound.put(CoreBinaryKeys.BASE, value.asTag())
        }

    override var curse: BinaryCurse
        get() {
            return BinaryCurseFactory.wrap(compound.getCompound(CurseBinaryKeys.BASE))
        }
        set(value) {
            compound.put(CurseBinaryKeys.BASE, value.asTag())
        }

    override var reforge: ReforgeDataHolder
        get() {
            return ReforgeDataFactory.wrap(compound.getCompound(ReforgeBinaryKeys.BASE))
        }
        set(value) {
            compound.put(ReforgeBinaryKeys.BASE, value.asTag())
        }

    override fun <T : BinaryCore> typedCore(clazz: KClass<T>): T? = Impl.typedCore(this, clazz)
    override fun <T : BinaryCurse> typedCurse(clazz: KClass<T>): T? = Impl.typedCurse(this, clazz)
    override fun asTag(): ShadowTag = compound
    override fun toString(): String = compound.asString()
}
