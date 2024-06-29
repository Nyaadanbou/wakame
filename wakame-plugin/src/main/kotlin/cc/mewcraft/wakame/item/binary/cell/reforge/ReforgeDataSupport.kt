package cc.mewcraft.wakame.item.binary.cell.reforge

import cc.mewcraft.nbt.CompoundTag
import cc.mewcraft.nbt.Tag
import cc.mewcraft.wakame.item.ReforgeBinaryKeys
import kotlin.reflect.KClass

//
// Internal Implementations
//

internal class ReforgeDataHolderImpl(
    private val root: CompoundTag,
) : ReforgeDataHolder {
    override val isEmpty: Boolean
        get() = root.isEmpty

    override fun <T : ReforgeDataAccess<*>> access(clazz: KClass<T>): T {
        val ret = when (clazz) {
            FailureCount::class -> FailureCountImpl(root)
            SuccessCount::class -> SuccessCountImpl(root)
            else -> throw IllegalArgumentException("Unsupported class: $clazz")
        }
        @Suppress("UNCHECKED_CAST")
        return ret as T
    }

    override fun clear() {
        root.tags().clear()
    }

    override fun asTag(): Tag = root
    override fun toString(): String = root.asString()
}

private class FailureCountImpl(
    private val root: CompoundTag,
) : FailureCount {
    override val exists: Boolean
        get() = root.contains(ReforgeBinaryKeys.FAILURE_COUNT)

    override fun get(): Int = root.getInt(ReforgeBinaryKeys.FAILURE_COUNT)
    override fun set(value: Int) = root.putInt(ReforgeBinaryKeys.FAILURE_COUNT, value)
    override fun init() = root.putInt(ReforgeBinaryKeys.FAILURE_COUNT, 0)
    override fun toString(): String = root.asString()
}

private class SuccessCountImpl(
    private val root: CompoundTag,
) : SuccessCount {
    override val exists: Boolean
        get() = root.contains(ReforgeBinaryKeys.SUCCESS_COUNT)

    override fun get(): Int = root.getInt(ReforgeBinaryKeys.SUCCESS_COUNT)
    override fun set(value: Int) = root.putInt(ReforgeBinaryKeys.SUCCESS_COUNT, value)
    override fun init() = root.putInt(ReforgeBinaryKeys.SUCCESS_COUNT, 0)
    override fun toString(): String = root.asString()
}