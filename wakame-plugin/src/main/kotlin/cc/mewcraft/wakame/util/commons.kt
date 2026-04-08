@file:OptIn(ExperimentalContracts::class)

package cc.mewcraft.wakame.util

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

inline fun <T> T?.applyIfNull(block: () -> Unit): T? {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }
    if (this == null) {
        block()
    }
    return this
}