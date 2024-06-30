package cc.mewcraft.wakame.config

import cc.mewcraft.commons.provider.Provider
import cc.mewcraft.commons.provider.immutable.orElse
import java.lang.reflect.Type
import kotlin.reflect.KType
import kotlin.reflect.jvm.javaType
import kotlin.reflect.typeOf

data class PatchedConfigProvider(
    val patch: ConfigProvider,
    val default: ConfigProvider
)

inline fun <reified T : Any> PatchedConfigProvider.entry(vararg path: String): Provider<T> =
    entry(typeOf<T>().javaType, *path)

fun <T : Any> PatchedConfigProvider.entry(type: KType, vararg path: String): Provider<T> =
    entry(type.javaType, *path)

fun <T : Any> PatchedConfigProvider.entry(type: Type, vararg path: String): Provider<T> =
    patch.optionalEntry<T>(type, *path).orElse(default.entry(type, *path))

inline fun <reified T : Any> PatchedConfigProvider.entry(vararg paths: Array<String>): Provider<T> =
    entry(typeOf<T>().javaType, *paths)

inline fun <reified T : Any> PatchedConfigProvider.entry(type: KType, vararg paths: Array<String>): Provider<T> =
    entry(type.javaType, *paths)

inline fun <reified T : Any> PatchedConfigProvider.entry(type: Type, vararg paths: Array<String>): Provider<T> =
    patch.optionalEntry<T>(*paths).orElse(default.entry(type, *paths))

inline fun <reified T : Any> PatchedConfigProvider.optionalEntry(vararg path: String): Provider<T?> =
    optionalEntry(typeOf<T>().javaType, *path)

fun <T : Any> PatchedConfigProvider.optionalEntry(type: KType, vararg path: String): Provider<T?> =
    optionalEntry(type.javaType, *path)

fun <T : Any> PatchedConfigProvider.optionalEntry(type: Type, vararg path: String): Provider<T?> =
    patch.optionalEntry<T>(type, *path).orElse(default.optionalEntry(type, *path))