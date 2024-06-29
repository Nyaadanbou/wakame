package cc.mewcraft.wakame.item.binary.cell.curse.type

import cc.mewcraft.nbt.CompoundTag
import cc.mewcraft.nbt.Tag
import cc.mewcraft.wakame.GenericKeys
import cc.mewcraft.wakame.item.binary.NekoStack
import cc.mewcraft.wakame.item.binary.cell.curse.BinaryCurse
import net.kyori.adventure.key.Key

/**
 * A special curse that always returns `true`.
 */
interface BinaryEmptyCurse : BinaryCurse

fun BinaryEmptyCurse(): BinaryEmptyCurse {
    return BinaryEmptyCurseImpl
}

//
// Internal Implementations
//

// 我们不需要给 BinaryEmptyCurse 创建一个专门的 NBTWrapper，
// 因为当我们读取一个 NBT Compound 的时候，如果已经知道没有内容，
// 那么返回空对象即可！
internal data object BinaryEmptyCurseImpl : BinaryEmptyCurse {
    override val key: Key = GenericKeys.EMPTY
    override fun test(context: NekoStack): Boolean = true
    override fun clear() = Unit
    override fun asTag(): Tag = EMPTY_COMPOUND
    private val EMPTY_COMPOUND = CompoundTag.create()
}