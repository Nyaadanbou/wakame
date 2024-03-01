package cc.mewcraft.wakame.util

object EnumLookup {
    inline fun <reified E : Enum<E>> lookup(name: String): Result<E> =
        runCatching {
            enumValueOf<E>(name.trim().uppercase().replace('-', '_').replace(' ', '_'))
        }

    inline fun <reified E : Enum<E>> lookup(name: String, defaultValue: E): E =
        lookup<E>(name).getOrElse { defaultValue }
}
