package cc.mewcraft.wakame.core

import com.mojang.serialization.DynamicOps
import java.util.function.Supplier

interface Keyable {
    fun <T> keys(ops: DynamicOps<T>): Sequence<T>

    companion object {
        fun forStrings(keys: Supplier<Sequence<String>>): Keyable {
            return object : Keyable {
                override fun <T> keys(ops: DynamicOps<T>): Sequence<T> {
                    return keys.get().map { value: String -> ops.createString(value) }
                }
            }
        }
    }
}
