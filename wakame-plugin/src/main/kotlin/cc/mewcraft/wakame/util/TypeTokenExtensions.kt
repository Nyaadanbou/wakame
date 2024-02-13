package cc.mewcraft.wakame.util

import io.leangen.geantyref.TypeToken

internal inline fun <reified T> typeTokenOf(): TypeToken<T> = object : TypeToken<T>() {}
