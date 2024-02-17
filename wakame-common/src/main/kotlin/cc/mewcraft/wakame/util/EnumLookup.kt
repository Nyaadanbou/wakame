package cc.mewcraft.wakame.util

import org.koin.core.context.GlobalContext
import org.slf4j.Logger

object EnumLookup {
    inline fun <reified E : Enum<E>> lookup(name: String): Result<E> =
        runCatching {
            enumValueOf<E>(name.trim().uppercase().replace('-', '_').replace(' ', '_'))
        }

    inline fun <reified E : Enum<E>> lookup(name: String, defaultValue: E): E =
        lookup<E>(name).getOrElse {
            GlobalContext.get().get<Logger>().warn("找不到名为 $name 的枚举实例，将采用默认值: $defaultValue")
            defaultValue
        }
}
