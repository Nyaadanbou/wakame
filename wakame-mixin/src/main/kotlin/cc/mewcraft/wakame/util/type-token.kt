package cc.mewcraft.wakame.util

import io.leangen.geantyref.TypeToken

inline fun <reified T> typeTokenOf(): TypeToken<T> {
    return TypeToken.get(javaTypeOf<T>()) as TypeToken<T>
}
