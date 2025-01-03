package cc.mewcraft.wakame.util

import kotlin.reflect.KClass

/**
 * Gets the internal name of the class (e.g. ``java/lang/Object``)
 */
val Class<*>.internalName get() = name.replace('.', '/')

/**
 * Gets the internal name of the class (e.g. ``kotlin/Any``)
 */
val KClass<*>.internalName get() = java.internalName