package cc.mewcraft.wakame.util

import java.lang.reflect.Type
import kotlin.reflect.jvm.javaType
import kotlin.reflect.typeOf

inline fun <reified T> javaTypeOf(): Type {
    return typeOf<T>().javaType
}